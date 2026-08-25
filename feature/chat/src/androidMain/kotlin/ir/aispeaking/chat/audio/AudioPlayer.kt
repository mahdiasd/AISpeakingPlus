package ir.aispeaking.chat.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import ir.aispeaking.utils.dLog

actual class AudioPlayer actual constructor() {
    private var mediaPlayer: MediaPlayer? = null

    actual fun play(url: String, onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        stop()
        try {
            "AudioPlayer (Android): Playing $url".dLog(tag = "AudioPlayer")
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    try {
                        mp.start()
                    } catch (t: Throwable) {
                        onError(t)
                    }
                }
                setOnCompletionListener {
                    stop()
                    onComplete()
                }
                setOnErrorListener { _, what, extra ->
                    "AudioPlayer (Android) error: what=$what, extra=$extra".dLog(tag = "AudioPlayer")
                    stop()
                    onError(Exception("MediaPlayer error: what=$what, extra=$extra"))
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (t: Throwable) {
            "AudioPlayer (Android) exception: ${t.message}".dLog(tag = "AudioPlayer")
            stop()
            onError(t)
        }
    }

    actual fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (_: Throwable) {
        }
        mediaPlayer = null
    }

    actual fun release() {
        stop()
    }
}
