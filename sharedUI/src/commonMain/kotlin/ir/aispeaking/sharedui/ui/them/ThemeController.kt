package ir.aispeaking.sharedui.ui.them

import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global reactive controller for app-wide theme mode (System, Dark, Light).
 * Backed by StateFlow so any composable or platform window immediately updates upon change.
 */
object ThemeController {
    private val _themeMode = MutableStateFlow<ThemeMode>(ThemeMode.System)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private var onSaveCallback: ((String) -> Unit)? = null

    fun initialize(savedKey: String, onSave: ((String) -> Unit)? = null) {
        _themeMode.value = ThemeMode.mapper(savedKey)
        if (onSave != null) {
            onSaveCallback = onSave
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        onSaveCallback?.invoke(mode.key)
    }
}
