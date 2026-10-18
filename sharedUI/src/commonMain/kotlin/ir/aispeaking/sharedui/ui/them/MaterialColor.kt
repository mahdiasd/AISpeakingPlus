package ir.aispeaking.sharedui.ui.them

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

fun createMaterialColorScheme(appColor: AppColor, isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = appColor.primary,
            onPrimary = appColor.onPrimary,
            primaryContainer = appColor.primaryContainer,
            onPrimaryContainer = appColor.onPrimaryContainer,
            secondary = appColor.secondary,
            onSecondary = appColor.onSecondary,
            secondaryContainer = appColor.secondaryContainer,
            onSecondaryContainer = appColor.onSecondaryContainer,
            tertiary = appColor.tertiary,
            onTertiary = appColor.onTertiary,
            surface = appColor.surface,
            onSurface = appColor.onSurface,
            surfaceVariant = appColor.surfaceContainerHigh,
            onSurfaceVariant = appColor.onSurface,
            background = appColor.surfaceDim,
            onBackground = appColor.onSurface,
            error = appColor.error,
            onError = appColor.onError,
            outline = appColor.outline,
            outlineVariant = appColor.outlineVariant,
        )
    } else {
        lightColorScheme(
            primary = appColor.primary,
            onPrimary = appColor.onPrimary,
            primaryContainer = appColor.primaryContainer,
            onPrimaryContainer = appColor.onPrimaryContainer,
            secondary = appColor.secondary,
            onSecondary = appColor.onSecondary,
            secondaryContainer = appColor.secondaryContainer,
            onSecondaryContainer = appColor.onSecondaryContainer,
            tertiary = appColor.tertiary,
            onTertiary = appColor.onTertiary,
            surface = appColor.surface,
            onSurface = appColor.onSurface,
            surfaceVariant = appColor.surfaceContainerHigh,
            onSurfaceVariant = appColor.onSurface,
            background = appColor.surfaceDim,
            onBackground = appColor.onSurface,
            error = appColor.error,
            onError = appColor.onError,
            outline = appColor.outline,
            outlineVariant = appColor.outlineVariant,
        )
    }
}
