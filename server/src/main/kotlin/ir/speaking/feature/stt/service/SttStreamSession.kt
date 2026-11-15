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
 * Buffers audio chunks, detects speech activity (VAD) via RMS energy,
 * trims trailing dead silence to prevent phantom hallucination words ("I. Yeah. Okay."),
 * and emits clean transcripts.
 */
class SenseVoiceStreamSession(
    private val recognizer: OfflineRecognizer,
    private val sampleRate: Int = 16000,
    private val minTrailingSilenceSamples: Int = 24000, // 1.5s @ 16kHz
    private val speechThresholdRms: Float = 0.010f
) : SttStreamSession {

    private val audioBuffer = PrimitiveFloatBuffer(32768)
    private var lastDecodedSampleCount = 0
    private var lastTranscript = ""
    private var isFinished = false
    private var trailingSilenceSampleCount = 0
    private var firstSpeechSampleIndex = -1
    private var lastSpeechSampleIndex = -1

    @Synchronized
    override fun acceptWaveform(samples: FloatArray) {
        if (isFinished || samples.isEmpty()) return
        val startOffset = audioBuffer.size
        audioBuffer.addAll(samples)

        val chunkRms = computeRms(samples, 0, samples.size)
        if (chunkRms >= speechThresholdRms) {
            if (firstSpeechSampleIndex < 0) {
                // Include 100ms of lead-in context before the first speech onset
                firstSpeechSampleIndex = maxOf(0, startOffset - (sampleRate * 0.1f).toInt())
            }
            lastSpeechSampleIndex = audioBuffer.size
            trailingSilenceSampleCount = 0
        } else {
            trailingSilenceSampleCount += samples.size
        }
    }

    @Synchronized
    override fun decodeIfReady(): Int {
        // If no genuine speech has been detected yet, do not waste inference on silence
        if (firstSpeechSampleIndex < 0 || lastSpeechSampleIndex < 0) {
            return 0
        }

        val speechLength = lastSpeechSampleIndex - firstSpeechSampleIndex
        val minSpeechSamples = (sampleRate * 0.35f).toInt() // At least 350ms of speech
        val newSamples = audioBuffer.size - lastDecodedSampleCount
        val minNewSamples = (sampleRate * 0.30f).toInt()

        if (speechLength >= minSpeechSamples && newSamples >= minNewSamples) {
            decodeInternal()
            return 1
        }
        return 0
    }

    @Synchronized
    private fun decodeInternal() {
        if (firstSpeechSampleIndex < 0 || lastSpeechSampleIndex <= firstSpeechSampleIndex) {
            return
        }

        // Strictly trim trailing silence! Include at most 200ms comfort padding after speech ends.
        val speechEnd = minOf(audioBuffer.size, lastSpeechSampleIndex + (sampleRate * 0.20f).toInt())
        val speechStart = maxOf(0, firstSpeechSampleIndex)
        val length = speechEnd - speechStart
        if (length < (sampleRate * 0.1f).toInt()) return

        val trimmedSamples = FloatArray(length)
        System.arraycopy(audioBuffer.dataRef, speechStart, trimmedSamples, 0, length)

        val stream = recognizer.createStream()
        try {
            stream.acceptWaveform(trimmedSamples, sampleRate)
            recognizer.decode(stream)
            val result = recognizer.getResult(stream)
            val raw = result.text ?: ""
            val clean = SttService.cleanSenseVoiceText(raw)
            if (clean.isNotBlank() && !SttService.isSilenceHallucination(clean)) {
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
        // Trigger endpoint only if real speech occurred and trailing silence exceeds threshold
        return firstSpeechSampleIndex >= 0 &&
                (lastSpeechSampleIndex - firstSpeechSampleIndex) >= (sampleRate * 0.8f).toInt() &&
                trailingSilenceSampleCount >= minTrailingSilenceSamples
    }

    @Synchronized
    override fun getText(): String = lastTranscript

    @Synchronized
    override fun inputFinished() {
        if (isFinished) return
        isFinished = true
        if (firstSpeechSampleIndex >= 0 && lastSpeechSampleIndex > firstSpeechSampleIndex) {
            decodeInternal()
        }
    }

    @Synchronized
    override fun reset() {
        audioBuffer.clear()
        lastDecodedSampleCount = 0
        lastTranscript = ""
        isFinished = false
        trailingSilenceSampleCount = 0
        firstSpeechSampleIndex = -1
        lastSpeechSampleIndex = -1
    }

    override fun release() {
        // In offline mode, per-session native stream pointers are ephemeral and released in decodeInternal
    }

    companion object {
        fun computeRms(samples: FloatArray, start: Int = 0, length: Int = samples.size): Float {
            if (length <= 0) return 0f
            var sumSquares = 0.0
            val end = (start + length).coerceAtMost(samples.size)
            for (i in start until end) {
                val s = samples[i]
                sumSquares += s * s
            }
            return sqrt(sumSquares / length).toFloat()
        }
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

    val dataRef: FloatArray get() = data

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
