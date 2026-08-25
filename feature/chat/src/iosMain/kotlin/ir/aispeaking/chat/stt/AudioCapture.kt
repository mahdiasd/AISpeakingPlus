package ir.aispeaking.chat.stt

import ir.aispeaking.utils.dLog
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.setActive

/**
 * iOS microphone capture backed by [AVAudioEngine] and [AVAudioSession].
 *
 * Captures audio via [AVAudioInputNode] tap at the device's native rate,
 * downsamples to 16 kHz mono Float32, and encodes to little-endian signed
 * 16-bit PCM bytes (PCM16) matching the server-side STT requirements.
 */
@OptIn(ExperimentalForeignApi::class)
actual class AudioCapture actual constructor() {

    actual fun start(): Flow<ByteArray> = callbackFlow {
        val audioSession = AVAudioSession.sharedInstance()

        val categoryOptions = AVAudioSessionCategoryOptionDefaultToSpeaker or
            AVAudioSessionCategoryOptionAllowBluetooth

        val categorySuccess = audioSession.setCategory(
            category = AVAudioSessionCategoryPlayAndRecord,
            withOptions = categoryOptions,
            error = null
        )
        if (!categorySuccess) {
            "AudioCapture (iOS): Failed to set AVAudioSession category".dLog(tag = "SttAudio")
        }

        audioSession.setActive(true, error = null)

        val engine = AVAudioEngine()
        val inputNode = engine.inputNode
        val inputFormat = inputNode.inputFormatForBus(0u)
        val nativeSampleRate = inputFormat.sampleRate

        "AudioCapture (iOS): inputFormat @ ${nativeSampleRate}Hz, channels=${inputFormat.channelCount}"
            .dLog(tag = "SttAudio")

        val frameDurationMs = 200
        val frameByteCount = (SAMPLE_RATE_HZ * frameDurationMs / 1000) * 2
        val pending = ByteArray(frameByteCount)
        var pendingFilled = 0

        val stopCapture: () -> Unit = {
            runCatching { inputNode.removeTapOnBus(0u) }
            runCatching { engine.stop() }
            runCatching { audioSession.setActive(false, error = null) }
            iosAudioCaptureState.engine = null
        }

        iosAudioCaptureState.engine = engine

        inputNode.installTapOnBus(
            bus = 0u,
            bufferSize = 4096u,
            format = inputFormat
        ) { buffer: AVAudioPCMBuffer?, _ ->
            if (!isActive || buffer == null) return@installTapOnBus

            val channelDataPtr = buffer.floatChannelData ?: return@installTapOnBus
            val channelData = channelDataPtr[0] ?: return@installTapOnBus
            val inLen = buffer.frameLength.toInt()
            if (inLen <= 0) return@installTapOnBus

            val ratio = nativeSampleRate / SAMPLE_RATE_HZ.toDouble()
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

        val startSuccess = engine.startAndReturnError(null)
        if (!startSuccess) {
            stopCapture()
            close(IllegalStateException("AVAudioEngine failed to start"))
            return@callbackFlow
        }

        "AudioCapture (iOS): engine started successfully".dLog(tag = "SttAudio")

        awaitClose {
            stopCapture()
        }
    }

    actual fun stop() {
        iosAudioCaptureState.engine?.let { engine ->
            runCatching { engine.inputNode.removeTapOnBus(0u) }
            runCatching { engine.stop() }
        }
        iosAudioCaptureState.engine = null
        runCatching { AVAudioSession.sharedInstance().setActive(false, error = null) }
    }

    actual fun isAvailable(): Boolean {
        val permission = AVAudioSession.sharedInstance().recordPermission
        return permission == AVAudioSessionRecordPermissionGranted
    }

    companion object {
        private const val SAMPLE_RATE_HZ = 16_000
    }
}

internal object iosAudioCaptureState {
    var engine: AVAudioEngine? = null
}
