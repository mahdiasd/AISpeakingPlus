package ir.aispeaking.chat.stt

import android.content.Context
import ir.aispeaking.utils.dLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android mic capture backed by `AudioRecord` at 16 kHz / 16-bit / mono.
 *
 * The MediaRecorder audio source `MIC` lets us request 16 kHz directly from
 * the platform, avoiding any software resampling — the server expects 16 kHz
 * PCM16 little-endian (§5.2 of STT_CLIENT_INTEGRATION.md).
 *
 * Each read yields ~100 ms of audio (1 600 samples = 3 200 bytes), which
 * matches the server's recommended ~200 ms chunk cadence when buffered once.
 *
 * The application [Context] is supplied via [AndroidAudioCapture.context]
 * — set once from the Android entry point (e.g. `androidApp`'s `onCreate`).
 * This keeps the `AudioCapture` expect class zero-argument in commonMain.
 */
actual class AudioCapture actual constructor() {

    actual fun start(): Flow<ByteArray> = callbackFlow {
        val ctx = AndroidAudioCapture.context
        if (ctx == null) {
            close(IllegalStateException(
                "AndroidAudioCapture.context was not initialised. " +
                    "Call AndroidAudioCapture.attach(context) from your Activity.onCreate().",
            ))
            return@callbackFlow
        }

        if (ctx.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            close(SecurityException("RECORD_AUDIO permission not granted"))
            return@callbackFlow
        }

        val sampleRate = SAMPLE_RATE_HZ
        val channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO
        val audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT

        val minBuffer = android.media.AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBuffer <= 0) {
            close(IllegalStateException("AudioRecord.getMinBufferSize returned $minBuffer"))
            return@callbackFlow
        }
        // Use 4x the minimum buffer so we don't lose samples under load.
        val bufferSize = minBuffer * 4

        val record = android.media.AudioRecord(
            android.media.MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize,
        )

        if (record.state != android.media.AudioRecord.STATE_INITIALIZED) {
            record.release()
            close(IllegalStateException("AudioRecord failed to initialize"))
            return@callbackFlow
        }

        androidAudioCaptureState.record = record
        record.startRecording()
        "AudioCapture: started @ ${sampleRate}Hz mono PCM16".dLog(tag = "SttAudio")

        // ~100 ms of audio = 1 600 samples = 3 200 bytes
        val frameSamples = sampleRate / 10
        val frame = ShortArray(frameSamples)

        val producer = launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val read = record.read(frame, 0, frameSamples)
                    when {
                        read < 0 -> {
                            close(IllegalStateException("AudioRecord.read returned $read"))
                            return@launch
                        }
                        read == 0 -> continue
                        else -> {
                            val bytes = ByteArray(read * 2)
                            for (i in 0 until read) {
                                val s = frame[i].toInt()
                                bytes[i * 2] = (s and 0xFF).toByte()
                                bytes[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
                            }
                            trySend(bytes)
                        }
                    }
                }
            } finally {
                runCatching { record.stop() }
                runCatching { record.release() }
                androidAudioCaptureState.record = null
            }
        }

        awaitClose { producer.cancel() }
    }.flowOn(Dispatchers.IO)

    actual fun stop() {
        androidAudioCaptureState.record?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        androidAudioCaptureState.record = null
    }

    actual fun isAvailable(): Boolean {
        val ctx = AndroidAudioCapture.context ?: return false
        return ctx.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val SAMPLE_RATE_HZ = 16_000
    }
}

/**
 * Holds the running [android.media.AudioRecord] so [AudioCapture.stop] can
 * release it from outside the coroutine that owns the start flow.
 */
internal object androidAudioCaptureState {
    @Volatile var record: android.media.AudioRecord? = null
}
