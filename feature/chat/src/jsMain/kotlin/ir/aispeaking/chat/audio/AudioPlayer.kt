package ir.aispeaking.chat.audio

import ir.aispeaking.utils.dLog
import org.w3c.dom.Audio

actual class AudioPlayer actual constructor() {
    private var audioElement: Audio? = null

    actual fun play(url: String, speed: Float, onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        stop()
        try {
            "AudioPlayer (JS): Playing $url at speed $speed".dLog(tag = "AudioPlayer")
            val audio = Audio(url)
            audioElement = audio
            audio.playbackRate = speed.toDouble()
            audio.onended = {
                stop()
                onComplete()
            }
            audio.onerror = { _, _, _, _, _ ->
                stop()
                onError(Exception("HTMLAudioElement error loading audio"))
            }
            audio.play().catch { err ->
                stop()
                onError(Exception(err.toString()))
            }
        } catch (t: Throwable) {
            stop()
            onError(t)
        }
    }

    actual fun setSpeed(speed: Float) {
        try {
            audioElement?.playbackRate = speed.toDouble()
        } catch (_: Throwable) {}
    }

    actual fun stop() {
        try {
            audioElement?.pause()
            audioElement?.currentTime = 0.0
        } catch (_: Throwable) {}
        audioElement = null
    }

    actual fun release() {
        stop()
    }
}
