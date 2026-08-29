package ir.aispeaking.chat.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import ir.aispeaking.utils.dLog

actual class AudioPlayer actual constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSpeed: Float = 1.0f

    actual fun play(url: String, speed: Float, onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        stop()
        currentSpeed = speed
        try {
            "AudioPlayer (Android): Playing $url at speed $speed".dLog(tag = "AudioPlayer")
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                mp.playbackParams = mp.playbackParams.setSpeed(currentSpeed)
                            } catch (_: Throwable) {
                                mp.playbackParams = PlaybackParams().apply { this.speed = currentSpeed }
                            }
                        }
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

    actual fun setSpeed(speed: Float) {
        currentSpeed = speed
        try {
            mediaPlayer?.let { mp ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mp.isPlaying) {
                    try {
                        mp.playbackParams = mp.playbackParams.setSpeed(speed)
                    } catch (_: Throwable) {
                        mp.playbackParams = PlaybackParams().apply { this.speed = speed }
                    }
                }
            }
        } catch (_: Throwable) {}
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
