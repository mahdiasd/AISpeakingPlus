package ir.aispeaking.sharedui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ir.aispeaking.sharedui.ui.them.AppTheme

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(false) {

}
