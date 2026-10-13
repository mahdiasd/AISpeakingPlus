package ir.aispeaking.androidApp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder.AudioSource
import android.net.Uri
import androidx.core.content.ContextCompat
import ir.aispeaking.chat.audio.DefaultStageAudioController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class AndroidAudioBridge(
    private val context: Context,
    private val controller: DefaultStageAudioController,
    private val onRequestRecordPermission: (((Boolean) -> Unit) -> Unit)? = null
) {
    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_BYTES = 2048 // 1024 samples of 16-bit PCM
        private const val SILENCE_RMS_THRESHOLD = 250.0
        private const val SILENCE_TIMEOUT_MS = 5000L
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    @Volatile
    private var isCapturingAudio = false
    private val bridgeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun attach() {
        controller.onPlatformPlayVoice = { rawUrl, onEnded ->
            stopAudio()
            if (rawUrl.isNullOrBlank()) {
                onEnded()
            } else {
                try {
                    val fullUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                        rawUrl
                    } else {
                        val base = ir.aispeaking.network.NetworkConfig.baseUrl.trimEnd('/')
                        val cleanPath = if (rawUrl.startsWith("/")) rawUrl else "/$rawUrl"
                        "$base$cleanPath"
                    }

                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(context, Uri.parse(fullUrl))
                        setOnCompletionListener {
                            stopAudio()
                            onEnded()
                        }
                        setOnErrorListener { _, _, _ ->
                            stopAudio()
                            onEnded()
                            true
                        }
                        prepareAsync()
                        setOnPreparedListener {
                            start()
                        }
                    }
                } catch (e: Exception) {
                    stopAudio()
                    onEnded()
                }
            }
        }

        controller.onPlatformStopVoice = {
            stopAudio()
        }

        controller.onPlatformStartRecording = { _, onSilence ->
            stopRecordingAudio()
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                startAudioRecordCapture(onSilence)
            } else if (onRequestRecordPermission != null) {
                onRequestRecordPermission.invoke { granted ->
                    if (granted) {
                        startAudioRecordCapture(onSilence)
                    } else {
                        onSilence()
                    }
                }
            } else {
                onSilence()
            }
        }

        controller.onPlatformStopRecording = {
            stopRecordingAudio()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAudioRecordCapture(onSilence: () -> Unit) {
        stopRecordingAudio()

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            onSilence()
            return
        }

        val bufferSizeInBytes = maxOf(minBufferSize, BUFFER_SIZE_BYTES * 2)
        val record = try {
            AudioRecord(
                AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSizeInBytes
            )
        } catch (_: Exception) {
            onSilence()
            return
        }

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            try {
                record.release()
            } catch (_: Exception) {}
            onSilence()
            return
        }

        try {
            record.startRecording()
        } catch (_: Exception) {
            try {
                record.release()
            } catch (_: Exception) {}
            onSilence()
            return
        }

        audioRecord = record
        isCapturingAudio = true

        recordingJob = bridgeScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(BUFFER_SIZE_BYTES)
            var lastVoiceActivityMs = System.currentTimeMillis()

            try {
                while (isActive && isCapturingAudio) {
                    val bytesRead = record.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        val pcmChunk = if (bytesRead == buffer.size) {
                            buffer.copyOf()
                        } else {
                            buffer.copyOfRange(0, bytesRead)
                        }
                        controller.onAudioChunkCaptured?.invoke(pcmChunk)

                        val rms = calculateRms(buffer, bytesRead)
                        val now = System.currentTimeMillis()
                        if (rms >= SILENCE_RMS_THRESHOLD) {
                            lastVoiceActivityMs = now
                            controller.notifyVoiceActivity()
                        } else if (now - lastVoiceActivityMs >= SILENCE_TIMEOUT_MS) {
                            isCapturingAudio = false
                            withContext(Dispatchers.Main) {
                                onSilence()
                            }
                            break
                        }
                    } else if (bytesRead < 0) {
                        break
                    }
                }
            } catch (_: Exception) {
            } finally {
                releaseAudioRecord(record)
            }
        }
    }

    private fun calculateRms(buffer: ByteArray, length: Int): Double {
        val sampleCount = length / 2
        if (sampleCount == 0) return 0.0
        var sumSquares = 0.0
        var i = 0
        while (i + 1 < length) {
            val lo = buffer[i].toInt() and 0xFF
            val hi = buffer[i + 1].toInt()
            val sample = (lo or (hi shl 8)).toShort().toDouble()
            sumSquares += sample * sample
            i += 2
        }
        return sqrt(sumSquares / sampleCount)
    }

    fun stopRecordingAudio() {
        isCapturingAudio = false
        recordingJob?.cancel()
        recordingJob = null
        val record = audioRecord
        audioRecord = null
        if (record != null) {
            releaseAudioRecord(record)
        }
    }

    private fun releaseAudioRecord(record: AudioRecord) {
        try {
            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                record.stop()
            }
        } catch (_: Exception) {}
        try {
            record.release()
        } catch (_: Exception) {}
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}

