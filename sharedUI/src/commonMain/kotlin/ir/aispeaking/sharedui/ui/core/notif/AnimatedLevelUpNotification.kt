package ir.aispeaking.sharedui.ui.core.notif


import ir.aispeaking.sharedui.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun AnimatedLevelUpNotification(
    isVisible: Boolean,
    onDismiss: () -> Unit,
) {
    var showContent by remember { mutableStateOf(false) }

    // این LaunchedEffect زمانی اجرا می‌شود که isVisible به true تبدیل شود.
    // از آن برای کنترل زمان‌بندی انیمیشن‌ها و بستن بنر استفاده می‌کنیم.
    LaunchedEffect(key1 = isVisible) {
        if (isVisible) {
            showContent = false
            delay(300) // کمی تاخیر قبل از شروع انیمیشن ورود آیکون
            showContent = true
            delay(1000L) // 1 ثانیه نمایش آیکون
            delay(4000L) // 4 ثانیه نمایش محتوا
            showContent = false
            delay(300) // کمی تاخیر قبل از شروع انیمیشن خروج
            onDismiss()
        }
    }

    // AnimatedVisibility برای انیمیشن ورود و خروج کل بنر
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 500)
        ) + fadeIn(animationSpec = tween(durationMillis = 500)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 500)
        ) + fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // پس زمینه آبی روشن
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = scaleIn(animationSpec = tween(durationMillis = 300)),
                    exit = scaleOut(animationSpec = tween(durationMillis = 300))
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_done),
                        contentDescription = "Level Up",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                    ) {
                        Text(
                            text = "Congregation",
                            color = Color(0xFF1976D2),
                            fontSize = 14.sp
                        )
                    }
                    AnimatedVisibility(
                        visible = showContent,
                        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    ) {
                        Text(
                            text = "Your Level is Up !",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3), // آبی روشن تر
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAnimatedLevelUpNotification() {
    var isVisible by remember { mutableStateOf(false) }
    Column {
        Button(onClick = { isVisible = true }) {
            Text("Show Notification")
        }
        AnimatedLevelUpNotification(isVisible = isVisible) {
            isVisible = false
        }
    }
}