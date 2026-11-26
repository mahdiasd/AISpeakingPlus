@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

import ir.aispeaking.sharedui.ui.them.ThemeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@JsFun("() => !!(window.aiSpeakingTheme && window.aiSpeakingTheme.isDark())")
private external fun jsIsSystemDark(): Boolean

@JsFun("() => window.aiSpeakingTheme ? window.aiSpeakingTheme.getChangeCounter() : 0")
private external fun jsGetThemeChangeCounter(): Int

@JsFun("(isDark) => window.aiSpeakingTheme ? window.aiSpeakingTheme.syncPageBackground(isDark) : null")
private external fun jsSyncPageBackground(isDark: Boolean)

private val themeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
private var themeWatcherJob: Job? = null

fun setupWebThemeBridge() {
    var lastDark = jsIsSystemDark()
    ThemeController.setSystemDarkOverride(lastDark)

    themeWatcherJob?.cancel()
    themeWatcherJob = themeScope.launch {
        var lastCounter = jsGetThemeChangeCounter()
        while (isActive) {
            delay(250)
            val currentCounter = jsGetThemeChangeCounter()
            val currentDark = jsIsSystemDark()
            if (currentCounter != lastCounter || currentDark != lastDark) {
                lastCounter = currentCounter
                lastDark = currentDark
                ThemeController.setSystemDarkOverride(currentDark)
            }
        }
    }
}

fun syncWebPageBackground(isDark: Boolean) {
    try {
        jsSyncPageBackground(isDark)
    } catch (_: Throwable) {}
}
