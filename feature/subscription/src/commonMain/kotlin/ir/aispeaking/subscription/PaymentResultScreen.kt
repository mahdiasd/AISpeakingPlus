package ir.aispeaking.subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_alert_circle
import ir.aispeaking.sharedui.ic_checked
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_copy
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_payment
import ir.aispeaking.sharedui.ic_profile
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ic_roadmap
import ir.aispeaking.sharedui.ic_support
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.dialog.SupportDialog
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * Modern Apple Liquid Glass Payment Result Screen.
 * Provides two beautifully crafted visual states for payment outcomes:
 * 1. Success state: celebratory emerald & gold aura, active subscription details, feature list, and action buttons.
 * 2. Failed / Cancelled state: reassuring warm rose & amber aura, error explanation, support channel trigger, and retry.
 */
@Composable
fun PaymentResultScreen(
    isSuccess: Boolean,
    refId: String? = null,
    planTitle: String? = null,
    orderId: String? = null,
    errorMessage: String? = null,
    market: String? = null,
    onNavigateHome: () -> Unit,
    onRetryPayment: () -> Unit,
    onNavigateToProfile: () -> Unit = onNavigateHome,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    // Pulsing aura animation
    val infiniteTransition = rememberInfiniteTransition(label = "auraTransition")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val entranceScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "entranceScale"
    )

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2500)
            isCopied = false
        }
    }

    val displayTrackingCode = remember(refId, orderId) {
        refId?.takeIf { it.isNotBlank() } ?: orderId?.takeIf { it.isNotBlank() }
    }

    val marketNameFa = remember(market) {
        when {
            market?.contains("BAZAAR", ignoreCase = true) == true -> "کافه بازار"
            market?.contains("MYKET", ignoreCase = true) == true -> "مایکت"
            else -> "درگاه مستقیم شاپرک / زرین‌پال"
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF080A10))
        ) {
            // Ambient Radial Gradient at top
            val ambientColors = if (isSuccess) {
                listOf(
                    Color(0xFF10B981).copy(alpha = 0.24f),
                    Color(0xFF059669).copy(alpha = 0.08f),
                    Color.Transparent
                )
            } else {
                listOf(
                    Color(0xFFF43F5E).copy(alpha = 0.24f),
                    Color(0xFFBE123C).copy(alpha = 0.08f),
                    Color.Transparent
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(Brush.radialGradient(ambientColors))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top App Bar: Back Button (pointing right in RTL) and Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppBackButton(
                        onClick = onNavigateHome,
                        modifier = Modifier.size(38.dp)
                    )

                    GameText(
                        text = "وضعیت پرداخت",
                        size = 17.sp,
                        bold = true,
                        align = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    // Spacer balancing the back button for symmetrical centering
                    Spacer(modifier = Modifier.size(38.dp))
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .graphicsLayer {
                            scaleX = entranceScale
                            scaleY = entranceScale
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSuccess) {
                        SuccessStateContent(
                            planTitle = planTitle,
                            displayTrackingCode = displayTrackingCode,
                            marketNameFa = marketNameFa,
                            auraScale = auraScale,
                            isCopied = isCopied,
                            onCopyTrackingCode = {
                                if (!displayTrackingCode.isNullOrBlank()) {
                                    clipboardManager.setText(AnnotatedString(displayTrackingCode))
                                    isCopied = true
                                }
                            },
                            onNavigateHome = onNavigateHome,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    } else {
                        FailedStateContent(
                            planTitle = planTitle,
                            displayTrackingCode = displayTrackingCode,
                            errorMessage = errorMessage,
                            marketNameFa = marketNameFa,
                            auraScale = auraScale,
                            isCopied = isCopied,
                            onCopyTrackingCode = {
                                if (!displayTrackingCode.isNullOrBlank()) {
                                    clipboardManager.setText(AnnotatedString(displayTrackingCode))
                                    isCopied = true
                                }
                            },
                            onRetryPayment = onRetryPayment,
                            onOpenSupport = { showSupportDialog = true },
                            onNavigateHome = onNavigateHome
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            // Support Dialog overlay when requested by user
            if (showSupportDialog) {
                SupportDialog(
                    visible = true,
                    onDismiss = { showSupportDialog = false }
                )
            }
        }
    }
}

/**
 * Celebratory state layout for successful payments.
 */
@Composable
private fun SuccessStateContent(
    planTitle: String?,
    displayTrackingCode: String?,
    marketNameFa: String,
    auraScale: Float,
    isCopied: Boolean,
    onCopyTrackingCode: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Badge with Aura
        Box(
            modifier = Modifier.size(104.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient outer pulsing ring
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = auraScale
                        scaleY = auraScale
                    }
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.18f))
            )

            // Inner glass squircle badge
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF10B981).copy(alpha = 0.35f),
                                Color(0xFF047857).copy(alpha = 0.20f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF34D399),
                                Color(0xFF10B981).copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_checked),
                    contentDescription = "پرداخت موفق",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // VIP Pill Badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF59E0B).copy(alpha = 0.14f))
                .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_crown),
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(16.dp)
            )
            GameText(
                text = "اشتراک ویژه فعال شد",
                size = 13.sp,
                bold = true,
                color = Color(0xFFFBBF24)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title & Celebration Description
        GameText(
            text = "پرداخت با موفقیت انجام شد!",
            size = 22.sp,
            bold = true,
            color = Color.White,
            align = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        GameText(
            text = "به جمع کاربران ویژه AI Speaking خوش آمدید. حساب کاربری شما با موفقیت ارتقا یافت و هم‌اکنون به تمامی مراحل، مکالمات هوش مصنوعی و تصحیح هوشمند دسترسی دارید.",
            size = 14.sp,
            color = Color(0xFF94A3B8),
            align = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Receipt Card (Apple Liquid Glass)
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameText(
                        text = "رسید تراکنش",
                        size = 14.sp,
                        bold = true,
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        GameText(
                            text = "موفق و تایید شده",
                            size = 12.sp,
                            bold = true,
                            color = Color(0xFF34D399)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // Package / Plan
                ReceiptRow(
                    label = "بسته خریداری شده",
                    value = planTitle ?: "اشتراک ویژه ۳ ماهه"
                )

                // Tracking / Reference code
                if (!displayTrackingCode.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameText(
                            text = "شناسه پیگیری",
                            size = 13.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameText(
                                text = displayTrackingCode,
                                size = 13.sp,
                                bold = true,
                                color = Color.White,
                                latin = true
                            )

                            // Copy button with feedback
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable(onClick = onCopyTrackingCode)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_copy),
                                        contentDescription = "کپی",
                                        tint = if (isCopied) Color(0xFF34D399) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    if (isCopied) {
                                        GameText(
                                            text = "کپی شد",
                                            size = 11.sp,
                                            bold = true,
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Payment Gateway / Market
                ReceiptRow(
                    label = "روش پرداخت",
                    value = marketNameFa
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Unlocked Features Mini Card
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.40f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureRowItem(text = "دسترسی نامحدود به تمامی ۳۰ مرحله سناریوی لندن")
                FeatureRowItem(text = "پارتنر مکالمه صوتی بلادرنگ با هوش مصنوعی")
                FeatureRowItem(text = "تحلیل گرامر، نمره‌دهی و فیدبک فارسی اختصاصی")
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action CTA Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameButton(
                text = "شروع مکالمه و ورود به نقشه",
                style = GameButtonStyle.Gold,
                icon = Res.drawable.ic_roadmap,
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp,
                textSize = 15.sp
            )

            GameButton(
                text = "مشاهده جزئیات در پروفایل",
                style = GameButtonStyle.Glass,
                icon = Res.drawable.ic_profile,
                onClick = onNavigateToProfile,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp,
                textSize = 14.sp
            )
        }
    }
}

/**
 * Reassuring state layout for failed or cancelled payments.
 */
@Composable
private fun FailedStateContent(
    planTitle: String?,
    displayTrackingCode: String?,
    errorMessage: String?,
    marketNameFa: String,
    auraScale: Float,
    isCopied: Boolean,
    onCopyTrackingCode: () -> Unit,
    onRetryPayment: () -> Unit,
    onOpenSupport: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Badge with Warning Aura
        Box(
            modifier = Modifier.size(104.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = auraScale
                        scaleY = auraScale
                    }
                    .clip(CircleShape)
                    .background(Color(0xFFF43F5E).copy(alpha = 0.18f))
            )

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFF43F5E).copy(alpha = 0.35f),
                                Color(0xFF9F1239).copy(alpha = 0.20f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFFFB7185),
                                Color(0xFFF43F5E).copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_alert_circle),
                    contentDescription = "پرداخت ناموفق",
                    tint = Color(0xFFFB7185),
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Warning Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE11D48).copy(alpha = 0.14f))
                .border(1.dp, Color(0xFFFB7185).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFB7185))
            )
            GameText(
                text = "تراکنش ناموفق",
                size = 13.sp,
                bold = true,
                color = Color(0xFFFB7185)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        GameText(
            text = "پرداخت انجام نشد",
            size = 22.sp,
            bold = true,
            color = Color.White,
            align = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        GameText(
            text = "فرآیند پرداخت تکمیل نشد یا توسط شما لغو گردید. در صورتی که مبلغی از حسابتان کسر شده است، طبق ضوابط بانکی حداکثر تا ۷۲ ساعت آینده خودکار برگشت داده خواهد شد.",
            size = 14.sp,
            color = Color(0xFF94A3B8),
            align = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Details Card
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameText(
                        text = "جزئیات تراکنش",
                        size = 14.sp,
                        bold = true,
                        color = Color.White
                    )

                    GameText(
                        text = "لغو شده / ناموفق",
                        size = 12.sp,
                        bold = true,
                        color = Color(0xFFFB7185)
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                if (!errorMessage.isNullOrBlank()) {
                    ReceiptRow(
                        label = "علت خطا",
                        value = errorMessage,
                        valueColor = Color(0xFFFDA4AF)
                    )
                }

                ReceiptRow(
                    label = "پلن درخواستی",
                    value = planTitle ?: "اشتراک ویژه"
                )

                ReceiptRow(
                    label = "درگاه پرداخت",
                    value = marketNameFa
                )

                if (!displayTrackingCode.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameText(
                            text = "شناسه پیگیری",
                            size = 13.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameText(
                                text = displayTrackingCode,
                                size = 13.sp,
                                bold = true,
                                color = Color.White,
                                latin = true
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable(onClick = onCopyTrackingCode)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_copy),
                                        contentDescription = "کپی",
                                        tint = if (isCopied) Color(0xFF34D399) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    if (isCopied) {
                                        GameText(
                                            text = "کپی شد",
                                            size = 11.sp,
                                            bold = true,
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons Cluster
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Retry Payment CTA
            GameButton(
                text = "تلاش مجدد و مشاهده پلن‌ها",
                style = GameButtonStyle.Primary,
                icon = Res.drawable.ic_refresh,
                onClick = onRetryPayment,
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp,
                textSize = 15.sp
            )

            // Support CTA (Prompt Requirement)
            GameButton(
                text = "ارتباط با پشتیبانی",
                style = GameButtonStyle.Glass,
                icon = Res.drawable.ic_support,
                onClick = onOpenSupport,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp,
                textSize = 14.sp
            )

            // Home Navigation CTA (Prompt Requirement)
            GameButton(
                text = "بازگشت به صفحه اصلی",
                style = GameButtonStyle.Glass,
                icon = Res.drawable.ic_roadmap,
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp,
                textSize = 14.sp
            )
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameText(
            text = label,
            size = 13.sp,
            color = Color(0xFF94A3B8)
        )

        GameText(
            text = value,
            size = 13.sp,
            bold = true,
            color = valueColor
        )
    }
}

@Composable
private fun FeatureRowItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_checked),
                contentDescription = null,
                tint = Color(0xFF34D399),
                modifier = Modifier.size(12.dp)
            )
        }

        GameText(
            text = text,
            size = 13.sp,
            color = Color(0xFFE2E8F0)
        )
    }
}

@Composable
private fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF141A29).copy(alpha = 0.65f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}
