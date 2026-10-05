package ir.aispeaking.androidApp

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import ir.aispeaking.sharedui.ui.stage.audio.DefaultStageAudioController

class AndroidAudioBridge(
    private val context: Context,
    private val controller: DefaultStageAudioController
) {
    private var mediaPlayer: MediaPlayer? = null

    fun attach() {
        controller.onPlatformPlayVoice = { rawUrl, onEnded ->
            stopAudio()
            if (rawUrl.isNullOrBlank()) {
                onEnded()
            } else {
                try {
                    val fullUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                        rawUrl
                    } else {
                        val base = ir.aispeaking.network.NetworkConfig.baseUrl.trimEnd('/')
                        val cleanPath = if (rawUrl.startsWith("/")) rawUrl else "/$rawUrl"
                        "$base$cleanPath"
                    }

                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(context, Uri.parse(fullUrl))
                        setOnCompletionListener {
                            stopAudio()
                            onEnded()
                        }
                        setOnErrorListener { _, _, _ ->
                            stopAudio()
                            onEnded()
                            true
                        }
                        prepareAsync()
                        setOnPreparedListener {
                            start()
                        }
                    }
                } catch (e: Exception) {
                    stopAudio()
                    onEnded()
                }
            }
        }

        controller.onPlatformStopVoice = {
            stopAudio()
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
