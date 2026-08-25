package ir.aispeaking.sharedui.ui.core.notif


import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.them.AppTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun LevelUpOverlayNotification(
    trigger: Boolean,
    onDismiss: () -> Unit,
) {
    if (!trigger) return // اگر trigger false باشه، چیزی نمایش نده

    // State برای کنترل مراحل انیمیشن
    var showIcon by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var dismiss by remember { mutableStateOf(false) }

    // Animatable برای موقعیت عمودی (slide down/up)
    val verticalOffset = remember { Animatable(-100f) } // شروع از بالای صفحه (منفی برای خارج از صفحه)

    // Coroutine برای sequence انیمیشن
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(trigger) {
        if (trigger) {
            showIcon = true
            // مرحله 1: slide down آیکون
            verticalOffset.animateTo(0f, animationSpec = tween(500)) // 0.5 ثانیه ورود

            // مرحله 2: 1 ثانیه توقف
            delay(1000)

            // مرحله 3: نمایش متن‌ها
            showText = true

            // مرحله 4: بعد از 5 ثانیه، شروع dismiss
            delay(5000)
            dismiss = true
        }
    }

    // انیمیشن dismiss (جمع شدن متن، slide up آیکون)
    LaunchedEffect(dismiss) {
        if (dismiss) {
            // اول متن‌ها محو بشن
            showText = false
            delay(500) // زمان برای fade out متن

            // بعد slide up آیکون و محو شدن
            coroutineScope.launch {
                verticalOffset.animateTo(-100f, animationSpec = tween(500))
            }.join() // صبر تا پایان انیمیشن

            showIcon = false
            onDismiss() // فراخوانی callback برای ریست trigger
        }
    }

    // UI اصلی
    if (showIcon) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = verticalOffset.value.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .background(AppTheme.colors.onPrimaryContainer, shape = AppTheme.shapes.roundLarge),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    // TODO replace icon for star
                    painter = painterResource(Res.drawable.ic_done),
                    contentDescription = "Gift Icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                AnimatedVisibility(
                    visible = showText,
                    enter = fadeIn(tween(500)) + slideInHorizontally(tween(500)) { -it },
                    exit = fadeOut(tween(500)) + slideOutHorizontally(tween(500)) { -it }
                ) {
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Congratulations",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your Level is Up!",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}