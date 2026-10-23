package ir.aispeaking.sharedui.ui.video

interface VideoPlayerBridge {
    fun play(url: String, onEnded: () -> Unit, onSkip: () -> Unit)
    fun stop()
}

object VideoPlayerBridgeHolder {
    var bridge: VideoPlayerBridge? = null
}
