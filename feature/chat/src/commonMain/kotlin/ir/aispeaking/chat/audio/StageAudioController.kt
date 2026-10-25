package ir.aispeaking.chat.audio

import kotlinx.coroutines.*
import org.koin.core.annotation.Single

interface StageAudioController {
    var onAudioChunkCaptured: ((ByteArray) -> Unit)?

    fun playVoice(url: String?, onEnded: () -> Unit)
    fun stopVoice()
    fun isVoicePlaying(): Boolean
    fun preloadVoice(url: String?) {}

    fun startRecording(
        onSpeechRecognized: (String) -> Unit,
        onSilenceDetected: () -> Unit
    )
    fun stopRecording()
    fun isRecording(): Boolean

    fun checkMicrophonePermission(onResult: (Boolean) -> Unit) {
        onResult(true)
    }

    fun requestMicrophonePermission(onResult: (Boolean) -> Unit) {
        onResult(true)
    }
}

@Single
class DefaultStageAudioController : StageAudioController {
    override var onAudioChunkCaptured: ((ByteArray) -> Unit)? = null

    private var isPlayingState = false
    private var isRecordingState = false
    private var currentOnEnded: (() -> Unit)? = null
    private var silenceJob: Job? = null
    private var currentOnSilenceDetected: (() -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Platform callback bridges (can be set by platform host like Web/Android)
    var onPlatformPlayVoice: ((String?, () -> Unit) -> Unit)? = null
    var onPlatformPreloadVoice: ((String?) -> Unit)? = null
    var onPlatformStopVoice: (() -> Unit)? = null
    var onPlatformStartRecording: (((String) -> Unit, () -> Unit) -> Unit)? = null
    var onPlatformStopRecording: (() -> Unit)? = null
    var onPlatformCheckMicrophonePermission: (((Boolean) -> Unit) -> Unit)? = null
    var onPlatformRequestMicrophonePermission: (((Boolean) -> Unit) -> Unit)? = null

    override fun preloadVoice(url: String?) {
        if (!url.isNullOrBlank()) {
            onPlatformPreloadVoice?.invoke(url)
        }
    }

    fun dispatchAudioChunk(bytes: ByteArray) {
        onAudioChunkCaptured?.invoke(bytes)
    }

    fun notifyVoiceActivity() {
        currentOnSilenceDetected?.let { resetSilenceTimer(it) }
    }

    override fun playVoice(url: String?, onEnded: () -> Unit) {
        stopVoice()
        if (url.isNullOrBlank()) {
            isPlayingState = false
            currentOnEnded = null
            onEnded()
            return
        }
        isPlayingState = true
        currentOnEnded = onEnded

        if (onPlatformPlayVoice != null) {
            onPlatformPlayVoice?.invoke(url) {
                isPlayingState = false
                currentOnEnded = null
                onEnded()
            }
        } else {
            // Default simulated duration if no platform native player attached
            scope.launch {
                delay(3000)
                if (isPlayingState) {
                    isPlayingState = false
                    currentOnEnded = null
                    onEnded()
                }
            }
        }
    }

    override fun stopVoice() {
        isPlayingState = false
        currentOnEnded?.invoke()
        currentOnEnded = null
        onPlatformStopVoice?.invoke()
    }

    override fun isVoicePlaying(): Boolean = isPlayingState

    override fun startRecording(
        onSpeechRecognized: (String) -> Unit,
        onSilenceDetected: () -> Unit
    ) {
        val savedChunkCallback = onAudioChunkCaptured
        stopRecording()
        onAudioChunkCaptured = savedChunkCallback
        isRecordingState = true
        currentOnSilenceDetected = onSilenceDetected

        resetSilenceTimer(onSilenceDetected)

        if (onPlatformStartRecording != null) {
            onPlatformStartRecording?.invoke(
                { text ->
                    resetSilenceTimer(onSilenceDetected)
                    onSpeechRecognized(text)
                },
                {
                    stopRecording()
                    onSilenceDetected()
                }
            )
        }
    }

    private fun resetSilenceTimer(onSilenceDetected: () -> Unit) {
        silenceJob?.cancel()
        silenceJob = scope.launch {
            delay(5000) // 5 seconds of silence auto-pause
            if (isRecordingState) {
                stopRecording()
                onSilenceDetected()
            }
        }
    }

    fun onSpeechReceived(text: String, onSpeechRecognized: (String) -> Unit, onSilenceDetected: () -> Unit) {
        currentOnSilenceDetected = onSilenceDetected
        resetSilenceTimer(onSilenceDetected)
        onSpeechRecognized(text)
    }

    override fun stopRecording() {
        isRecordingState = false
        silenceJob?.cancel()
        silenceJob = null
        currentOnSilenceDetected = null
        onAudioChunkCaptured = null
        onPlatformStopRecording?.invoke()
    }

    override fun isRecording(): Boolean = isRecordingState

    override fun checkMicrophonePermission(onResult: (Boolean) -> Unit) {
        if (onPlatformCheckMicrophonePermission != null) {
            onPlatformCheckMicrophonePermission?.invoke(onResult)
        } else {
            onResult(true)
        }
    }

    override fun requestMicrophonePermission(onResult: (Boolean) -> Unit) {
        if (onPlatformRequestMicrophonePermission != null) {
            onPlatformRequestMicrophonePermission?.invoke(onResult)
        } else {
            onResult(true)
        }
    }
}
