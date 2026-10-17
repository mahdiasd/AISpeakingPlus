package ir.speaking.feature.stt.service

import com.k2fsa.sherpa.onnx.EndpointConfig
import com.k2fsa.sherpa.onnx.EndpointRule
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import org.koin.core.annotation.Single
import java.io.File

/**
 * Singleton service that owns the single shared [OnlineRecognizer] instance
 * and enforces a hard cap on the number of concurrent [OnlineStream]s via
 * a [StreamConcurrencyLimiter] (backed by a fair [Semaphore]).
 *
 * The native model is loaded exactly once when this class is instantiated by
 * Koin (at application startup) and kept in memory for the lifetime of the
 * process. Each WebSocket session calls [tryAcquireStream] to obtain a
 * dedicated [OnlineStream]; when the session ends (normally or abruptly)
 * it must call [releaseStream] so the native resources are freed and the
 * semaphore permit is returned.
 */
@Single
class SttService(
    private val config: SttConfig
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(SttService::class.java)
    private val recognizer: OnlineRecognizer
    private val limiter: StreamConcurrencyLimiter =
        StreamConcurrencyLimiter(config.maxConcurrentStreams)

    val sampleRate: Int get() = config.sampleRate
    val maxConcurrentStreams: Int get() = limiter.maxConcurrent
    val activeStreams: Int get() = limiter.active

    init {
        val modelDir = findValidModelDir()
            ?: throw IllegalArgumentException(
                "STT model directory not found or incomplete: ${config.modelDir}. " +
                "Ensure encoder, decoder, joiner (.onnx/.int8.onnx) and tokens.txt are present."
            )
        logger.info("Using STT model directory: {}", modelDir.absolutePath)

        // Locate the transducer model files. The naming convention for the
        // 2023-06-21 release is:
        //   encoder-epoch-99-avg-1.int8.onnx
        //   decoder-epoch-99-avg-1.onnx
        //   joiner-epoch-99-avg-1.int8.onnx
        //   tokens.txt
        // We resolve them dynamically so the service also works if a newer
        // model with a different epoch/avg suffix is dropped in.
        val encoder = findModel(modelDir, "encoder", ".int8.onnx", ".onnx")
            ?: throw IllegalArgumentException("encoder .onnx not found in ${modelDir.absolutePath}")
        val decoder = findModel(modelDir, "decoder", ".onnx", ".int8.onnx")
            ?: throw IllegalArgumentException("decoder .onnx not found in ${modelDir.absolutePath}")
        val joiner = findModel(modelDir, "joiner", ".int8.onnx", ".onnx")
            ?: throw IllegalArgumentException("joiner .onnx not found in ${modelDir.absolutePath}")
        val tokens = File(modelDir, "tokens.txt")
        require(tokens.isFile) { "tokens.txt not found in ${modelDir.absolutePath}" }

        logger.info("Resolved STT models:\n  encoder={}\n  decoder={}\n  joiner={}\n  tokens={}",
            encoder, decoder, joiner, tokens.absolutePath)

        val transducer = OnlineTransducerModelConfig.builder()
            .setEncoder(encoder)
            .setDecoder(decoder)
            .setJoiner(joiner)
            .build()

        val modelConfig = OnlineModelConfig.builder()
            .setTransducer(transducer)
            .setTokens(tokens.absolutePath)
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

        recognizer = OnlineRecognizer(recognizerConfig)
    }

    /**
     * Attempts to acquire a semaphore permit and create a new [OnlineStream].
     * Returns `null` if the server is at capacity (all permits taken); the
     * caller should then send a `server_busy` error and close the WebSocket.
     */
    fun tryAcquireStream(): OnlineStream? {
        if (!limiter.tryAcquire()) {
            return null
        }
        return try {
            val stream = recognizer.createStream()
            stream
        } catch (e: Throwable) {
            limiter.release()
            throw e
        }
    }

    /**
     * Releases a previously acquired [OnlineStream]: frees the native
     * resources and returns the semaphore permit. Safe to call on a null
     * stream (no-op).
     */
    fun releaseStream(stream: OnlineStream?) {
        if (stream == null) return
        try {
            stream.release()
        } catch (_: Throwable) {
            // Best-effort cleanup; the native pointer may already be invalid.
        } finally {
            limiter.release()
        }
    }

    /** Feeds PCM float samples into the stream. */
    fun acceptWaveform(stream: OnlineStream, samples: FloatArray) {
        stream.acceptWaveform(samples, config.sampleRate)
    }

    /** Signals the stream that no more audio will arrive. */
    fun inputFinished(stream: OnlineStream) {
        stream.inputFinished()
    }

    /** Runs decode steps while the stream has enough data ready. */
    fun decodeIfReady(stream: OnlineStream): Int {
        var count = 0
        while (recognizer.isReady(stream)) {
            recognizer.decode(stream)
            count++
        }
        return count
    }

    fun isReady(stream: OnlineStream): Boolean = recognizer.isReady(stream)

    fun decode(stream: OnlineStream) {
        recognizer.decode(stream)
    }

    /** Transcribes a complete audio waveform (samples at 16kHz) and returns the recognized text. */
    fun transcribeWaveform(samples: FloatArray): String? {
        val stream = tryAcquireStream() ?: return null
        return try {
            val chunkSize = 3200 // 200ms chunks @ 16kHz
            var offset = 0
            val fullResult = StringBuilder()
            var chunkIndex = 0
            while (offset < samples.size) {
                val end = (offset + chunkSize).coerceAtMost(samples.size)
                val chunk = samples.copyOfRange(offset, end)
                acceptWaveform(stream, chunk)
                val decodes = decodeIfReady(stream)
                val raw = recognizer.getResult(stream)
                val currentT = raw.text?.trim() ?: ""
                val endp = isEndpoint(stream)
                logger.debug("transcribeWaveform chunk {}: decodes={}, text='{}', isEndpoint={}", chunkIndex, decodes, currentT, endp)
                if (endp) {
                    val segmentText = getText(stream)
                    if (segmentText.isNotBlank()) {
                        if (fullResult.isNotEmpty()) fullResult.append(" ")
                        fullResult.append(segmentText)
                    }
                    reset(stream)
                }
                offset = end
                chunkIndex++
            }
            inputFinished(stream)
            decodeIfReady(stream)
            val remaining = getText(stream)
            logger.debug("transcribeWaveform inputFinished: remaining='{}'", remaining)
            if (remaining.isNotBlank()) {
                if (fullResult.isNotEmpty()) fullResult.append(" ")
                fullResult.append(remaining)
            }
            formatTranscript(fullResult.toString().trim())
        } catch (e: Throwable) {
            logger.error("transcribeWaveform error", e)
            null
        } finally {
            releaseStream(stream)
        }
    }

    /** Returns the current partial/interim transcription text formatted with natural sentence casing. */
    fun getText(stream: OnlineStream): String =
        formatTranscript(recognizer.getResult(stream).text.trim())

    fun getRawResult(stream: OnlineStream): com.k2fsa.sherpa.onnx.OnlineRecognizerResult =
        recognizer.getResult(stream)

    /** Returns true when the endpoint detector fires (utterance boundary). */
    fun isEndpoint(stream: OnlineStream): Boolean =
        recognizer.isEndpoint(stream)

    /** Resets the internal state of the stream to start a new utterance. */
    fun reset(stream: OnlineStream) {
        recognizer.reset(stream)
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
        val encoder = findModel(dir, "encoder", ".int8.onnx", ".onnx")
        val decoder = findModel(dir, "decoder", ".int8.onnx", ".onnx")
        val joiner = findModel(dir, "joiner", ".int8.onnx", ".onnx")
        val tokens = File(dir, "tokens.txt")
        return encoder != null && decoder != null && joiner != null && tokens.isFile
    }

    companion object {
        private val PROPER_NOUNS = setOf(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
            "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december",
            "english", "persian", "iran", "tehran", "london", "uk", "usa", "america", "american", "british",
            "french", "german", "spanish", "chinese", "japanese", "paris", "europe", "asia", "doctor", "mr", "mrs", "miss"
        )

        private val ALL_CAPS_ACRONYMS = setOf(
            "ok", "ai", "tv", "id", "pc", "cd", "dvd", "usb", "gps", "ceo", "vip", "stt", "tts"
        )

        /**
         * Converts all-caps raw ASR transcript output into natural English sentence casing,
         * capitalizing the first word of sentences, pronoun 'I', contractions ('I'm', etc.),
         * proper nouns, and recognized acronyms.
         */
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
