@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

@JsFun("() => !!(window.aiSpeakingIosPwa && window.aiSpeakingIosPwa.shouldShowBanner())")
private external fun jsShouldShowIosPwaBanner(): Boolean

@JsFun("() => window.aiSpeakingIosPwa ? window.aiSpeakingIosPwa.dismissBanner() : null")
private external fun jsDismissIosPwaBanner()

@JsFun("() => !!(window.aiSpeakingIosPwa && window.aiSpeakingIosPwa.isInAppBrowser())")
private external fun jsIsInAppBrowser(): Boolean

fun shouldShowIosPwaBanner(): Boolean {
    return try {
        jsShouldShowIosPwaBanner()
    } catch (_: Throwable) {
        false
    }
}

fun dismissIosPwaBanner() {
    try {
        jsDismissIosPwaBanner()
    } catch (_: Throwable) {}
}

fun isUserInAppBrowser(): Boolean {
    return try {
        jsIsInAppBrowser()
    } catch (_: Throwable) {
        false
    }
}
