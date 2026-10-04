@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

import ir.aispeaking.sharedui.ui.stage.audio.DefaultStageAudioController

// External JS functions for WasmJs browser runtime
@JsFun("(url) => window.aiSpeakingAudio ? window.aiSpeakingAudio.play(url, () => window._onAudioEnded && window._onAudioEnded()) : null")
private external fun jsPlayAudio(url: String)

@JsFun("() => window.aiSpeakingAudio ? window.aiSpeakingAudio.stop() : null")
private external fun jsStopAudio()

@JsFun("() => window.aiSpeakingSpeech ? window.aiSpeakingSpeech.start((text) => window._onSpeechResult && window._onSpeechResult(text)) : null")
private external fun jsStartSpeech()

@JsFun("() => window.aiSpeakingSpeech ? window.aiSpeakingSpeech.stop() : null")
private external fun jsStopSpeech()

fun setupWebAudioBridge(controller: DefaultStageAudioController) {
    try {
        controller.onPlatformPlayVoice = { url, onEnded ->
            jsPlayAudio(url ?: "")
        }
        controller.onPlatformStopVoice = {
            jsStopAudio()
        }
        controller.onPlatformStartRecording = { onSpeech, onSilence ->
            jsStartSpeech()
        }
        controller.onPlatformStopRecording = {
            jsStopSpeech()
        }
    } catch (_: Throwable) {
        // Fallback gracefully on non-browser / test runs
    }
}
