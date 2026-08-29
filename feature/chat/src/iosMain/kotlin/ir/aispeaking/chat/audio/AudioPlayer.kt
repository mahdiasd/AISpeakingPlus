package ir.aispeaking.chat.audio

import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.play
import platform.AVFoundation.pause
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol

actual class AudioPlayer actual constructor() {
    private var player: AVPlayer? = null
    private var observer: NSObjectProtocol? = null
    private var currentSpeed: Float = 1.0f

    actual fun play(url: String, speed: Float, onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        stop()
        currentSpeed = speed
        try {
            val nsUrl = NSURL(string = url) ?: run {
                onError(IllegalArgumentException("Invalid URL: $url"))
                return
            }
            val item = AVPlayerItem(uRL = nsUrl)
            val avPlayer = AVPlayer(playerItem = item)
            player = avPlayer

            observer = NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = item,
                queue = null
            ) { _ ->
                stop()
                onComplete()
            }
            avPlayer.play()
            avPlayer.rate = speed
        } catch (t: Throwable) {
            stop()
            onError(t)
        }
    }

    actual fun setSpeed(speed: Float) {
        currentSpeed = speed
        try {
            player?.let {
                it.rate = speed
            }
        } catch (_: Throwable) {}
    }

    actual fun stop() {
        observer?.let {
            NSNotificationCenter.defaultCenter.removeObserver(it)
            observer = null
        }
        player?.pause()
        player = null
    }

    actual fun release() {
        stop()
    }
}
