package ir.aispeaking.chat.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Cross-platform Audio Player for streaming and playing audio responses from Kokoro-82M TTS.
 */
expect class AudioPlayer() {
    fun play(url: String, speed: Float = 1.0f, onComplete: () -> Unit = {}, onError: (Throwable) -> Unit = {})
    fun setSpeed(speed: Float)
    fun stop()
    fun release()
}

@Composable
fun rememberAudioPlayer(): AudioPlayer {
    val player = remember { AudioPlayer() }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    return player
}
