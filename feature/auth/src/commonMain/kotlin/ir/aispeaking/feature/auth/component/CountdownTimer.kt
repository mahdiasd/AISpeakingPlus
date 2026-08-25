package ir.aispeaking.feature.auth.component


import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.retry_send_sms
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun CountdownTimer(
    initialTime: Int = 60,
    key: String,
    onRetry: () -> Unit = {}
) {
    var timeLeft by remember { mutableIntStateOf(initialTime) }

    LaunchedEffect(key) {
        timeLeft = initialTime
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    // 2. استفاده از padStart به جای .format برای سازگاری ۱۰۰٪ با KMP
    val formattedTime = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    AnimatedContent(targetState = timeLeft > 0, label = "TimerAnimation") { timerIsRunning ->
        if (timerIsRunning) {
            BodyMediumText(text = formattedTime)
        } else {
            BodyMediumText(
                modifier = Modifier.animateClickable(onRetry),
                text = stringResource(Res.string.retry_send_sms)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            CountdownTimer(key = "1")
            CountdownTimer(0, key = "2")
        }
    }
}