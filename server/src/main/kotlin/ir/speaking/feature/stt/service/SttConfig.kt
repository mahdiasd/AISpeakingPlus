package ir.speaking.feature.stt.service

import org.koin.core.annotation.Single

/**
 * Configuration for the streaming STT service, resolved at startup from
 * environment variables (with sane fallbacks). Kept as a plain data class so
 * it can be injected into [SttService] and overridden in tests.
 *
 * Environment variables (prefix `STT_`):
 *
 *  - STT_MODEL_DIR           Directory containing the transducer onnx files + tokens.txt
 *  - STT_MAX_STREAMS         Max concurrent WebSocket sessions (default 2)
 *  - STT_NUM_THREADS         Inference threads (default 1)
 *  - STT_SAMPLE_RATE         Audio sample rate in Hz (default 16000)
 *  - STT_RULE1_TRAILING      rule1 min trailing silence in seconds (default 2.4)
 *  - STT_RULE2_TRAILING      rule2 min trailing silence in seconds (default 1.4)
 *  - STT_RULE3_UTTERANCE     rule3 max utterance length in seconds (default 20.0)
 */
data class SttConfig(
    val modelDir: String,
    val maxConcurrentStreams: Int,
    val numThreads: Int,
    val sampleRate: Int,
    val rule1MustContainNonSilence: Boolean,
    val rule1MinTrailingSilence: Float,
    val rule1MinUtteranceLength: Float,
    val rule2MustContainNonSilence: Boolean,
    val rule2MinTrailingSilence: Float,
    val rule2MinUtteranceLength: Float,
    val rule3MustContainNonSilence: Boolean,
    val rule3MinTrailingSilence: Float,
    val rule3MinUtteranceLength: Float,
    val language: String = "en",
    val useItn: Boolean = true,
    val autoDownload: Boolean = true,
) {
    companion object {
        const val SENSE_VOICE_MODEL_NAME = "sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17"

        /**
         * Build a [SttConfig] from environment variables, falling back to the
         * provided defaults when a variable is missing or invalid.
         */
        fun fromEnv(): SttConfig {
            fun envBool(name: String, default: Boolean): Boolean =
                System.getenv(name)?.toBooleanStrictOrNull() ?: default

            fun envInt(name: String, default: Int): Int =
                System.getenv(name)?.toIntOrNull() ?: default

            fun envFloat(name: String, default: Float): Float =
                System.getenv(name)?.toFloatOrNull() ?: default

            fun envString(name: String, default: String): String =
                System.getenv(name)?.takeIf { it.isNotBlank() } ?: default

            val candidateModelDirs = listOfNotNull(
                System.getenv("STT_MODEL_DIR"),
                java.io.File("models/$SENSE_VOICE_MODEL_NAME").takeIf { it.exists() }?.absolutePath,
                java.io.File("../models/$SENSE_VOICE_MODEL_NAME").takeIf { it.exists() }?.absolutePath,
                java.io.File("server/models/$SENSE_VOICE_MODEL_NAME").takeIf { it.exists() }?.absolutePath,
                java.io.File(System.getProperty("user.home"), "models/$SENSE_VOICE_MODEL_NAME").takeIf { it.exists() }?.absolutePath,
                "/app/models/$SENSE_VOICE_MODEL_NAME",
                java.io.File("models/sherpa-onnx-streaming-zipformer-en-2023-06-21").takeIf { it.exists() }?.absolutePath,
                java.io.File("models/sherpa-onnx-streaming-zipformer-en-2023-06-26").takeIf { it.exists() }?.absolutePath,
                java.io.File("../models/sherpa-onnx-streaming-zipformer-en-2023-06-21").takeIf { it.exists() }?.absolutePath,
                java.io.File("../models/sherpa-onnx-streaming-zipformer-en-2023-06-26").takeIf { it.exists() }?.absolutePath,
                java.io.File("server/models/sherpa-onnx-streaming-zipformer-en-2023-06-21").takeIf { it.exists() }?.absolutePath,
                java.io.File("server/models/sherpa-onnx-streaming-zipformer-en-2023-06-26").takeIf { it.exists() }?.absolutePath,
                java.io.File(System.getProperty("user.home"), "models/sherpa-onnx-streaming-zipformer-en-2023-06-21").takeIf { it.exists() }?.absolutePath,
                java.io.File(System.getProperty("user.home"), "models/sherpa-onnx-streaming-zipformer-en-2023-06-26").takeIf { it.exists() }?.absolutePath,
                "/app/models/sherpa-onnx-streaming-zipformer-en-2023-06-21",
                "/app/models/sherpa-onnx-streaming-zipformer-en-2023-06-26"
            )
            val resolvedModelDir = candidateModelDirs.firstOrNull { java.io.File(it).isDirectory }
                ?: candidateModelDirs.firstOrNull()
                ?: "models/$SENSE_VOICE_MODEL_NAME"

            return SttConfig(
                modelDir = resolvedModelDir,
                maxConcurrentStreams = envInt("STT_MAX_STREAMS", 6),
                numThreads = envInt("STT_NUM_THREADS", 2),
                sampleRate = envInt("STT_SAMPLE_RATE", 16000),
                rule1MustContainNonSilence = envBool("STT_RULE1_NON_SILENCE", true),
                rule1MinTrailingSilence = envFloat("STT_RULE1_TRAILING", 3.0f),
                rule1MinUtteranceLength = envFloat("STT_RULE1_UTTERANCE", 0.0f),
                rule2MustContainNonSilence = envBool("STT_RULE2_NON_SILENCE", true),
                rule2MinTrailingSilence = envFloat("STT_RULE2_TRAILING", 2.0f),
                rule2MinUtteranceLength = envFloat("STT_RULE2_UTTERANCE", 0.0f),
                rule3MustContainNonSilence = envBool("STT_RULE3_NON_SILENCE", false),
                rule3MinTrailingSilence = envFloat("STT_RULE3_TRAILING", 0.0f),
                rule3MinUtteranceLength = envFloat("STT_RULE3_UTTERANCE", 30.0f),
                language = envString("STT_LANGUAGE", "en"),
                useItn = envBool("STT_USE_ITN", true),
                autoDownload = envBool("STT_AUTO_DOWNLOAD", true)
            )
        }
    }
}

/**
 * Koin provider for [SttConfig]. Read from environment variables so the
 * exact same config can be used in both the production server and tests.
 */
@Single
fun provideSttConfig(): SttConfig = SttConfig.fromEnv()
