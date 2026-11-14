package ir.speaking.feature.stt.service

import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineStream
import kotlin.math.sqrt

/**
 * Unified abstraction for an active STT streaming session.
 * Hides whether the underlying engine is an offline non-autoregressive model (like SenseVoice)
 * or an online streaming transducer (like Zipformer).
 */
interface SttStreamSession {
    fun acceptWaveform(samples: FloatArray)
    fun decodeIfReady(): Int
    fun isReady(): Boolean
    fun isEndpoint(): Boolean
    fun getText(): String
    fun inputFinished()
    fun reset()
    fun release()
}

/**
 * High-performance streaming session adapter for SenseVoice.
 *
 * Buffers audio chunks, runs fast interim decodes on CPU (~50-100ms),
 * performs trailing silence energy checks for endpointing, and emits
 * clean transcripts without raw metadata tags.
 */
class SenseVoiceStreamSession(
    private val recognizer: OfflineRecognizer,
    private val sampleRate: Int = 16000,
    private val minTrailingSilenceSamples: Int = 24000, // 1.5s @ 16kHz
    private val silenceThresholdRms: Float = 0.012f
) : SttStreamSession {

    private val audioBuffer = PrimitiveFloatBuffer(32768)
    private var lastDecodedSampleCount = 0
    private var lastTranscript = ""
    private var isFinished = false
    private var trailingSilenceSampleCount = 0

    @Synchronized
    override fun acceptWaveform(samples: FloatArray) {
        if (isFinished || samples.isEmpty()) return
        audioBuffer.addAll(samples)

        val chunkRms = computeRms(samples)
        if (chunkRms < silenceThresholdRms) {
            trailingSilenceSampleCount += samples.size
        } else {
            trailingSilenceSampleCount = 0
        }
    }

    private fun computeRms(samples: FloatArray): Float {
        if (samples.isEmpty()) return 0f
        var sumSquares = 0.0
        for (s in samples) {
            sumSquares += s * s
        }
        return sqrt(sumSquares / samples.size).toFloat()
    }

    @Synchronized
    override fun decodeIfReady(): Int {
        val currentSize = audioBuffer.size
        val newSamples = currentSize - lastDecodedSampleCount
        val minSamplesForFirstDecode = (sampleRate * 0.5f).toInt() // 500ms
        val minNewSamples = (sampleRate * 0.35f).toInt()           // 350ms between decodes

        if (currentSize >= minSamplesForFirstDecode && newSamples >= minNewSamples) {
            decodeInternal()
            return 1
        }
        return 0
    }

    @Synchronized
    private fun decodeInternal() {
        if (audioBuffer.size == 0) return
        val samplesArray = audioBuffer.toArray()
        val stream = recognizer.createStream()
        try {
            stream.acceptWaveform(samplesArray, sampleRate)
            recognizer.decode(stream)
            val result = recognizer.getResult(stream)
            val raw = result.text ?: ""
            val clean = SttService.cleanSenseVoiceText(raw)
            if (clean.isNotBlank()) {
                lastTranscript = SttService.formatTranscript(clean)
            }
            lastDecodedSampleCount = audioBuffer.size
        } finally {
            stream.release()
        }
    }

    override fun isReady(): Boolean = false

    @Synchronized
    override fun isEndpoint(): Boolean {
        // Trigger endpoint only if we have at least 1s of accumulated audio and trailing silence exceeds threshold
        return audioBuffer.size >= sampleRate && trailingSilenceSampleCount >= minTrailingSilenceSamples
    }

    @Synchronized
    override fun getText(): String = lastTranscript

    @Synchronized
    override fun inputFinished() {
        if (isFinished) return
        isFinished = true
        decodeInternal()
    }

    @Synchronized
    override fun reset() {
        audioBuffer.clear()
        lastDecodedSampleCount = 0
        lastTranscript = ""
        isFinished = false
        trailingSilenceSampleCount = 0
    }

    override fun release() {
        // In offline mode, per-session native stream pointers are ephemeral and released in decodeInternal
    }
}

/**
 * Adapter for OnlineTransducer (Zipformer) models using [OnlineRecognizer] and [OnlineStream].
 */
class ZipformerStreamSession(
    private val recognizer: OnlineRecognizer,
    private val stream: OnlineStream,
    private val sampleRate: Int
) : SttStreamSession {

    override fun acceptWaveform(samples: FloatArray) {
        stream.acceptWaveform(samples, sampleRate)
    }

    override fun decodeIfReady(): Int {
        var count = 0
        while (recognizer.isReady(stream)) {
            recognizer.decode(stream)
            count++
        }
        return count
    }

    override fun isReady(): Boolean = recognizer.isReady(stream)

    override fun isEndpoint(): Boolean = recognizer.isEndpoint(stream)

    override fun getText(): String =
        SttService.formatTranscript(recognizer.getResult(stream).text.trim())

    override fun inputFinished() {
        stream.inputFinished()
    }

    override fun reset() {
        recognizer.reset(stream)
    }

    override fun release() {
        try {
            stream.release()
        } catch (_: Throwable) {}
    }
}

/**
 * Fast, unboxed float buffer avoiding object allocations during real-time streaming.
 */
class PrimitiveFloatBuffer(initialCapacity: Int = 32768) {
    private var data = FloatArray(initialCapacity)
    var size: Int = 0
        private set

    fun addAll(samples: FloatArray) {
        ensureCapacity(size + samples.size)
        System.arraycopy(samples, 0, data, size, samples.size)
        size += samples.size
    }

    fun toArray(): FloatArray {
        return data.copyOf(size)
    }

    fun clear() {
        size = 0
    }

    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > data.size) {
            val newCapacity = maxOf(data.size * 2, minCapacity)
            data = data.copyOf(newCapacity)
        }
    }
}
