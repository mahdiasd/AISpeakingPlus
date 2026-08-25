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
    private val recognizer: OnlineRecognizer
    private val limiter: StreamConcurrencyLimiter =
        StreamConcurrencyLimiter(config.maxConcurrentStreams)

    val sampleRate: Int get() = config.sampleRate
    val maxConcurrentStreams: Int get() = limiter.maxConcurrent
    val activeStreams: Int get() = limiter.active

    init {
        val modelDir = File(config.modelDir)
        require(modelDir.isDirectory) {
            "STT model directory not found: ${config.modelDir}. " +
                "Download the streaming-zipformer-en-2023-06-21 (int8) model first."
        }

        // Locate the transducer model files. The naming convention for the
        // 2023-06-21 release is:
        //   encoder-epoch-99-avg-1.int8.onnx
        //   decoder-epoch-99-avg-1.onnx
        //   joiner-epoch-99-avg-1.int8.onnx
        //   tokens.txt
        // We resolve them dynamically so the service also works if a newer
        // model with a different epoch/avg suffix is dropped in.
        val encoder = findModel(modelDir, "encoder", ".int8.onnx", ".onnx")
            ?: throw IllegalArgumentException("encoder .onnx not found in ${config.modelDir}")
        val decoder = findModel(modelDir, "decoder", ".onnx")
            ?: throw IllegalArgumentException("decoder .onnx not found in ${config.modelDir}")
        val joiner = findModel(modelDir, "joiner", ".int8.onnx", ".onnx")
            ?: throw IllegalArgumentException("joiner .onnx not found in ${config.modelDir}")
        val tokens = File(modelDir, "tokens.txt")
        require(tokens.isFile) { "tokens.txt not found in ${config.modelDir}" }

        val transducer = OnlineTransducerModelConfig.builder()
            .setEncoder(encoder)
            .setDecoder(decoder)
            .setJoiner(joiner)
            .build()

        val modelConfig = OnlineModelConfig.builder()
            .setTransducer(transducer)
            .setTokens(tokens.absolutePath)
            .setNumThreads(config.numThreads)
            .setDebug(false)
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

    /** Runs a decode step if the stream has enough data. */
    fun decodeIfReady(stream: OnlineStream) {
        if (recognizer.isReady(stream)) {
            recognizer.decode(stream)
        }
    }

    /** Returns the current partial/interim transcription text. */
    fun getText(stream: OnlineStream): String =
        recognizer.getResult(stream).text.trim()

    /** Returns true when the endpoint detector fires (utterance boundary). */
    fun isEndpoint(stream: OnlineStream): Boolean =
        recognizer.isEndpoint(stream)

    /** Resets the internal state of the stream to start a new utterance. */
    fun reset(stream: OnlineStream) {
        recognizer.reset(stream)
    }

    private fun findModel(dir: File, prefix: String, vararg suffixes: String): String? {
        // Try exact prefixes first (e.g. "encoder") with the given suffixes,
        // preferring the first suffix (int8).
        for (suffix in suffixes) {
            dir.listFiles()?.forEach { f ->
                val n = f.name.lowercase()
                if (n.startsWith(prefix) && n.endsWith(suffix)) {
                    return f.absolutePath
                }
            }
        }
        return null
    }
}
