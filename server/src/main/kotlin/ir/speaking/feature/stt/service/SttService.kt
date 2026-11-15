package ir.speaking.feature.stt.service

import com.k2fsa.sherpa.onnx.EndpointConfig
import com.k2fsa.sherpa.onnx.EndpointRule
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

/**
 * Unified STT service supporting both high-accuracy SenseVoice (offline non-autoregressive)
 * and Zipformer (online streaming transducer).
 */
@Single
class SttService(
    private val config: SttConfig
) {
    private val logger = LoggerFactory.getLogger(SttService::class.java)
    private val limiter: StreamConcurrencyLimiter =
        StreamConcurrencyLimiter(config.maxConcurrentStreams)

    val sampleRate: Int get() = config.sampleRate
    val maxConcurrentStreams: Int get() = limiter.maxConcurrent
    val activeStreams: Int get() = limiter.active

    val isSenseVoice: Boolean
    private val offlineRecognizer: OfflineRecognizer?
    private val onlineRecognizer: OnlineRecognizer?

    init {
        var modelDir = findValidModelDir()
        if (modelDir == null && config.autoDownload) {
            val targetDir = File(config.modelDir)
            logger.info("STT model files not found. Attempting automatic download of SenseVoice model to: {}", targetDir.absolutePath)
            if (downloadSenseVoiceModelIfMissing(targetDir)) {
                modelDir = targetDir
            }
        }

        val resolvedModelDir = modelDir
            ?: throw IllegalArgumentException(
                "STT model directory not found or incomplete: ${config.modelDir}. " +
                "Ensure either SenseVoice (model.int8.onnx + tokens.txt) or Zipformer (encoder, decoder, joiner, tokens.txt) are present."
            )

        logger.info("Using STT model directory: {}", resolvedModelDir.absolutePath)

        val senseVoiceModel = findSenseVoiceModel(resolvedModelDir)
        val tokensFile = File(resolvedModelDir, "tokens.txt")
        require(tokensFile.isFile) { "tokens.txt not found in ${resolvedModelDir.absolutePath}" }

        if (senseVoiceModel != null) {
            isSenseVoice = true
            onlineRecognizer = null
            logger.info("Initializing SenseVoice STT engine:\n  model={}\n  tokens={}\n  lang={}\n  useItn={}",
                senseVoiceModel, tokensFile.absolutePath, config.language, config.useItn)

            val senseVoiceConfig = OfflineSenseVoiceModelConfig.builder()
                .setModel(senseVoiceModel)
                .setLanguage(config.language)
                .setInverseTextNormalization(config.useItn)
                .build()

            val modelConfig = OfflineModelConfig.builder()
                .setSenseVoice(senseVoiceConfig)
                .setTokens(tokensFile.absolutePath)
                .setNumThreads(config.numThreads)
                .setDebug(false)
                .setProvider("cpu")
                .build()

            val recognizerConfig = OfflineRecognizerConfig.builder()
                .setOfflineModelConfig(modelConfig)
                .setDecodingMethod("greedy_search")
                .build()

            offlineRecognizer = OfflineRecognizer(recognizerConfig)
            logger.info("SenseVoice STT engine successfully initialized on CPU with {} threads", config.numThreads)
        } else {
            isSenseVoice = false
            offlineRecognizer = null
            logger.info("Initializing Zipformer Online Transducer STT engine...")

            val encoder = findModel(resolvedModelDir, "encoder", ".int8.onnx", ".onnx")
                ?: throw IllegalArgumentException("encoder .onnx not found in ${resolvedModelDir.absolutePath}")
            val decoder = findModel(resolvedModelDir, "decoder", ".onnx", ".int8.onnx")
                ?: throw IllegalArgumentException("decoder .onnx not found in ${resolvedModelDir.absolutePath}")
            val joiner = findModel(resolvedModelDir, "joiner", ".int8.onnx", ".onnx")
                ?: throw IllegalArgumentException("joiner .onnx not found in ${resolvedModelDir.absolutePath}")

            val transducer = OnlineTransducerModelConfig.builder()
                .setEncoder(encoder)
                .setDecoder(decoder)
                .setJoiner(joiner)
                .build()

            val modelConfig = OnlineModelConfig.builder()
                .setTransducer(transducer)
                .setTokens(tokensFile.absolutePath)
                .setNumThreads(config.numThreads)
                .setDebug(true)
                .setProvider("cpu")
                .build()

            val featureConfig = FeatureConfig.builder()
                .setSampleRate(config.sampleRate)
                .build()

            val endpointConfig = EndpointConfig.builder()
                .setRule1(
                    EndpointRule.builder()
                        .setMustContainNonSilence(config.rule1MustContainNonSilence)
                        .setMinTrailingSilence(config.rule1MinTrailingSilence)
                        .setMinUtteranceLength(config.rule1MinUtteranceLength)
                        .build()
                )
                .setRule2(
                    EndpointRule.builder()
                        .setMustContainNonSilence(config.rule2MustContainNonSilence)
                        .setMinTrailingSilence(config.rule2MinTrailingSilence)
                        .setMinUtteranceLength(config.rule2MinUtteranceLength)
                        .build()
                )
                .setRule3(
                    EndpointRule.builder()
                        .setMustContainNonSilence(config.rule3MustContainNonSilence)
                        .setMinTrailingSilence(config.rule3MinTrailingSilence)
                        .setMinUtteranceLength(config.rule3MinUtteranceLength)
                        .build()
                )
                .build()

            val recognizerConfig = OnlineRecognizerConfig.builder()
                .setFeatureConfig(featureConfig)
                .setOnlineModelConfig(modelConfig)
                .setEndpointConfig(endpointConfig)
                .setEnableEndpoint(true)
                .setDecodingMethod("greedy_search")
                .build()

            onlineRecognizer = OnlineRecognizer(recognizerConfig)
            logger.info("Zipformer Online STT engine successfully initialized")
        }
    }

    /**
     * Attempts to acquire a concurrency permit and create a unified [SttStreamSession].
     * Returns null if max concurrent streams limit is reached.
     */
    fun tryAcquireSession(): SttStreamSession? {
        if (!limiter.tryAcquire()) {
            return null
        }
        return try {
            if (isSenseVoice) {
                val recognizer = offlineRecognizer ?: throw IllegalStateException("Offline recognizer not initialized")
                SenseVoiceStreamSession(
                    recognizer = recognizer,
                    sampleRate = config.sampleRate,
                    minTrailingSilenceSamples = (config.sampleRate * config.rule2MinTrailingSilence).toInt()
                )
            } else {
                val recognizer = onlineRecognizer ?: throw IllegalStateException("Online recognizer not initialized")
                val stream = recognizer.createStream()
                ZipformerStreamSession(
                    recognizer = recognizer,
                    stream = stream,
                    sampleRate = config.sampleRate
                )
            }
        } catch (e: Throwable) {
            limiter.release()
            throw e
        }
    }

    /**
     * Releases a session and returns its concurrency permit.
     */
    fun releaseSession(session: SttStreamSession?) {
        if (session == null) return
        try {
            session.release()
        } catch (_: Throwable) {
        } finally {
            limiter.release()
        }
    }

    /**
     * Transcribes a complete audio waveform (samples at 16kHz) and returns the recognized text.
     */
    fun transcribeWaveform(samples: FloatArray): String? {
        if (samples.isEmpty()) return ""
        if (!limiter.tryAcquire()) return null

        return try {
            if (isSenseVoice) {
                val recognizer = offlineRecognizer ?: return null
                val bounds = findSpeechBounds(samples, config.sampleRate) ?: return ""
                val (start, end) = bounds
                val length = end - start
                if (length < (config.sampleRate * 0.1f).toInt()) return ""

                val trimmedSamples = FloatArray(length)
                System.arraycopy(samples, start, trimmedSamples, 0, length)

                val stream = recognizer.createStream()
                try {
                    stream.acceptWaveform(trimmedSamples, config.sampleRate)
                    recognizer.decode(stream)
                    val result = recognizer.getResult(stream)
                    val clean = cleanSenseVoiceText(result.text ?: "")
                    if (isSilenceHallucination(clean)) "" else formatTranscript(clean)
                } finally {
                    stream.release()
                }
            } else {
                val recognizer = onlineRecognizer ?: return null
                val stream = recognizer.createStream()
                try {
                    val chunkSize = 3200 // 200ms chunks @ 16kHz
                    var offset = 0
                    val fullResult = StringBuilder()
                    while (offset < samples.size) {
                        val end = (offset + chunkSize).coerceAtMost(samples.size)
                        val chunk = samples.copyOfRange(offset, end)
                        stream.acceptWaveform(chunk, config.sampleRate)
                        while (recognizer.isReady(stream)) {
                            recognizer.decode(stream)
                        }
                        if (recognizer.isEndpoint(stream)) {
                            val segmentText = formatTranscript(recognizer.getResult(stream).text.trim())
                            if (segmentText.isNotBlank()) {
                                if (fullResult.isNotEmpty()) fullResult.append(" ")
                                fullResult.append(segmentText)
                            }
                            recognizer.reset(stream)
                        }
                        offset = end
                    }
                    stream.inputFinished()
                    while (recognizer.isReady(stream)) {
                        recognizer.decode(stream)
                    }
                    val remaining = formatTranscript(recognizer.getResult(stream).text.trim())
                    if (remaining.isNotBlank()) {
                        if (fullResult.isNotEmpty()) fullResult.append(" ")
                        fullResult.append(remaining)
                    }
                    formatTranscript(fullResult.toString().trim())
                } finally {
                    stream.release()
                }
            }
        } catch (e: Throwable) {
            logger.error("transcribeWaveform error", e)
            null
        } finally {
            limiter.release()
        }
    }

    // --- Backward-compatibility methods for OnlineStream (e.g. tests) ---
    fun tryAcquireStream(): OnlineStream? {
        val recognizer = onlineRecognizer ?: return null
        if (!limiter.tryAcquire()) return null
        return try {
            recognizer.createStream()
        } catch (e: Throwable) {
            limiter.release()
            throw e
        }
    }

    fun releaseStream(stream: OnlineStream?) {
        if (stream == null) return
        try {
            stream.release()
        } catch (_: Throwable) {
        } finally {
            limiter.release()
        }
    }

    fun acceptWaveform(stream: OnlineStream, samples: FloatArray) {
        stream.acceptWaveform(samples, config.sampleRate)
    }

    fun inputFinished(stream: OnlineStream) {
        stream.inputFinished()
    }

    fun decodeIfReady(stream: OnlineStream): Int {
        val recognizer = onlineRecognizer ?: return 0
        var count = 0
        while (recognizer.isReady(stream)) {
            recognizer.decode(stream)
            count++
        }
        return count
    }

    fun isReady(stream: OnlineStream): Boolean = onlineRecognizer?.isReady(stream) == true

    fun decode(stream: OnlineStream) {
        onlineRecognizer?.decode(stream)
    }

    fun getText(stream: OnlineStream): String =
        onlineRecognizer?.let { formatTranscript(it.getResult(stream).text.trim()) } ?: ""

    fun getRawResult(stream: OnlineStream): com.k2fsa.sherpa.onnx.OnlineRecognizerResult =
        onlineRecognizer?.getResult(stream) ?: throw IllegalStateException("OnlineRecognizer is null")

    fun isEndpoint(stream: OnlineStream): Boolean =
        onlineRecognizer?.isEndpoint(stream) == true

    fun reset(stream: OnlineStream) {
        onlineRecognizer?.reset(stream)
    }

    private fun findSenseVoiceModel(dir: File): String? {
        val int8 = File(dir, "model.int8.onnx")
        if (int8.isFile && int8.length() > 50_000_000L) return int8.absolutePath
        val fp32 = File(dir, "model.onnx")
        if (fp32.isFile && fp32.length() > 50_000_000L) return fp32.absolutePath
        return null
    }

    private fun findModel(dir: File, prefix: String, vararg suffixes: String): String? {
        for (suffix in suffixes) {
            dir.listFiles()?.forEach { f ->
                val n = f.name.lowercase()
                if (n.startsWith(prefix) && n.endsWith(suffix)) {
                    if (suffix == ".onnx" && n.endsWith(".int8.onnx")) {
                        return@forEach
                    }
                    return f.absolutePath
                }
            }
        }
        return null
    }

    private fun findValidModelDir(): File? {
        val candidates = listOf(
            File(config.modelDir),
            File("models/${SttConfig.SENSE_VOICE_MODEL_NAME}"),
            File("../models/${SttConfig.SENSE_VOICE_MODEL_NAME}"),
            File("server/models/${SttConfig.SENSE_VOICE_MODEL_NAME}"),
            File(System.getProperty("user.home"), "models/${SttConfig.SENSE_VOICE_MODEL_NAME}"),
            File("/app/models/${SttConfig.SENSE_VOICE_MODEL_NAME}"),
            File("models/sherpa-onnx-streaming-zipformer-en-2023-06-21"),
            File("models/sherpa-onnx-streaming-zipformer-en-2023-06-26"),
            File("../models/sherpa-onnx-streaming-zipformer-en-2023-06-21"),
            File("../models/sherpa-onnx-streaming-zipformer-en-2023-06-26"),
            File("server/models/sherpa-onnx-streaming-zipformer-en-2023-06-21"),
            File("server/models/sherpa-onnx-streaming-zipformer-en-2023-06-26"),
            File(System.getProperty("user.home"), "models/sherpa-onnx-streaming-zipformer-en-2023-06-21"),
            File(System.getProperty("user.home"), "models/sherpa-onnx-streaming-zipformer-en-2023-06-26"),
            File("/app/models/sherpa-onnx-streaming-zipformer-en-2023-06-21"),
            File("/app/models/sherpa-onnx-streaming-zipformer-en-2023-06-26")
        )
        return candidates.firstOrNull { isValidModelDir(it) }
    }

    private fun isValidModelDir(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory) return false
        val tokens = File(dir, "tokens.txt")
        if (!tokens.isFile) return false

        // Check if SenseVoice
        if (findSenseVoiceModel(dir) != null) return true

        // Check if Zipformer
        val encoder = findModel(dir, "encoder", ".int8.onnx", ".onnx")
        val decoder = findModel(dir, "decoder", ".int8.onnx", ".onnx")
        val joiner = findModel(dir, "joiner", ".int8.onnx", ".onnx")
        return encoder != null && decoder != null && joiner != null
    }

    private fun downloadSenseVoiceModelIfMissing(targetDir: File): Boolean {
        return try {
            targetDir.mkdirs()
            val tokensFile = File(targetDir, "tokens.txt")
            val modelFile = File(targetDir, "model.int8.onnx")

            if (!tokensFile.exists() || tokensFile.length() < 100_000L) {
                logger.info("Downloading SenseVoice tokens.txt to ${tokensFile.absolutePath}...")
                downloadFile(
                    "https://huggingface.co/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17/resolve/main/tokens.txt",
                    tokensFile
                )
            }
            if (!modelFile.exists() || modelFile.length() < 200_000_000L) {
                logger.info("Downloading SenseVoice model.int8.onnx to ${modelFile.absolutePath}...")
                downloadFile(
                    "https://huggingface.co/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17/resolve/main/model.int8.onnx",
                    modelFile
                )
            }
            modelFile.exists() && tokensFile.exists()
        } catch (e: Exception) {
            logger.error("Failed to auto-download SenseVoice model: {}", e.message)
            false
        }
    }

    private fun downloadFile(urlStr: String, dest: File) {
        val temp = File(dest.parentFile, dest.name + ".tmp")
        val url = URI(urlStr).toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 30000
        connection.readTimeout = 60000
        connection.instanceFollowRedirects = true
        connection.inputStream.use { input ->
            temp.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (dest.exists()) dest.delete()
        temp.renameTo(dest)
    }

    companion object {
        fun cleanSenseVoiceText(raw: String): String {
            return raw.replace(Regex("<\\|.*?\\|>"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private val PHANTOM_SILENCE_WORDS = setOf(
            "i", "yeah", "yes", "okay", "ok", "uh", "um", "ah", "oh", "you", "the", "bye", "byebye"
        )

        /**
         * Checks whether the decoded SenseVoice text is an acoustic silence/noise hallucination.
         * SenseVoice often hallucinates "I.", "Yeah.", "Okay.", or repetitive sequences when fed
         * dead silence or background noise.
         */
        fun isSilenceHallucination(raw: String): Boolean {
            val clean = raw.trim()
            if (clean.isBlank()) return true

            val words = clean.lowercase()
                .replace(Regex("[^a-z0-9\\s]"), " ")
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }

            if (words.isEmpty()) return true

            // SenseVoice notoriously outputs "I." on complete silence
            if (words.size == 1 && words[0] == "i") {
                return true
            }

            // Repetitive filler phantom sequences like "I. Yeah. Yeah. Okay." or "Yeah. Okay."
            if (words.size >= 2 && words.all { it in PHANTOM_SILENCE_WORDS }) {
                return true
            }

            return false
        }

        /**
         * Scans waveform using short RMS energy windows (20ms) to detect active speech boundaries.
         * Returns null if no frames cross [speechThresholdRms], indicating pure silence or ambient noise.
         */
        fun findSpeechBounds(
            samples: FloatArray,
            sampleRate: Int = 16000,
            speechThresholdRms: Float = 0.010f
        ): Pair<Int, Int>? {
            if (samples.isEmpty()) return null
            val frameSize = (sampleRate * 0.02f).toInt() // 20ms frames
            var firstSpeechSample = -1
            var lastSpeechSample = -1

            var offset = 0
            while (offset < samples.size) {
                val len = minOf(frameSize, samples.size - offset)
                val rms = SenseVoiceStreamSession.computeRms(samples, offset, len)
                if (rms >= speechThresholdRms) {
                    if (firstSpeechSample < 0) {
                        firstSpeechSample = offset
                    }
                    lastSpeechSample = offset + len
                }
                offset += len
            }

            if (firstSpeechSample < 0 || lastSpeechSample <= firstSpeechSample) {
                return null
            }

            val leadIn = (sampleRate * 0.10f).toInt()
            val trailing = (sampleRate * 0.20f).toInt()
            val start = maxOf(0, firstSpeechSample - leadIn)
            val end = minOf(samples.size, lastSpeechSample + trailing)
            return Pair(start, end)
        }

        private val PROPER_NOUNS = setOf(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
            "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december",
            "english", "persian", "iran", "tehran", "london", "uk", "usa", "america", "american", "british",
            "french", "german", "spanish", "chinese", "japanese", "paris", "europe", "asia", "doctor", "mr", "mrs", "miss"
        )

        private val ALL_CAPS_ACRONYMS = setOf(
            "ok", "ai", "tv", "id", "pc", "cd", "dvd", "usb", "gps", "ceo", "vip", "stt", "tts"
        )

        fun formatTranscript(raw: String): String {
            if (raw.isBlank()) return raw
            val tokens = raw.trim().split(Regex("\\s+"))
            if (tokens.isEmpty()) return ""

            val result = StringBuilder()
            var capitalizeNext = true

            for (token in tokens) {
                if (token.isEmpty()) continue
                if (result.isNotEmpty()) result.append(" ")

                val wordOnly = token.trimEnd('.', ',', '!', '?', ';', ':')
                val trailingPunct = token.substring(wordOnly.length)
                val lowerWord = wordOnly.lowercase()

                val formattedWord = when {
                    ALL_CAPS_ACRONYMS.contains(lowerWord) -> lowerWord.uppercase()
                    lowerWord == "i" -> "I"
                    lowerWord.startsWith("i'") -> "I'" + lowerWord.substring(2)
                    capitalizeNext || PROPER_NOUNS.contains(lowerWord) -> {
                        lowerWord.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                    else -> lowerWord
                }

                result.append(formattedWord).append(trailingPunct)
                capitalizeNext = trailingPunct.any { it == '.' || it == '!' || it == '?' }
            }

            return result.toString()
        }
    }
}
