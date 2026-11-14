package ir.aispeaking.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.domain.model.stt.SttConnectionStatus
import ir.aispeaking.domain.model.stt.SttEngine
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_clear
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_microphone
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ic_setting
import ir.aispeaking.sharedui.ui.core.loading.AppLoadingIndicator
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Speech-to-Text Settings Bottom Sheet.
 *
 * Allows switching between:
 * - App Smart Voice Recognition (default, via real-time WebSocket backend)
 * - Browser/Platform Native Speech Recognition (only presented if browser/device supports it)
 */
@Composable
fun ChatSettingsSheet(
    visible: Boolean,
    currentEngine: SttEngine,
    connectionStatus: SttConnectionStatus,
    hasBrowserSupport: Boolean,
    onSelectEngine: (SttEngine) -> Unit,
    onReconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    GameSheet(visible = visible, onDismiss = onDismiss) {
        val isDark = AppTheme.isDark

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Icon badge & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GameIconButton(
                    icon = Res.drawable.ic_clear,
                    onClick = onDismiss,
                    contentDescription = "بستن",
                    size = 36.dp,
                    iconSize = 16.dp,
                    container = if (isDark) Color(0x18FFFFFF) else Color(0x1A000000)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameText(
                        text = "تنظیمات صوتی و مکالمه",
                        size = 16.sp,
                        bold = true
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.15f))
                            .border(1.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_setting),
                            contentDescription = null,
                            tint = AppleChatTheme.PrimaryAzure,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(36.dp))
            }

            GameText(
                text = "موتور تبدیل گفتار به متن (Speech-to-Text) را متناسب با کیفیت اتصال خود تنظیم کنید.",
                size = 12.sp,
                lineHeight = 18.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            // Section 1: App STT (Default & Recommended)
            SttOptionCard(
                title = "سرویس صوتی هوشمند اپلیکیشن",
                subtitle = "پردازش بلادرنگ و دقیق با هوش مصنوعی اختصاصی",
                badgeText = "پیشنهادی",
                isSelected = currentEngine == SttEngine.APP,
                isRecommended = true,
                onClick = { onSelectEngine(SttEngine.APP) },
                statusContent = {
                    if (currentEngine == SttEngine.APP) {
                        AppConnectionStatusBadge(
                            status = connectionStatus,
                            onReconnect = onReconnect
                        )
                    }
                }
            )

            // Section 2: Browser STT (Conditionally shown ONLY if browser supports it)
            if (hasBrowserSupport) {
                SttOptionCard(
                    title = "تشخیص گفتار مرورگر / دستگاه",
                    subtitle = "استفاده از سیستم داخلی مرورگر برای تبدیل گفتار (نیازمند پشتیبانی مرورگر)",
                    badgeText = null,
                    isSelected = currentEngine == SttEngine.BROWSER,
                    isRecommended = false,
                    onClick = { onSelectEngine(SttEngine.BROWSER) },
                    statusContent = {
                        if (currentEngine == SttEngine.BROWSER) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(AppleChatTheme.SuccessCyan, CircleShape)
                                )
                                GameText(
                                    text = "فعال • آماده تبدیل صدا",
                                    size = 11.sp,
                                    color = AppleChatTheme.SuccessCyan
                                )
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Button
            GameButton(
                text = "تایید و بازگشت",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Primary,
                height = 46.dp,
                textSize = 14.sp
            )
        }
    }
}

/**
 * Visual card for an STT Provider option.
 */
@Composable
private fun SttOptionCard(
    title: String,
    subtitle: String,
    badgeText: String?,
    isSelected: Boolean,
    isRecommended: Boolean,
    onClick: () -> Unit,
    statusContent: @Composable () -> Unit = {}
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(18.dp)
    val borderColor = when {
        isSelected -> AppleChatTheme.PrimaryAzure.copy(alpha = 0.75f)
        else -> AppleChatTheme.SpecularBorderColor
    }
    val bgColor = when {
        isSelected -> if (isDark) Color(0x33007AFF) else Color(0x1F007AFF)
        else -> AppleChatTheme.GlassPanelRaised
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .border(1.2.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Radio indicator
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) AppleChatTheme.PrimaryAzure else Color.Transparent
                        )
                        .border(
                            1.5.dp,
                            if (isSelected) AppleChatTheme.PrimaryAzure else Game.TextMuted,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_done),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.18f))
                                .border(0.8.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            GameText(
                                text = badgeText,
                                size = 10.sp,
                                bold = true,
                                color = AppleChatTheme.PrimaryAzure
                            )
                        }
                    }

                    GameText(
                        text = title,
                        size = 14.sp,
                        bold = true,
                        color = if (isSelected) AppleChatTheme.PrimaryAzure else Game.TextPrimary
                    )
                }
            }

            GameText(
                text = subtitle,
                size = 11.sp,
                lineHeight = 16.sp,
                color = Game.TextSecondary
            )

            statusContent()
        }
    }
}

/**
 * Micro-badge indicating real-time WebSocket connection state in Settings.
 */
@Composable
private fun AppConnectionStatusBadge(
    status: SttConnectionStatus,
    onReconnect: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "badge_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "status_dot_alpha"
    )

    when (status) {
        SttConnectionStatus.CONNECTED -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(AppleChatTheme.SuccessCyan, CircleShape)
                )
                GameText(
                    text = "ارتباط با سرور صوتی: برقرار و آماده",
                    size = 11.sp,
                    color = AppleChatTheme.SuccessCyan
                )
            }
        }

        SttConnectionStatus.CONNECTING -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                AppLoadingIndicator(
                    size = 12.dp,
                    color = AppleChatTheme.PrimaryAzure
                )
                GameText(
                    text = "در حال اتصال به سرویس صوتی…",
                    size = 11.sp,
                    color = AppleChatTheme.PrimaryAzure
                )
            }
        }

        SttConnectionStatus.DISCONNECTED, SttConnectionStatus.ERROR -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .graphicsLayer { this.alpha = alpha }
                            .background(AppleChatTheme.Coral, CircleShape)
                    )
                    GameText(
                        text = "عدم ارتباط با سرور صوتی",
                        size = 11.sp,
                        color = AppleChatTheme.Coral
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleChatTheme.Coral.copy(alpha = 0.12f))
                        .border(0.8.dp, AppleChatTheme.Coral.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onReconnect)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = "تلاش مجدد",
                        tint = AppleChatTheme.Coral,
                        modifier = Modifier.size(12.dp)
                    )
                    GameText(
                        text = "تلاش مجدد",
                        size = 11.sp,
                        bold = true,
                        color = AppleChatTheme.Coral
                    )
                }
            }
        }
    }
}
