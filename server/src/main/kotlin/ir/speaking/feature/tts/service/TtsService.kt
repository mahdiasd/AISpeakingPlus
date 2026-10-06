package ir.speaking.feature.tts.service

import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsKokoroModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import ir.speaking.feature.tts.model.KokoroVoices
import ir.speaking.feature.tts.model.TtsResult
import ir.speaking.feature.tts.model.TtsVoiceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import java.io.File
import java.security.MessageDigest

@Single
class TtsService(
    private val config: TtsConfig
) {
    private val logger = LoggerFactory.getLogger(TtsService::class.java)
    private var offlineTts: OfflineTts? = null
    private val mutex = Mutex()
    private val cacheDirectory: File = File(config.cacheDir)

    val isAvailable: Boolean
        get() = offlineTts != null

    init {
        if (!config.enabled) {
            logger.info("Kokoro TTS service is disabled via TTS_ENABLED=false")
        } else {
            initializeEngine()
        }
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
        }
    }

    private fun initializeEngine() {
        try {
            val modelDir = ensureModelAvailable()
            if (modelDir == null) {
                logger.warn("Kokoro TTS model could not be found or downloaded. Fallback speech will be used.")
                return
            }

            val modelFile = File(modelDir, "model.onnx")
            val voicesFile = File(modelDir, "voices.bin")
            val tokensFile = File(modelDir, "tokens.txt")
            val dataDir = File(modelDir, "espeak-ng-data")

            val kokoroConfig = OfflineTtsKokoroModelConfig.builder()
                .setModel(modelFile.absolutePath)
                .setVoices(voicesFile.absolutePath)
                .setTokens(tokensFile.absolutePath)
                .setDataDir(dataDir.absolutePath)
                .setLengthScale(1.0f)
                .build()

            val modelConfig = OfflineTtsModelConfig.builder()
                .setKokoro(kokoroConfig)
                .setNumThreads(config.numThreads)
                .setDebug(false)
                .setProvider("cpu")
                .build()

            val ttsConfig = OfflineTtsConfig.builder()
                .setModel(modelConfig)
                .setMaxNumSentences(1)
                .setSilenceScale(0.2f)
                .build()

            offlineTts = OfflineTts(ttsConfig)
            logger.info("Kokoro-82M TTS initialized successfully from: ${modelDir.absolutePath} with sampleRate: ${offlineTts?.sampleRate}Hz, speakers: ${offlineTts?.numSpeakers}")
        } catch (e: Throwable) {
            logger.error("Failed to initialize Kokoro-82M TTS engine: ${e.message}", e)
            offlineTts = null
        }
    }

    private fun ensureModelAvailable(): File? {
        val existing = findValidModelDir()
        if (existing != null) {
            return existing
        }

        if (!config.autoDownload) {
            logger.warn("Kokoro TTS model files not found and TTS_AUTO_DOWNLOAD is disabled.")
            return null
        }

        val targetDir = File(config.modelDir)
        logger.info("Kokoro-82M TTS model missing. Starting automatic self-bootstrap download to: ${targetDir.absolutePath}")

        val downloaded = downloadAndExtractModel(targetDir, config.modelUrl)
        if (downloaded) {
            val verified = findValidModelDir()
            if (verified != null) {
                logger.info("Kokoro-82M TTS model downloaded and verified successfully at: ${verified.absolutePath}")
                return verified
            }
        }

        logger.error("Automatic download of Kokoro-82M TTS model failed or extraction was incomplete.")
        return null
    }

    private fun findValidModelDir(): File? {
        val candidates = listOf(
            File(config.modelDir),
            File("models/kokoro-en-v0_19"),
            File("server/models/kokoro-en-v0_19"),
            File("../models/kokoro-en-v0_19"),
            File(System.getProperty("user.home"), "models/kokoro-en-v0_19"),
            File("/app/models/kokoro-en-v0_19")
        )
        return candidates.firstOrNull { isValidModelDir(it) }
    }

    private fun isValidModelDir(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory) return false
        val modelFile = File(dir, "model.onnx")
        val voicesFile = File(dir, "voices.bin")
        val tokensFile = File(dir, "tokens.txt")
        val dataDir = File(dir, "espeak-ng-data")
        return modelFile.exists() && modelFile.length() > 50_000_000L &&
                voicesFile.exists() && tokensFile.exists() && dataDir.exists()
    }

    private fun downloadAndExtractModel(targetDir: File, urlString: String): Boolean {
        try {
            val parentDir = targetDir.parentFile ?: File(".")
            parentDir.mkdirs()
            val tempArchive = File(parentDir, "kokoro-en-v0_19.tar.bz2.download")

            logger.info("Connecting to model URL: $urlString")
            var currentUrl = java.net.URI(urlString).toURL()
            var connection = currentUrl.openConnection() as java.net.HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000
            connection.setRequestProperty("User-Agent", "AISpeakingPlus-TTS/1.0")

            var redirects = 0
            while (redirects < 5) {
                val status = connection.responseCode
                if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == java.net.HttpURLConnection.HTTP_MOVED_PERM ||
                    status == 307 || status == 308
                ) {
                    val newLocation = connection.getHeaderField("Location") ?: break
                    currentUrl = java.net.URI(newLocation).toURL()
                    connection.disconnect()
                    connection = currentUrl.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 30000
                    connection.readTimeout = 60000
                    connection.setRequestProperty("User-Agent", "AISpeakingPlus-TTS/1.0")
                    redirects++
                } else {
                    break
                }
            }

            val totalBytes = connection.contentLengthLong
            logger.info("Downloading Kokoro-82M archive (${if (totalBytes > 0) totalBytes / (1024 * 1024) else "?"} MB)...")

            connection.inputStream.use { input ->
                java.io.FileOutputStream(tempArchive).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastLogMb = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        val mb = totalRead / (1024 * 1024)
                        if (mb - lastLogMb >= 50) {
                            lastLogMb = mb
                            logger.info("Kokoro download progress: $mb MB...")
                        }
                    }
                }
            }

            logger.info("Archive downloaded (${tempArchive.length() / (1024 * 1024)} MB). Extracting...")
            val pb = ProcessBuilder("tar", "-xjf", tempArchive.absolutePath, "-C", parentDir.absolutePath)
                .redirectErrorStream(true)
            val proc = pb.start()
            val exitCode = proc.waitFor()
            tempArchive.delete()

            if (exitCode != 0) {
                logger.error("tar extraction failed with exit code $exitCode")
                return false
            }

            return true
        } catch (e: Exception) {
            logger.error("Failed to auto-download Kokoro TTS model: ${e.message}", e)
            return false
        }
    }

    fun getVoices(): List<TtsVoiceInfo> = KokoroVoices.ALL

    fun getVoiceInfo(voiceId: Int): TtsVoiceInfo = KokoroVoices.findById(voiceId)

    fun getAudioFile(filename: String): File? {
        val sanitized = File(filename).name // Prevent directory traversal
        val candidates = listOf(
            File(cacheDirectory, sanitized),
            File("audio_cache", sanitized),
            File("server/audio_cache", sanitized),
            File("../audio_cache", sanitized),
            File("/app/audio_cache", sanitized)
        )
        return candidates.firstOrNull { it.exists() && it.isFile }
    }

    suspend fun synthesize(
        text: String,
        sid: Int = config.defaultSid,
        speed: Float = config.defaultSpeed
    ): TtsResult? = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) {
            return@withContext null
        }

        val targetSid = if (sid in 0..10) sid else config.defaultSid
        val targetSpeed = speed.coerceIn(0.5f, 2.0f)
        val voiceInfo = getVoiceInfo(targetSid)

        // Cache filename by MD5 hash
        val hashInput = "$cleanText|$targetSid|$targetSpeed"
        val hash = md5(hashInput)
        val filename = "kokoro_${hash}.wav"
        val cachedFile = File(cacheDirectory, filename)
        val audioUrl = "/api/v1/tts/audio/$filename"

        if (cachedFile.exists() && cachedFile.length() > 44) {
            val sampleRate = offlineTts?.sampleRate ?: 24000
            val pcmBytes = cachedFile.length() - 44
            val durationMs = if (pcmBytes > 0) (pcmBytes / (sampleRate * 2.0) * 1000).toLong() else 0L
            return@withContext TtsResult(
                filename = filename,
                audioUrl = audioUrl,
                durationMs = durationMs,
                sampleRate = sampleRate,
                voiceId = targetSid,
                voiceName = voiceInfo.name
            )
        }

        val engine = offlineTts ?: run {
            logger.info("Kokoro TTS engine is not initialized; using fallback speech synthesis.")
            return@withContext fallbackSynthesize(cleanText, targetSid, targetSpeed, voiceInfo, filename, audioUrl, cachedFile)
        }

        try {
            val audio = mutex.withLock {
                engine.generate(cleanText, targetSid, targetSpeed)
            }

            val samples = audio.samples
            val sampleRate = audio.sampleRate
            val durationMs = if (sampleRate > 0) ((samples.size.toDouble() / sampleRate) * 1000).toLong() else 0L

            val wavBytes = WavUtils.samplesToWavBytes(samples, sampleRate)
            cachedFile.writeBytes(wavBytes)

            TtsResult(
                filename = filename,
                audioUrl = audioUrl,
                durationMs = durationMs,
                sampleRate = sampleRate,
                voiceId = targetSid,
                voiceName = voiceInfo.name,
                audioData = wavBytes
            )
        } catch (e: Throwable) {
            logger.error("Kokoro TTS synthesis error: ${e.message}", e)
            fallbackSynthesize(cleanText, targetSid, targetSpeed, voiceInfo, filename, audioUrl, cachedFile)
        }
    }

    private fun fallbackSynthesize(
        text: String,
        targetSid: Int,
        targetSpeed: Float,
        voiceInfo: TtsVoiceInfo,
        filename: String,
        audioUrl: String,
        cachedFile: File
    ): TtsResult? {
        val isMac = System.getProperty("os.name", "").lowercase().contains("mac")
        if (isMac) {
            try {
                val macVoice = when (targetSid) {
                    0 -> "Samantha"
                    1 -> "Flo"
                    2 -> "Samantha"
                    3 -> "Victoria"
                    4 -> "Flo"
                    5 -> "Eddy"
                    6 -> "Fred"
                    7 -> "Flo"
                    8 -> "Flo"
                    9 -> "Daniel"
                    10 -> "Daniel"
                    else -> "Samantha"
                }
                val rate = (175 * targetSpeed).toInt().coerceIn(100, 350)
                val tempWav = File.createTempFile("macos_say_", ".wav")
                try {
                    val process = ProcessBuilder(
                        "say",
                        "-v", macVoice,
                        "-r", rate.toString(),
                        text,
                        "-o", tempWav.absolutePath,
                        "--data-format=LEI16@24000"
                    ).start()
                    val finished = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)
                    if (finished && process.exitValue() == 0 && tempWav.exists() && tempWav.length() > 44) {
                        tempWav.copyTo(cachedFile, overwrite = true)
                        val pcmBytes = cachedFile.length() - 44
                        val durationMs = if (pcmBytes > 0) (pcmBytes / (24000.0 * 2.0) * 1000).toLong() else 1000L
                        val audioBytes = cachedFile.readBytes()
                        return TtsResult(
                            filename = filename,
                            audioUrl = audioUrl,
                            durationMs = durationMs,
                            sampleRate = 24000,
                            voiceId = targetSid,
                            voiceName = voiceInfo.name,
                            audioData = audioBytes
                        )
                    }
                } finally {
                    tempWav.delete()
                }
            } catch (e: Throwable) {
                logger.error("macOS fallback speech synthesis failed: ${e.message}", e)
            }
        }

        // Generic fallback: Generate harmonic speech-like WAV
        try {
            val sampleRate = 24000
            val durationSeconds = (text.length * 0.06 / targetSpeed).coerceIn(0.8, 12.0)
            val totalSamples = (sampleRate * durationSeconds).toInt()
            val samples = FloatArray(totalSamples)
            val baseFreq = when (voiceInfo.gender) {
                ir.speaking.feature.tts.model.VoiceGender.FEMALE -> 240.0
                ir.speaking.feature.tts.model.VoiceGender.MALE -> 135.0
            }
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = Math.sin(Math.PI * (i.toDouble() / totalSamples))
                val wave = (Math.sin(2.0 * Math.PI * baseFreq * t) * 0.5 +
                            Math.sin(2.0 * Math.PI * baseFreq * 2.0 * t) * 0.25 +
                            Math.sin(2.0 * Math.PI * baseFreq * 3.0 * t) * 0.125) * envelope
                samples[i] = wave.toFloat().coerceIn(-1.0f, 1.0f)
            }
            val wavBytes = WavUtils.samplesToWavBytes(samples, sampleRate)
            cachedFile.writeBytes(wavBytes)
            return TtsResult(
                filename = filename,
                audioUrl = audioUrl,
                durationMs = (durationSeconds * 1000).toLong(),
                sampleRate = sampleRate,
                voiceId = targetSid,
                voiceName = voiceInfo.name,
                audioData = wavBytes
            )
        } catch (e: Throwable) {
            logger.error("Tone fallback failed: ${e.message}", e)
            return null
        }
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
