package ir.aispeaking.chat.stt

import ir.aispeaking.utils.dLog
import kotlin.js.Promise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Web (JS) mic capture using `navigator.mediaDevices.getUserMedia` +
 * Web Audio API.
 *
 * Pipeline:
 *  1. `getUserMedia({ audio: true })` → MediaStream
 *  2. `AudioContext` at the browser's native rate (usually 44.1 / 48 kHz)
 *  3. `ScriptProcessorNode` decimates each render buffer to 16 kHz mono
 *  4. Float32 samples are clamped to [-1, 1], scaled to short range, and
 *     packed as little-endian PCM16 bytes
 *
 * The bytes emitted here are exactly what the server expects: mono 16 kHz
 * PCM16 little-endian, raw sample bytes (no WAV header).
 */
actual class AudioCapture actual constructor() {

    actual fun start(): Flow<ByteArray> = callbackFlow {
        val mediaDevices = js("typeof navigator !== 'undefined' ? navigator.mediaDevices : null")
        if (mediaDevices == null) {
            close(IllegalStateException("navigator.mediaDevices is not available in this environment"))
            return@callbackFlow
        }

        val mediaStream: dynamic = try {
            val promise: Promise<dynamic> = js("navigator.mediaDevices.getUserMedia({ audio: { channelCount: 1, echoCancellation: true, noiseSuppression: true, autoGainControl: true } })")
            promise.await()
        } catch (t: Throwable) {
            close(IllegalStateException("Microphone access denied: ${t.message ?: t}"))
            return@callbackFlow
        }

        val AudioContextCtor: dynamic = js("(typeof AudioContext !== 'undefined') ? AudioContext : (typeof webkitAudioContext !== 'undefined') ? webkitAudioContext : null")
        if (AudioContextCtor == null) {
            closeMediaStream(mediaStream)
            close(IllegalStateException("Web Audio API is not supported in this browser"))
            return@callbackFlow
        }

        val ctx = js("new AudioContextCtor()")
        js("try { if (ctx.state === 'suspended') ctx.resume(); } catch(e) {}")
        val sourceNode = ctx.createMediaStreamSource(mediaStream)
        val nativeSampleRate: Int = (ctx.sampleRate as Number).toInt()
        "AudioCapture: AudioContext @ ${nativeSampleRate}Hz, downsampling to 16kHz".dLog(tag = "SttAudio")

        // We accumulate ~200 ms of downsampled PCM16 before yielding a frame,
        // matching the server's recommended cadence (§5.3).
        val frameDurationMs = 200
        val frameByteCount = (SAMPLE_RATE_HZ * frameDurationMs / 1000) * 2
        val pending = ByteArray(frameByteCount)
        var pendingFilled = 0

        val bufferSize = 4096
        val processor = ctx.createScriptProcessor(bufferSize, 1, 1)
        sourceNode.connect(processor)
        processor.connect(ctx.destination)

        val stop: () -> Unit = {
            runCatching { processor.disconnect() }
            runCatching { sourceNode.disconnect() }
            runCatching { ctx.close() }
            closeMediaStream(mediaStream)
        }

        processor.onaudioprocess = jsFunction@{ event: dynamic ->
            if (!isActive) {
                stop()
                return@jsFunction
            }
            val inputBuffer: dynamic = event.inputBuffer
            val channelData: FloatArray = (inputBuffer.getChannelData(0) as FloatArray)
            val inLen = channelData.size

            val ratio = nativeSampleRate.toDouble() / SAMPLE_RATE_HZ
            val outLen = (inLen / ratio).toInt()

            for (i in 0 until outLen) {
                val srcIdx = (i * ratio).toInt()
                if (srcIdx >= inLen) break

                var sample = channelData[srcIdx]
                if (sample > 1f) sample = 1f else if (sample < -1f) sample = -1f

                val short = (sample * 32767f).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

                if (pendingFilled + 1 < pending.size) {
                    pending[pendingFilled++] = (short and 0xFF).toByte()
                    pending[pendingFilled++] = ((short shr 8) and 0xFF).toByte()
                }

                if (pendingFilled >= pending.size) {
                    trySend(pending.copyOf())
                    pendingFilled = 0
                }
            }
        }

        awaitClose { stop() }
    }.flowOn(Dispatchers.Default)

    actual fun stop() {
        // Captured `mediaStream` lives in the start() flow only — when the
        // collector cancels, the stop() lambda inside `awaitClose` runs.
    }

    actual fun isAvailable(): Boolean {
        val mediaDevices = js("typeof navigator !== 'undefined' ? navigator.mediaDevices : null")
        return mediaDevices != null
    }

    private fun closeMediaStream(stream: dynamic) {
        runCatching {
            val tracks: dynamic = stream.getTracks()
            if (tracks != null) {
                val length: Int = (tracks.length as Int)
                for (i in 0 until length) {
                    tracks.item(i).stop()
                }
            }
        }
    }

    companion object {
        private const val SAMPLE_RATE_HZ = 16_000
    }
}
