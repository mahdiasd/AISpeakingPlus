package ir.aispeaking.chat.audio

import ir.aispeaking.utils.dLog
import java.net.URI
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

actual class AudioPlayer actual constructor() {
    private var currentClip: Clip? = null
    private var playThread: Thread? = null

    actual fun play(url: String, onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        stop()
        playThread = Thread {
            try {
                "AudioPlayer (JVM): streaming $url".dLog(tag = "AudioPlayer")
                val audioUrl = URI(url).toURL()
                val audioInputStream = AudioSystem.getAudioInputStream(audioUrl)
                val clip = AudioSystem.getClip()
                currentClip = clip

                clip.addLineListener { event ->
                    if (event.type == LineEvent.Type.STOP) {
                        try {
                            clip.close()
                        } catch (_: Throwable) {}
                        onComplete()
                    }
                }

                clip.open(audioInputStream)
                clip.start()
            } catch (t: Throwable) {
                "AudioPlayer (JVM) error: ${t.message}".dLog(tag = "AudioPlayer")
                onError(t)
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    actual fun stop() {
        try {
            currentClip?.let {
                if (it.isRunning) it.stop()
                it.close()
            }
            playThread?.interrupt()
        } catch (_: Throwable) {}
        currentClip = null
        playThread = null
    }

    actual fun release() {
        stop()
    }
}
