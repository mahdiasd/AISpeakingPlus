package ir.aispeaking.chat.stt

import android.content.Context

/**
 * Holds the application [Context] for the duration of the Android process
 * so that the zero-arg `expect class AudioCapture` can reach platform APIs
 * (permission checks, `AudioRecord`).
 *
 * Must be initialised once from `Activity.onCreate` (or the Application
 * class) before the user taps the chat mic button.
 */
object AndroidAudioCapture {
    @Volatile
    var context: Context? = null
        internal set

    fun attach(context: Context) {
        // Hold the application context to avoid leaking an Activity.
        this.context = context.applicationContext
    }
}
