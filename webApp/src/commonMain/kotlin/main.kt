
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.ComposeViewport
import ir.aispeaking.navigation.AppNavigation
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.ui.them.AppTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0.0f to Color(0xFF393B40), // مرکز: خاکستری ملایم (Spotlight)
                        0.6f to Color(0xFF1E1F22), // میانه: تم تیره نیو یوآی
                        1.0f to Color(0xFF18191B), // لبه‌ها: عمیق و تیره
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxHeight()
            ) {
                val responsiveWidth = min(maxWidth, maxHeight * (9f / 16f))

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 16.dp)
                        .width(responsiveWidth)
                        .shadow(24.dp, AppTheme.shapes.roundLarge) // اضافه کردن سایه برای عمق
                        .clip(AppTheme.shapes.roundLarge)
                ) {
                    AppTheme(false) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
