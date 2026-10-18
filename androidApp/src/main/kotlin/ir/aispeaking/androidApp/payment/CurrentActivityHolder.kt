package ir.aispeaking.androidApp.payment

import android.app.Activity
import java.lang.ref.WeakReference

object CurrentActivityHolder {
    private var activityRef: WeakReference<Activity>? = null

    var currentActivity: Activity?
        get() = activityRef?.get()
        set(value) {
            activityRef = if (value != null) WeakReference(value) else null
        }
}
