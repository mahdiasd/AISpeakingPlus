package ir.speaking.feature.tts.service

import org.koin.core.annotation.Single

/**
 * Configuration for the offline TTS (Kokoro-82M) service.
 *
 * Environment variables:
 *  - TTS_ENABLED           Whether TTS is enabled (default true)
 *  - TTS_MODEL_DIR         Directory containing Kokoro model (model.onnx, voices.bin, tokens.txt, espeak-ng-data)
 *  - TTS_NUM_THREADS       Number of inference threads (default 2)
 *  - TTS_DEFAULT_SID       Default speaker ID (default 0)
 *  - TTS_DEFAULT_SPEED     Default speaking speed (default 1.0)
 *  - TTS_CACHE_DIR         Directory where generated audio files are stored (default ./audio_cache)
 */
data class TtsConfig(
    val enabled: Boolean,
    val modelDir: String,
    val numThreads: Int,
    val defaultSid: Int,
    val defaultSpeed: Float,
    val cacheDir: String
) {
    companion object {
        fun fromEnv(): TtsConfig {
            fun envBool(name: String, default: Boolean): Boolean =
                System.getenv(name)?.toBooleanStrictOrNull() ?: default

            fun envInt(name: String, default: Int): Int =
                System.getenv(name)?.toIntOrNull() ?: default

            fun envFloat(name: String, default: Float): Float =
                System.getenv(name)?.toFloatOrNull() ?: default

            return TtsConfig(
                enabled = envBool("TTS_ENABLED", true),
                modelDir = System.getenv("TTS_MODEL_DIR")
                    ?: "/app/models/kokoro-en-v0_19",
                numThreads = envInt("TTS_NUM_THREADS", 2),
                defaultSid = envInt("TTS_DEFAULT_SID", 0),
                defaultSpeed = envFloat("TTS_DEFAULT_SPEED", 1.0f),
                cacheDir = System.getenv("TTS_CACHE_DIR")
                    ?: "/app/audio_cache"
            )
        }
    }
}

@Single
fun provideTtsConfig(): TtsConfig = TtsConfig.fromEnv()
