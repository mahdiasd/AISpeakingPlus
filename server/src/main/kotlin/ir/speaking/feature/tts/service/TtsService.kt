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
            val modelDir = File(config.modelDir)
            if (!modelDir.exists() || !modelDir.isDirectory) {
                logger.warn("Kokoro TTS model directory not found at: ${config.modelDir}. TTS will be unavailable until model is downloaded.")
                return
            }

            val modelFile = File(modelDir, "model.onnx")
            val voicesFile = File(modelDir, "voices.bin")
            val tokensFile = File(modelDir, "tokens.txt")
            val dataDir = File(modelDir, "espeak-ng-data")

            if (!modelFile.exists() || !voicesFile.exists() || !tokensFile.exists() || !dataDir.exists()) {
                logger.warn("Incomplete Kokoro TTS model files in ${config.modelDir}. Expected model.onnx, voices.bin, tokens.txt, and espeak-ng-data/.")
                return
            }

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
            logger.info("Kokoro-82M TTS initialized successfully with sampleRate: ${offlineTts?.sampleRate}Hz, speakers: ${offlineTts?.numSpeakers}")
        } catch (e: Throwable) {
            logger.error("Failed to initialize Kokoro-82M TTS engine: ${e.message}", e)
            offlineTts = null
        }
    }

    fun getVoices(): List<TtsVoiceInfo> = KokoroVoices.ALL

    fun getVoiceInfo(voiceId: Int): TtsVoiceInfo = KokoroVoices.findById(voiceId)

    fun getAudioFile(filename: String): File? {
        val sanitized = File(filename).name // Prevent directory traversal
        val file = File(cacheDirectory, sanitized)
        return if (file.exists() && file.isFile) file else null
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
            logger.warn("Kokoro TTS engine is not available.")
            return@withContext null
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
            null
        }
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
