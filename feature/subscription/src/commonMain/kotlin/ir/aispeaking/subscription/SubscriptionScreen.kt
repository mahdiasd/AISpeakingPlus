package ir.aispeaking.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.subscription.component.PlanSelectionCard
import ir.aispeaking.subscription.component.PromoCodeInputRow
import ir.aispeaking.subscription.component.SubscriptionBenefitsList
import ir.aispeaking.subscription.component.SubscriptionHeader
import ir.aispeaking.subscription.component.TrustBadgesRow
import kotlinx.coroutines.delay

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit,
    onSubscriptionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SubscriptionEffect.NavigateBack -> onNavigateBack()
                is SubscriptionEffect.SubscriptionActivated -> onSubscriptionSuccess()
                is SubscriptionEffect.ShowSnackbar -> toast = effect.message
            }
        }
    }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(3500)
            toast = null
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Game.FallbackBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GameIconButton(
                        icon = Res.drawable.ic_back,
                        onClick = onNavigateBack,
                        contentDescription = "بازگشت"
                    )
                    GameText(
                        text = "اشتراک ویژه",
                        size = 18.sp,
                        bold = true,
                        align = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(44.dp))
                }

                if (uiState.isLoading && uiState.plans.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Game.Gold, strokeWidth = 3.dp)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SubscriptionHeader(currentStatus = uiState.currentStatus)

                        uiState.plans.forEach { plan ->
                            val isSelected = plan.id == uiState.selectedPlanId
                            val finalPrice = uiState.calculateFinalPrice(plan)
                            PlanSelectionCard(
                                plan = plan,
                                finalPriceTomans = finalPrice,
                                isSelected = isSelected,
                                onClick = {
                                    viewModel.processIntent(SubscriptionIntent.SelectPlan(plan.id))
                                }
                            )
                        }

                        PromoCodeInputRow(
                            promoCode = uiState.promoCodeInput,
                            onPromoCodeChanged = {
                                viewModel.processIntent(SubscriptionIntent.OnPromoCodeChanged(it))
                            },
                            onApplyClicked = {
                                viewModel.processIntent(SubscriptionIntent.ApplyPromoCode)
                            },
                            feedbackMessage = uiState.promoFeedbackMessage,
                            isError = uiState.isPromoError
                        )

                        SubscriptionBenefitsList()
                        TrustBadgesRow()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Sticky purchase bar
                val dockShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(dockShape)
                        .background(Game.Panel)
                        .border(1.dp, Game.Stroke, dockShape)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val selectedPlan = uiState.selectedPlan
                    val finalPrice = selectedPlan?.let { uiState.calculateFinalPrice(it) } ?: 0L

                    if (finalPrice > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GameText(text = "مبلغ قابل پرداخت", size = 13.sp, color = Game.TextSecondary)
                            GameText(
                                text = "${formatToPersian(finalPrice)} تومان",
                                size = 16.sp,
                                bold = true,
                                color = Game.Gold
                            )
                        }
                    }
                    GameButton(
                        text = "خرید اشتراک و شروع یادگیری",
                        onClick = { viewModel.processIntent(SubscriptionIntent.PurchaseSelectedPlan) },
                        modifier = Modifier.fillMaxWidth(),
                        style = GameButtonStyle.Gold,
                        icon = Res.drawable.ic_crown,
                        enabled = !uiState.isPurchasing && !uiState.isLoading,
                        loading = uiState.isPurchasing
                    )
                }
            }

            toast?.let { message ->
                GlassPanel(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Game.PanelSolid,
                    border = Game.Sky.copy(alpha = 0.6f)
                ) {
                    GameText(
                        text = message,
                        size = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

private fun formatToPersian(number: Long): String {
    val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val formatted = number.toString().reversed().chunked(3).joinToString(",").reversed()
    return formatted.map { char ->
        if (char in '0'..'9') persianDigits[char - '0'] else char
    }.joinToString("")
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun SubscriptionScreenPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Game.Ink),
            contentAlignment = Alignment.Center
        ) {
            GameText(text = "پیش‌نمایش صفحه ارتقا و اشتراک ویژه", size = 16.sp)
        }
    }
}
