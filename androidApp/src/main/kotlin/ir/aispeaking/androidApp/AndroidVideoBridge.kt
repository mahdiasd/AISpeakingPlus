package ir.aispeaking.androidApp

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import ir.aispeaking.network.resolveMediaUrl
import ir.aispeaking.sharedui.ui.video.VideoPlayerBridge
import ir.aispeaking.sharedui.ui.video.VideoPlayerBridgeHolder

class AndroidVideoBridge(
    private val activity: Activity
) : VideoPlayerBridge {

    private var currentDialog: Dialog? = null
    private var currentVideoView: VideoView? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun attach() {
        VideoPlayerBridgeHolder.bridge = this
    }

    fun detach() {
        stop()
        if (VideoPlayerBridgeHolder.bridge === this) {
            VideoPlayerBridgeHolder.bridge = null
        }
    }

    override fun play(url: String, onEnded: () -> Unit, onSkip: () -> Unit) {
        mainHandler.post {
            if (activity.isFinishing || activity.isDestroyed) return@post
            stop()

            val fullUrl = resolveMediaUrl(url)

            val dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
            }

            val rootLayout = FrameLayout(activity).apply {
                setBackgroundColor(Color.BLACK)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val videoView = VideoView(activity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            }
            rootLayout.addView(videoView)

            // Indeterminate loading spinner while video is preparing
            val progressBar = ProgressBar(activity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
                isIndeterminate = true
            }
            rootLayout.addView(progressBar)

            // Semi-transparent sleek skip button
            val density = activity.resources.displayMetrics.density
            val skipButton = TextView(activity).apply {
                text = "رد کردن ›"
                setTextColor(Color.WHITE)
                textSize = 14f
                val dp14 = (14 * density).toInt()
                val dp8 = (8 * density).toInt()
                setPadding(dp14, dp8, dp14, dp8)
                background = GradientDrawable().apply {
                    cornerRadius = 24 * density
                    setColor(Color.parseColor("#66000000"))
                    setStroke((1 * density).toInt(), Color.parseColor("#4DFFFFFF"))
                }
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP or Gravity.START
                ).apply {
                    val dp16 = (16 * density).toInt()
                    val dp36 = (36 * density).toInt()
                    setMargins(dp16, dp36, dp16, dp16)
                }
                setOnClickListener {
                    stop()
                    onSkip()
                }
            }
            rootLayout.addView(skipButton)

            videoView.setOnPreparedListener { mp ->
                progressBar.visibility = View.GONE
                mp.isLooping = false
                videoView.start()
            }

            videoView.setOnCompletionListener {
                stop()
                onEnded()
            }

            videoView.setOnErrorListener { _, _, _ ->
                // On error, safely dismiss and allow the user to continue via onSkip
                stop()
                onSkip()
                true
            }

            try {
                videoView.setVideoURI(Uri.parse(fullUrl))
                dialog.setContentView(rootLayout)
                dialog.window?.let { win ->
                    win.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                    win.setBackgroundDrawable(ColorDrawable(Color.BLACK))
                }
                dialog.show()

                currentDialog = dialog
                currentVideoView = videoView
            } catch (_: Exception) {
                stop()
                onSkip()
            }
        }
    }

    override fun stop() {
        mainHandler.post {
            try {
                currentVideoView?.stopPlayback()
            } catch (_: Throwable) {}
            currentVideoView = null

            try {
                if (currentDialog?.isShowing == true) {
                    currentDialog?.dismiss()
                }
            } catch (_: Throwable) {}
            currentDialog = null
        }
    }
}
