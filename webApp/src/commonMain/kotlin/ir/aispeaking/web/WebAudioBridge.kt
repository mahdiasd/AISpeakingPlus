@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class, kotlin.io.encoding.ExperimentalEncodingApi::class)

package ir.aispeaking.web

import ir.aispeaking.chat.audio.DefaultStageAudioController
import kotlinx.coroutines.*
import kotlin.io.encoding.Base64

// External JS functions for WasmJs / JS browser runtime
@JsFun("(url) => window.aiSpeakingAudio ? window.aiSpeakingAudio.play(url) : null")
private external fun jsPlayAudio(url: String)

@JsFun("(url) => window.aiSpeakingAudio ? window.aiSpeakingAudio.preload(url) : null")
private external fun jsPreloadAudio(url: String)

@JsFun("() => window.aiSpeakingAudio ? window.aiSpeakingAudio.stop() : null")
private external fun jsStopAudio()

@JsFun("() => !!(window.aiSpeakingAudio && window.aiSpeakingAudio.isPlaying())")
private external fun jsIsAudioPlaying(): Boolean

@JsFun("() => window.aiSpeakingSpeech ? window.aiSpeakingSpeech.start() : null")
private external fun jsStartSpeech()

@JsFun("() => window.aiSpeakingSpeech ? window.aiSpeakingSpeech.stop() : null")
private external fun jsStopSpeech()

@JsFun("() => window.aiSpeakingSpeech ? window.aiSpeakingSpeech.getTranscript() : ''")
private external fun jsGetSpeechTranscript(): String

@JsFun("() => !!(window.aiSpeakingSpeech && window.aiSpeakingSpeech.isRecording)")
private external fun jsIsSpeechRecording(): Boolean

@JsFun("() => window.aiSpeakingSpeech ? window.aiSpeakingSpeech.drainPcmChunks() : ''")
private external fun jsDrainPcmChunks(): String

@JsFun("() => !!(window.aiSpeakingSpeech && window.aiSpeakingSpeech.hasVoiceActivity && window.aiSpeakingSpeech.hasVoiceActivity())")
private external fun jsHasVoiceActivity(): Boolean

@JsFun("() => window.aiSpeakingPermission ? window.aiSpeakingPermission.check() : 'prompt'")
private external fun jsCheckSpeechPermission(): String

@JsFun("() => window.aiSpeakingPermission ? window.aiSpeakingPermission.request() : null")
private external fun jsRequestSpeechPermission()

@JsFun("() => window.aiSpeakingPermission ? window.aiSpeakingPermission.getState() : 'prompt'")
private external fun jsGetSpeechPermissionState(): String

@JsFun("() => window.aiSpeakingPermission ? window.aiSpeakingPermission.getLastResult() : ''")
private external fun jsGetSpeechPermissionLastResult(): String

@JsFun("() => !!(window.aiSpeakingPermission && window.aiSpeakingPermission.isRequesting())")
private external fun jsIsSpeechPermissionRequesting(): Boolean

private val bridgeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun setupWebAudioBridge(controller: DefaultStageAudioController) {
    try {
        var playJob: Job? = null
        controller.onPlatformPlayVoice = { url, onEnded ->
            playJob?.cancel()
            if (url.isNullOrBlank()) {
                onEnded()
            } else {
                jsPlayAudio(url)
                playJob = bridgeScope.launch {
                    delay(300)
                    var elapsed = 0
                    while (jsIsAudioPlaying() && elapsed < 60000) {
                        delay(100)
                        elapsed += 100
                    }
                    onEnded()
                }
            }
        }

        controller.onPlatformPreloadVoice = { url ->
            if (!url.isNullOrBlank()) {
                jsPreloadAudio(url)
            }
        }

        controller.onPlatformStopVoice = {
            playJob?.cancel()
            jsStopAudio()
        }

        var recordJob: Job? = null
        var currentOnSpeech: ((String) -> Unit)? = null

        controller.onPlatformStartRecording = { onSpeech, onSilence ->
            recordJob?.cancel()
            currentOnSpeech = onSpeech
            jsStartSpeech()
            recordJob = bridgeScope.launch {
                var lastText = ""

                // 1. Initialization grace period: wait for recording to start (e.g. during mic permission prompt or initialization)
                var graceElapsedMs = 0
                val maxGraceMs = 3000
                while (!jsIsSpeechRecording() && graceElapsedMs < maxGraceMs && isActive) {
                    delay(50)
                    graceElapsedMs += 50
                }

                // If recording failed to start (e.g. permission denied or unsupported), notify controller and exit
                if (!jsIsSpeechRecording()) {
                    onSilence()
                    return@launch
                }

                // 2. Active recording loop: drain PCM chunks and stream to controller
                while (isActive) {
                    if (!jsIsSpeechRecording()) {
                        // Short grace check to absorb momentary restarts between utterances
                        delay(150)
                        if (!jsIsSpeechRecording()) {
                            break
                        }
                    }

                    // Drain binary PCM audio chunks captured from Web Audio API
                    val chunksPayload = jsDrainPcmChunks()
                    if (chunksPayload.isNotEmpty()) {
                        val chunks = chunksPayload.split('|')
                        for (base64Chunk in chunks) {
                            val trimmed = base64Chunk.trim()
                            if (trimmed.isNotEmpty()) {
                                try {
                                    val bytes = Base64.decode(trimmed)
                                    controller.onAudioChunkCaptured?.invoke(bytes)
                                } catch (_: Throwable) {}
                            }
                        }
                    }

                    // Reset silence timer on controller if voice activity detected
                    if (jsHasVoiceActivity()) {
                        controller.notifyVoiceActivity()
                    }

                    // Poll secondary speech transcript (from SpeechRecognition fallback if active)
                    val currentText = jsGetSpeechTranscript()
                    if (currentText.isNotEmpty() && currentText != lastText) {
                        lastText = currentText
                        onSpeech(currentText)
                    }

                    delay(40)
                }

                // 3. Flush any final PCM chunks and transcript accumulated
                val remainingPayload = jsDrainPcmChunks()
                if (remainingPayload.isNotEmpty()) {
                    val chunks = remainingPayload.split('|')
                    for (base64Chunk in chunks) {
                        val trimmed = base64Chunk.trim()
                        if (trimmed.isNotEmpty()) {
                            try {
                                val bytes = Base64.decode(trimmed)
                                controller.onAudioChunkCaptured?.invoke(bytes)
                            } catch (_: Throwable) {}
                        }
                    }
                }

                val finalText = jsGetSpeechTranscript()
                if (finalText.isNotEmpty() && finalText != lastText) {
                    onSpeech(finalText)
                }

                // 4. Notify silence / stop if recording finished naturally or by browser
                onSilence()
            }
        }

        controller.onPlatformStopRecording = {
            jsStopSpeech()
            val remainingPayload = jsDrainPcmChunks()
            if (remainingPayload.isNotEmpty()) {
                val chunks = remainingPayload.split('|')
                for (base64Chunk in chunks) {
                    val trimmed = base64Chunk.trim()
                    if (trimmed.isNotEmpty()) {
                        try {
                            val bytes = Base64.decode(trimmed)
                            controller.onAudioChunkCaptured?.invoke(bytes)
                        } catch (_: Throwable) {}
                    }
                }
            }
            val finalText = jsGetSpeechTranscript()
            if (finalText.isNotEmpty()) {
                currentOnSpeech?.invoke(finalText)
            }
            recordJob?.cancel()
            recordJob = null
            currentOnSpeech = null
        }

        controller.onPlatformCheckMicrophonePermission = { callback ->
            try {
                jsCheckSpeechPermission()
                bridgeScope.launch {
                    delay(80)
                    val state = jsGetSpeechPermissionState()
                    callback(state == "granted")
                }
            } catch (_: Throwable) {
                callback(true)
            }
        }

        controller.onPlatformRequestMicrophonePermission = { callback ->
            try {
                jsRequestSpeechPermission()
                bridgeScope.launch {
                    var elapsed = 0
                    while (jsIsSpeechPermissionRequesting() && elapsed < 30000 && isActive) {
                        delay(100)
                        elapsed += 100
                    }
                    val result = jsGetSpeechPermissionLastResult()
                    callback(result == "granted")
                }
            } catch (_: Throwable) {
                callback(true)
            }
        }
    } catch (_: Throwable) {
        // Fallback gracefully on non-browser / test runs
    }
}

