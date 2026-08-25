package ir.aispeaking.chat.stt

import ir.aispeaking.utils.dLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

/**
 * Desktop JVM microphone capture backed by Java's standard [TargetDataLine]
 * at 16 kHz / 16-bit / mono / little-endian signed PCM.
 *
 * Requests 16 kHz directly from the system mixer (no software resampling needed).
 * Emits ~200 ms PCM16 chunks (6 400 bytes per frame) matching the server-side
 * STT requirements (§5.1-5.3 of STT_CLIENT_INTEGRATION.md).
 */
actual class AudioCapture actual constructor() {

    actual fun start(): Flow<ByteArray> = callbackFlow {
        val format = getAudioFormat()
        val info = DataLine.Info(TargetDataLine::class.java, format)

        if (!AudioSystem.isLineSupported(info)) {
            close(IllegalStateException("Desktop audio capture line not supported for 16kHz mono PCM16"))
            return@callbackFlow
        }

        val line: TargetDataLine = try {
            (AudioSystem.getLine(info) as TargetDataLine).apply {
                // Buffer size for ~1 second of audio
                open(format, SAMPLE_RATE_HZ * 2)
                start()
            }
        } catch (t: Throwable) {
            close(IllegalStateException("Failed to open audio recording line: ${t.message}", t))
            return@callbackFlow
        }

        jvmAudioCaptureState.activeLine = line
        "AudioCapture (Desktop JVM): started @ ${SAMPLE_RATE_HZ}Hz mono PCM16".dLog(tag = "SttAudio")

        // 200 ms frame = 3 200 samples = 6 400 bytes
        val frameBytes = (SAMPLE_RATE_HZ * FRAME_DURATION_MS / 1000) * 2
        val buffer = ByteArray(frameBytes)

        val readerJob = launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    var totalRead = 0
                    while (totalRead < frameBytes && isActive) {
                        val read = line.read(buffer, totalRead, frameBytes - totalRead)
                        if (read < 0) {
                            close(IllegalStateException("TargetDataLine.read returned $read"))
                            return@launch
                        }
                        totalRead += read
                    }
                    if (totalRead > 0) {
                        trySend(buffer.copyOf(totalRead))
                    }
                }
            } catch (t: Throwable) {
                if (isActive) {
                    "AudioCapture (Desktop JVM): error reading audio: ${t.message}".dLog(tag = "SttAudio")
                    close(t)
                }
            } finally {
                runCatching { line.stop() }
                runCatching { line.close() }
                jvmAudioCaptureState.activeLine = null
            }
        }

        awaitClose {
            readerJob.cancel()
            runCatching { line.stop() }
            runCatching { line.close() }
            jvmAudioCaptureState.activeLine = null
        }
    }.flowOn(Dispatchers.IO)

    actual fun stop() {
        jvmAudioCaptureState.activeLine?.let {
            runCatching { it.stop() }
            runCatching { it.close() }
        }
        jvmAudioCaptureState.activeLine = null
    }

    actual fun isAvailable(): Boolean {
        val format = getAudioFormat()
        val info = DataLine.Info(TargetDataLine::class.java, format)
        return AudioSystem.isLineSupported(info)
    }

    companion object {
        private const val SAMPLE_RATE_HZ = 16_000
        private const val FRAME_DURATION_MS = 200

        private fun getAudioFormat() = AudioFormat(
            /* sampleRate = */ 16000.0f,
            /* sampleSizeInBits = */ 16,
            /* channels = */ 1,
            /* signed = */ true,
            /* bigEndian = */ false // little-endian
        )
    }
}

/**
 * Holds the active [TargetDataLine] so [AudioCapture.stop] can release it
 * from any thread.
 */
internal object jvmAudioCaptureState {
    @Volatile var activeLine: TargetDataLine? = null
}
