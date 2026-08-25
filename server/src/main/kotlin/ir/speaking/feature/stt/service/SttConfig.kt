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
) {
    companion object {
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

            return SttConfig(
                modelDir = System.getenv("STT_MODEL_DIR")
                    ?: "/app/models/sherpa-onnx-streaming-zipformer-en-2023-06-21",
                maxConcurrentStreams = envInt("STT_MAX_STREAMS", 2),
                numThreads = envInt("STT_NUM_THREADS", 1),
                sampleRate = envInt("STT_SAMPLE_RATE", 16000),
                rule1MustContainNonSilence = envBool("STT_RULE1_NON_SILENCE", false),
                rule1MinTrailingSilence = envFloat("STT_RULE1_TRAILING", 2.4f),
                rule1MinUtteranceLength = envFloat("STT_RULE1_UTTERANCE", 0.0f),
                rule2MustContainNonSilence = envBool("STT_RULE2_NON_SILENCE", true),
                rule2MinTrailingSilence = envFloat("STT_RULE2_TRAILING", 1.4f),
                rule2MinUtteranceLength = envFloat("STT_RULE2_UTTERANCE", 0.0f),
                rule3MustContainNonSilence = envBool("STT_RULE3_NON_SILENCE", false),
                rule3MinTrailingSilence = envFloat("STT_RULE3_TRAILING", 0.0f),
                rule3MinUtteranceLength = envFloat("STT_RULE3_UTTERANCE", 20.0f),
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
