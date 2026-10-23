@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

import ir.aispeaking.sharedui.ui.video.VideoPlayerBridge
import ir.aispeaking.sharedui.ui.video.VideoPlayerBridgeHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@JsFun("(src) => window.aiSpeakingVideo ? window.aiSpeakingVideo.show(src) : null")
private external fun jsShowVideo(src: String)

@JsFun("() => window.aiSpeakingVideo ? window.aiSpeakingVideo.hide() : null")
private external fun jsHideVideo()

@JsFun("() => !!(window.aiSpeakingVideo && window.aiSpeakingVideo.consumeEnded())")
private external fun jsConsumeVideoEnded(): Boolean

@JsFun("() => !!(window.aiSpeakingVideo && window.aiSpeakingVideo.consumeSkipped())")
private external fun jsConsumeVideoSkipped(): Boolean

private val videoScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun setupWebVideoBridge() {
    VideoPlayerBridgeHolder.bridge = object : VideoPlayerBridge {
        private var pollingJob: Job? = null

        override fun play(url: String, onEnded: () -> Unit, onSkip: () -> Unit) {
            pollingJob?.cancel()
            jsShowVideo(url)
            pollingJob = videoScope.launch {
                while (isActive) {
                    if (jsConsumeVideoEnded()) {
                        onEnded()
                        break
                    }
                    if (jsConsumeVideoSkipped()) {
                        onSkip()
                        break
                    }
                    delay(50)
                }
            }
        }

        override fun stop() {
            pollingJob?.cancel()
            pollingJob = null
            jsHideVideo()
        }
    }
}
