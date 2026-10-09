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
        controller.onPlatformStartRecording = { onSpeech, onSilence ->
            recordJob?.cancel()
            jsStartSpeech()
            recordJob = bridgeScope.launch {
                var lastText = ""
                while (jsIsSpeechRecording()) {
                    delay(200)
                    val currentText = jsGetSpeechTranscript()
                    if (currentText.isNotEmpty() && currentText != lastText) {
                        lastText = currentText
                        onSpeech(currentText)
                    }
                }
                val finalText = jsGetSpeechTranscript()
                if (finalText.isNotEmpty() && finalText != lastText) {
                    onSpeech(finalText)
                }
            }
        }

        controller.onPlatformStopRecording = {
            recordJob?.cancel()
            jsStopSpeech()
        }
    } catch (_: Throwable) {
        // Fallback gracefully on non-browser / test runs
    }
}
