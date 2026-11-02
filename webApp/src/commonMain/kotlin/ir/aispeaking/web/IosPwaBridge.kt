@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

@JsFun("() => !!(window.aiSpeakingIosPwa && window.aiSpeakingIosPwa.shouldShowA2hsBanner())")
private external fun jsShouldShowIosA2hsBanner(): Boolean

@JsFun("() => window.aiSpeakingIosPwa ? window.aiSpeakingIosPwa.dismissA2hsBanner() : null")
private external fun jsDismissIosA2hsBanner()

@JsFun("() => !!(window.aiSpeakingIosPwa && window.aiSpeakingIosPwa.shouldShowChromeBanner())")
private external fun jsShouldShowIosChromeBanner(): Boolean

@JsFun("() => window.aiSpeakingIosPwa ? window.aiSpeakingIosPwa.dismissChromeBanner() : null")
private external fun jsDismissIosChromeBanner()

@JsFun("() => !!(window.aiSpeakingIosPwa && window.aiSpeakingIosPwa.isInAppBrowser())")
private external fun jsIsInAppBrowser(): Boolean

@JsFun("() => !!(window.aiSpeakingIosPwa && window.aiSpeakingIosPwa.copyCurrentUrl())")
private external fun jsCopyCurrentUrl(): Boolean

fun shouldShowIosA2hsBanner(): Boolean {
    return try {
        jsShouldShowIosA2hsBanner()
    } catch (_: Throwable) {
        false
    }
}

fun dismissIosA2hsBanner() {
    try {
        jsDismissIosA2hsBanner()
    } catch (_: Throwable) {}
}

fun shouldShowIosChromeBanner(): Boolean {
    return try {
        jsShouldShowIosChromeBanner()
    } catch (_: Throwable) {
        false
    }
}

fun dismissIosChromeBanner() {
    try {
        jsDismissIosChromeBanner()
    } catch (_: Throwable) {}
}

fun isUserInAppBrowser(): Boolean {
    return try {
        jsIsInAppBrowser()
    } catch (_: Throwable) {
        false
    }
}

fun copyCurrentUrlToClipboard(): Boolean {
    return try {
        jsCopyCurrentUrl()
    } catch (_: Throwable) {
        false
    }
}
