package ir.aispeaking.subscription

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.sharedui.ui.core.loading.AppLoadingIndicator
import ir.aispeaking.sharedui.ui.dialog.RegisterBottomSheet
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.subscription.component.ModernPlanCard
import ir.aispeaking.subscription.component.ModernPromoRow
import ir.aispeaking.subscription.component.ModernSubscriptionHeader
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

private val defaultFallbackPlans = listOf(
    SubscriptionPlan(
        id = "plan-1m",
        type = "1_MONTH",
        titleFa = "اشتراک ۱ ماهه",
        durationDays = 30,
        priceTomans = 65_000L,
        originalPriceTomans = 119_000L,
        discountPercent = 45,
        badge = null
    ),
    SubscriptionPlan(
        id = "plan-3m",
        type = "3_MONTHS",
        titleFa = "اشتراک ۳ ماهه ویژه",
        durationDays = 90,
        priceTomans = 115_000L,
        originalPriceTomans = 299_000L,
        discountPercent = 62,
        badge = "محبوب‌ترین"
    ),
    SubscriptionPlan(
        id = "plan-6m",
        type = "6_MONTHS",
        titleFa = "اشتراک ۶ ماهه طلایی",
        durationDays = 180,
        priceTomans = 200_000L,
        originalPriceTomans = 499_000L,
        discountPercent = 60,
        badge = "بهترین ارزش"
    )
)

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit,
    onSubscriptionSuccess: () -> Unit,
    onNavigateToPaymentResult: (isSuccess: Boolean, refId: String?, planTitle: String?, orderId: String?, errorMessage: String?, market: String?) -> Unit = { _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var toast by remember { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current
    val isDark = AppTheme.isDark

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SubscriptionEffect.NavigateBack -> onNavigateBack()
                is SubscriptionEffect.SubscriptionActivated -> onSubscriptionSuccess()
                is SubscriptionEffect.ShowSnackbar -> toast = effect.message
                is SubscriptionEffect.OpenWebUrl -> {
                    try {
                        uriHandler.openUri(effect.url)
                    } catch (_: Exception) {}
                }
                is SubscriptionEffect.NavigateToPaymentResult -> {
                    onNavigateToPaymentResult(
                        effect.isSuccess,
                        effect.refId,
                        effect.planTitle,
                        effect.orderId,
                        effect.errorMessage,
                        effect.market
                    )
                }
            }
        }
    }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(3500)
            toast = null
        }
    }

    val effectivePlans = if (uiState.plans.isNotEmpty()) uiState.plans else defaultFallbackPlans

    val initialIndex = remember(effectivePlans, uiState.selectedPlanId) {
        val found = effectivePlans.indexOfFirst { it.id == uiState.selectedPlanId }
        if (found >= 0) found else 1.coerceAtMost(effectivePlans.size - 1)
    }

    val pagerState = rememberPagerState(initialPage = initialIndex) { effectivePlans.size }

    // Sync pager scroll to ViewModel selectedPlanId
    LaunchedEffect(pagerState.currentPage, effectivePlans) {
        val plan = effectivePlans.getOrNull(pagerState.currentPage)
        if (plan != null && plan.id != uiState.selectedPlanId) {
            viewModel.processIntent(SubscriptionIntent.SelectPlan(plan.id))
        }
    }

    // Sync ViewModel selectedPlanId to pager scroll
    LaunchedEffect(uiState.selectedPlanId, effectivePlans) {
        val targetIndex = effectivePlans.indexOfFirst { it.id == uiState.selectedPlanId }
        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    // Dynamic ambient glow color based on active plan page
    val currentTargetColor = when (pagerState.currentPage) {
        0 -> Color(0xFFFF7A45) // Orange / Peach for 1 Month
        1 -> Color(0xFF22C55E) // Mint Green for 3 Months
        2 -> Color(0xFF0EA5E9) // Sky Blue for 6 Months
        else -> Color(0xFF22C55E)
    }

    val animatedAmbientColor by animateColorAsState(
        targetValue = currentTargetColor,
        animationSpec = tween(durationMillis = 400),
        label = "ambientGlowColor"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0B0F17) else Color(0xFFF8F9FC))
        ) {
            // Dynamic Ambient Bottom Glow (matching the image design)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.60f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                animatedAmbientColor.copy(alpha = if (isDark) 0.12f else 0.18f),
                                animatedAmbientColor.copy(alpha = if (isDark) 0.32f else 0.45f)
                            )
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Section with Back Button & Centered Title
                ModernSubscriptionHeader(
                    onNavigateBack = onNavigateBack
                )

                // Sleek Promo Code Row prominently positioned above the plan cards
                ModernPromoRow(
                    promoCode = uiState.promoCodeInput,
                    appliedDiscountPercent = uiState.appliedDiscountPercent,
                    feedbackMessage = uiState.promoFeedbackMessage,
                    isError = uiState.isPromoError,
                    onPromoCodeChanged = {
                        viewModel.processIntent(SubscriptionIntent.OnPromoCodeChanged(it))
                    },
                    onApplyClicked = {
                        viewModel.processIntent(SubscriptionIntent.ApplyPromoCode)
                    },
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )

                if (uiState.isLoading && uiState.plans.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AppLoadingIndicator(
                            color = animatedAmbientColor,
                            size = 40.dp
                        )
                    }
                } else if (uiState.errorMessage != null && uiState.plans.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            GameText(
                                text = uiState.errorMessage ?: "خطا در دریافت اطلاعات",
                                size = 14.sp,
                                color = Game.Coral,
                                align = TextAlign.Center
                            )
                            GameButton(
                                text = "تلاش مجدد",
                                onClick = { viewModel.processIntent(SubscriptionIntent.LoadData) },
                                style = GameButtonStyle.Primary
                            )
                        }
                    }
                } else {
                    // Scrollable main content area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Horizontal 3D Coverflow Pager with enhanced side card visibility
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 54.dp),
                            pageSpacing = 12.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                        ) { page ->
                            val plan = effectivePlans[page]
                            val isSelected = plan.id == uiState.selectedPlanId ||
                                    (uiState.selectedPlanId.isEmpty() && page == pagerState.currentPage)
                            val finalPrice = uiState.calculateFinalPrice(plan)

                            // Check whether this plan is currently active for the subscriber
                            val isCurrentActivePlan = remember(plan, uiState.currentStatus, effectivePlans) {
                                val status = uiState.currentStatus
                                if (status?.isSubscriber != true) false
                                else {
                                    val st = status.planType?.uppercase()?.trim().orEmpty()
                                    when {
                                        st.isNotEmpty() && (plan.type.equals(st, ignoreCase = true) || plan.id.equals(st, ignoreCase = true)) -> true
                                        st.contains("1") && (plan.type.contains("1") || plan.id.contains("1")) -> true
                                        st.contains("3") && (plan.type.contains("3") || plan.id.contains("3")) -> true
                                        st.contains("6") && (plan.type.contains("6") || plan.id.contains("6")) -> true
                                        else -> {
                                            val anyMatched = effectivePlans.any { p ->
                                                val pt = p.type.uppercase()
                                                val pid = p.id.uppercase()
                                                st.isNotEmpty() && (pt == st || pid == st || (st.contains("1") && pt.contains("1")) || (st.contains("3") && pt.contains("3")) || (st.contains("6") && pt.contains("6")))
                                            }
                                            if (!anyMatched) {
                                                plan.id.contains("3m") || plan.durationDays == 90
                                            } else false
                                        }
                                    }
                                }
                            }

                            // 3D coverflow calculation with enhanced side peek visibility
                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                            val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1.5f)

                            val scale = 1f - (absOffset * 0.08f)
                            val alpha = 1f - (absOffset * 0.18f).coerceIn(0f, 0.28f)
                            val rotationZ = -pageOffset * 3.5f
                            val rotationY = pageOffset * 7.5f

                            ModernPlanCard(
                                plan = plan,
                                finalPriceTomans = finalPrice,
                                isSelected = isSelected,
                                isPurchasing = uiState.isPurchasing,
                                purchaseProgressMessage = uiState.purchaseProgressMessage,
                                isCurrentActivePlan = isCurrentActivePlan,
                                remainingDays = uiState.currentStatus?.remainingDays ?: 0,
                                onPurchaseClick = {
                                    if (plan.id != uiState.selectedPlanId) {
                                        viewModel.processIntent(SubscriptionIntent.SelectPlan(plan.id))
                                    }
                                    viewModel.processIntent(SubscriptionIntent.PurchaseSelectedPlan)
                                },
                                onCardClick = {
                                    viewModel.processIntent(SubscriptionIntent.SelectPlan(plan.id))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                        this.rotationZ = rotationZ
                                        this.rotationY = rotationY
                                        cameraDistance = 16f * density
                                        translationY = absOffset * 4.dp.toPx()
                                    }
                            )
                        }

                        // Pager Dots Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(effectivePlans.size) { index ->
                                val isCurrent = index == pagerState.currentPage
                                val dotWidth by animateDpAsState(
                                    targetValue = if (isCurrent) 22.dp else 7.dp,
                                    animationSpec = tween(300),
                                    label = "dotWidth"
                                )
                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(dotWidth)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrent) animatedAmbientColor
                                            else if (isDark) Color(0x33FFFFFF)
                                            else Color(0x22000000)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Top floating feedback toast
            toast?.let { message ->
                GlassPanel(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF1E2638) else Color.White,
                    border = animatedAmbientColor.copy(alpha = 0.6f)
                ) {
                    GameText(
                        text = message,
                        size = 13.sp,
                        lineHeight = 20.sp,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            // Phone registration bottom sheet for guest checkouts
            RegisterBottomSheet(
                visible = uiState.showRegisterDialog,
                title = "ورود یا ثبت‌نام برای خرید اشتراک",
                subtitle = "برای فعال‌سازی اشتراک ویژه، اول شماره موبایلت رو وارد کن تا اشتراک روی حسابت فعال بشه.",
                confirmButtonText = "تایید و ادامه خرید",
                onDismiss = { viewModel.processIntent(SubscriptionIntent.DismissRegisterDialog) },
                onRegisterSuccess = {},
                isLoading = uiState.isRegisterLoading,
                step = uiState.registerStep,
                errorMessage = uiState.registerError,
                onSendOtp = { phone -> viewModel.processIntent(SubscriptionIntent.SendRegisterOtp(phone)) },
                onVerifyOtp = { phone, otp -> viewModel.processIntent(SubscriptionIntent.VerifyRegisterOtp(phone, otp)) },
                onEditPhone = { viewModel.processIntent(SubscriptionIntent.ResetRegisterStep) }
            )
        }
    }
}
