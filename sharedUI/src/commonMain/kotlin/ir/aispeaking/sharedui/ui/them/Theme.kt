package ir.aispeaking.sharedui.ui.them

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

object AppTheme {
    val colors: AppColor
        @Composable
        @ReadOnlyComposable
        get() = LocalColor.current

    val dimensions: AppDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current

    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
}

private val LightColorScheme = AppColor(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceDim = SurfaceDimLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    shadow = ShadowLight,

    // رنگ‌های کاستوم
    aiChatContainer = AiChatContainerLight,
    userChatContainer = UserChatContainerLight,
    points = PointsColor,
    roadMapSurface = Color(0xFF003543)
)

private val DarkColorScheme = AppColor(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceDim = SurfaceDimDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    shadow = ShadowDark,

    aiChatContainer = AiChatContainerDark,
    userChatContainer = UserChatContainerDark,
    points = PointsColor,
    roadMapSurface = Color(0xFF003543)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = rememberAppTypography()

    val baseStyle = androidx.compose.ui.text.TextStyle(fontFamily = typography.persianRegular)
    val boldStyle = androidx.compose.ui.text.TextStyle(fontFamily = typography.persianBold)

    val m3Typography = androidx.compose.material3.Typography(
        displayLarge = boldStyle,
        displayMedium = boldStyle,
        displaySmall = boldStyle,
        headlineLarge = boldStyle,
        headlineMedium = boldStyle,
        headlineSmall = boldStyle,
        titleLarge = boldStyle,
        titleMedium = boldStyle,
        titleSmall = boldStyle,
        bodyLarge = baseStyle,
        bodyMedium = baseStyle,
        bodySmall = baseStyle,
        labelLarge = boldStyle,
        labelMedium = baseStyle,
        labelSmall = baseStyle
    )

    CompositionLocalProvider(
        LocalColor provides colorScheme,
        LocalDimensions provides AppDimensions(),
        LocalShapes provides AppShapes(),
        LocalTypography provides typography,
        androidx.compose.material3.LocalTextStyle provides baseStyle
    ) {
        androidx.compose.material3.MaterialTheme(
            typography = m3Typography
        ) {
            content()
        }
    }

}