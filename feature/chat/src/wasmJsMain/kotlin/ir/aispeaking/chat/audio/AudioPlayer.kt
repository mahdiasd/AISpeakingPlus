package ir.aispeaking.chat.audio

import ir.aispeaking.utils.dLog

actual class AudioPlayer actual constructor() {
    private var activeAudio: JsAny? = null

    actual fun play(url: String, speed: Float, onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        stop()
        try {
            "AudioPlayer (WASM): Playing $url at speed $speed".dLog(tag = "AudioPlayer")
            val audio = wasmPlayAudio(
                url = url,
                speed = speed.toDouble(),
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

    actual fun setSpeed(speed: Float) {
        try {
            activeAudio?.let { wasmSetSpeed(it, speed.toDouble()) }
        } catch (_: Throwable) {}
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

@JsFun("(url, speed, onEnd, onError) => { try { const a = new Audio(url); a.playbackRate = speed || 1.0; a.onended = () => onEnd(); a.onerror = (e) => onError(e); a.play().catch((e) => onError(e)); return a; } catch (e) { onError(e); return null; } }")
private external fun wasmPlayAudio(url: String, speed: Double, onEnd: () -> Unit, onError: (JsAny) -> Unit): JsAny?

@JsFun("(a, speed) => { try { if (a) { a.playbackRate = speed || 1.0; } } catch (e) {} }")
private external fun wasmSetSpeed(a: JsAny, speed: Double): Unit

@JsFun("(a) => { try { if (a) { a.pause(); a.currentTime = 0; } } catch (e) {} }")
private external fun wasmStopAudio(a: JsAny): Unit
