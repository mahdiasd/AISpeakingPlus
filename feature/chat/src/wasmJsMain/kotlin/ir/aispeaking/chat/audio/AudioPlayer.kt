package ir.aispeaking.chat.audio

import ir.aispeaking.utils.dLog

actual class AudioPlayer actual constructor() {
    private var activeAudio: JsAny? = null

    actual fun play(url: String, onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        stop()
        try {
            "AudioPlayer (WASM): Playing $url".dLog(tag = "AudioPlayer")
            val audio = wasmPlayAudio(
                url = url,
                onEnd = {
                    stop()
                    onComplete()
                },
                onError = { err ->
                    stop()
                    onError(Exception("Wasm audio playback error"))
                }
            )
            activeAudio = audio
        } catch (t: Throwable) {
            stop()
            onError(t)
        }
    }

    actual fun stop() {
        try {
            activeAudio?.let { wasmStopAudio(it) }
        } catch (_: Throwable) {}
        activeAudio = null
    }

    actual fun release() {
        stop()
    }
}

@JsFun("(url, onEnd, onError) => { try { const a = new Audio(url); a.onended = () => onEnd(); a.onerror = (e) => onError(e); a.play().catch((e) => onError(e)); return a; } catch (e) { onError(e); return null; } }")
private external fun wasmPlayAudio(url: String, onEnd: () -> Unit, onError: (JsAny) -> Unit): JsAny?

@JsFun("(a) => { try { if (a) { a.pause(); a.currentTime = 0; } } catch (e) {} }")
private external fun wasmStopAudio(a: JsAny): Unit
