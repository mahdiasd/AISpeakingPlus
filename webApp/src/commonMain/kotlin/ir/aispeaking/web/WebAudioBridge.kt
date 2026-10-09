@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

import ir.aispeaking.chat.audio.DefaultStageAudioController
import kotlinx.coroutines.*

// External JS functions for WasmJs browser runtime
@JsFun("(url) => window.aiSpeakingAudio ? window.aiSpeakingAudio.play(url) : null")
private external fun jsPlayAudio(url: String)

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
                    delay(100)
                    graceElapsedMs += 100
                }

                // If recording failed to start (e.g. permission denied or unsupported), notify controller and exit
                if (!jsIsSpeechRecording()) {
                    onSilence()
                    return@launch
                }

                // 2. Active recording loop: poll transcript while recording is active
                while (isActive) {
                    if (!jsIsSpeechRecording()) {
                        // Short grace check to absorb momentary restarts between utterances
                        delay(250)
                        if (!jsIsSpeechRecording()) {
                            break
                        }
                    }

                    delay(150)
                    val currentText = jsGetSpeechTranscript()
                    if (currentText.isNotEmpty() && currentText != lastText) {
                        lastText = currentText
                        onSpeech(currentText)
                    }
                }

                // 3. Flush any final transcript accumulated
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
            val finalText = jsGetSpeechTranscript()
            if (finalText.isNotEmpty()) {
                currentOnSpeech?.invoke(finalText)
            }
            recordJob?.cancel()
            recordJob = null
            currentOnSpeech = null
        }
    } catch (_: Throwable) {
        // Fallback gracefully on non-browser / test runs
    }
}
