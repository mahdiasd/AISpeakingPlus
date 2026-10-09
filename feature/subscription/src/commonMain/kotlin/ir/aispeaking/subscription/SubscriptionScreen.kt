package ir.aispeaking.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.subscription.component.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit,
    onSubscriptionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SubscriptionEffect.NavigateBack -> onNavigateBack()
                is SubscriptionEffect.SubscriptionActivated -> onSubscriptionSuccess()
                is SubscriptionEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFF070B19),
            modifier = modifier.fillMaxSize(),
            topBar = {
                // Top App Bar with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppBackButton(onClick = onNavigateBack)

                    Text(
                        text = "اشتراک ویژه",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Spacer to keep title centered
                    Spacer(modifier = Modifier.size(38.dp))
                }
            },
            bottomBar = {
                // Sticky Purchase Action Bar at Bottom
                Surface(
                    color = Color(0xFF0B1120),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val selectedPlan = uiState.selectedPlan
                        val finalPrice = selectedPlan?.let { uiState.calculateFinalPrice(it) } ?: 0L

                        Button(
                            onClick = {
                                viewModel.processIntent(SubscriptionIntent.PurchaseSelectedPlan)
                            },
                            enabled = !uiState.isPurchasing && !uiState.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = Color(0xFF6366F1)
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF8B5CF6),
                                            Color(0xFF6366F1),
                                            Color(0xFFEC4899)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            if (uiState.isPurchasing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "خرید اشتراک و شروع یادگیری",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (finalPrice > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "(${formatToPersian(finalPrice)} تومان)",
                                            color = Color(0xFFFFD700),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (uiState.isLoading && uiState.plans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        strokeWidth = 3.dp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Hero Header
                    SubscriptionHeader(currentStatus = uiState.currentStatus)

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Plan Cards
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

                    // 3. Promo Code Row
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4. Feature Benefits List
                    SubscriptionBenefitsList()

                    // 5. Trust and Security Badges
                    TrustBadgesRow()

                    Spacer(modifier = Modifier.height(24.dp))
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
                .background(Color(0xFF070B19)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "پیش‌نمایش صفحه ارتقا و اشتراک ویژه",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
