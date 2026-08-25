package ir.aispeaking.sharedui.ui.them


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.poppins_bold
import ir.aispeaking.sharedui.poppins_regular
import ir.aispeaking.sharedui.vazir
import ir.aispeaking.sharedui.vazir_bold
import org.jetbrains.compose.resources.Font

data class AppTypography(
    val persianRegular: FontFamily = FontFamily.Default,
    val persianBold: FontFamily = FontFamily.Default,

    val headingLarge: TextStyle = TextStyle(),
    val headingLargeBold: TextStyle = TextStyle(),
    val headingMedium: TextStyle = TextStyle(),
    val headingMediumBold: TextStyle = TextStyle(),
    val headline: TextStyle = TextStyle(),
    val headlineBold: TextStyle = TextStyle(),
    val title: TextStyle = TextStyle(),
    val titleBold: TextStyle = TextStyle(),
    val bodyLarge: TextStyle = TextStyle(),
    val bodyLargeBold: TextStyle = TextStyle(),
    val bodyMedium: TextStyle = TextStyle(),
    val bodyMediumBold: TextStyle = TextStyle(),
    val labelMedium: TextStyle = TextStyle(),
    val labelMediumBold: TextStyle = TextStyle(),
    val labelSmall: TextStyle = TextStyle(),
    val labelSmallBold: TextStyle = TextStyle()
)

val LocalTypography = staticCompositionLocalOf { AppTypography() }

// In CMP, Font() is a @Composable function, so we must instantiate Typography inside a Composable scope
@Composable
fun rememberAppTypography(): AppTypography {
    val regular = FontFamily(Font(Res.font.poppins_regular, FontWeight.Normal))
    val bold = FontFamily(Font(Res.font.poppins_bold, FontWeight.Bold))

    val persianRegular = FontFamily(Font(Res.font.vazir, FontWeight.Normal))
    val persianBold = FontFamily(Font(Res.font.vazir_bold, FontWeight.Bold))

    return remember(regular, bold) {
        AppTypography(
            headingLarge = TextStyle(
                fontFamily = regular,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp
            ),
            headingLargeBold = TextStyle(
                fontFamily = bold,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp
            ),
            headingMedium = TextStyle(
                fontFamily = regular,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp
            ),
            headingMediumBold = TextStyle(
                fontFamily = bold,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp
            ),
            headline = TextStyle(
                fontFamily = regular,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                lineHeight = 28.sp
            ),
            headlineBold = TextStyle(
                fontFamily = bold,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 28.sp
            ),
            title = TextStyle(
                fontFamily = regular,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 26.sp
            ),
            titleBold = TextStyle(
                fontFamily = bold,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 26.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = regular,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            bodyLargeBold = TextStyle(
                fontFamily = bold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = regular,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 22.sp
            ),
            bodyMediumBold = TextStyle(
                fontFamily = bold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 22.sp
            ),
            labelMedium = TextStyle(
                fontFamily = regular,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 18.sp
            ),
            labelMediumBold = TextStyle(
                fontFamily = bold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 18.sp
            ),
            labelSmall = TextStyle(
                fontFamily = regular,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = 16.sp
            ),
            labelSmallBold = TextStyle(
                fontFamily = bold,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                lineHeight = 16.sp
            )
        )
    }
}