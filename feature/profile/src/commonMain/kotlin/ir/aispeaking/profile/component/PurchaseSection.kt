package ir.aispeaking.profile.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.profile.OnAction
import ir.aispeaking.profile.ProfileUiEvent
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.expiration_subscription
import ir.aispeaking.sharedui.ic_payment
import ir.aispeaking.sharedui.my_subscription
import ir.aispeaking.sharedui.purchase
import ir.aispeaking.sharedui.start_subscription
import ir.aispeaking.sharedui.subscription
import ir.aispeaking.sharedui.ui.core.error.ErrorContent
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.loading.PageLoading
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.animatedBorder
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.utils.time.toReadableString
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

private sealed class SubscriptionMode {
    data object Loading : SubscriptionMode()
    data object Active : SubscriptionMode()
    data object NeedToBuy : SubscriptionMode()
    data object Error : SubscriptionMode()
}

@Composable
fun PurchaseSection(
    modifier: Modifier,
    subscriptions: ImmutableList<Purchase>?,
    onAction: OnAction,
    isFetching: Boolean,
    isErrorFetching: Boolean,
) {
    val uiMode by remember(isFetching, subscriptions) {
        derivedStateOf {
            if (isFetching) SubscriptionMode.Loading
            else if (isErrorFetching) SubscriptionMode.Error
            else if (subscriptions.isNullOrEmpty()) SubscriptionMode.NeedToBuy
            else SubscriptionMode.Active
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppTheme.colors.surfaceContainerLow,
                shape = AppTheme.shapes.roundMedium
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        AnimatedContent(targetState = uiMode) {
            when (it) {
                SubscriptionMode.Active -> {
                    ActiveSubscriptionContent(
                        purchases = subscriptions!!,
                        onPurchase = { onAction(ProfileUiEvent.OnPurchaseClick) }
                    )
                }

                SubscriptionMode.Loading -> {
                    PageLoading(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(16.dp),
                        dotColor = AppTheme.colors.onSurface
                    )
                }

                SubscriptionMode.NeedToBuy -> {
                    NeedToBuyContent(onPurchase = { onAction(ProfileUiEvent.OnPurchaseClick) })
                }

                SubscriptionMode.Error -> {
                    ErrorContent(
                        iconSize = 100.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onRetry = { onAction(ProfileUiEvent.GetActiveSubscriptions) }
                    )
                }
            }
        }
    }
}

@Composable
fun NeedToBuyContent(onPurchase: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animatedBorder(
                borderColors = listOf(
                    Color(0xFFAB47BC),
                    Color(0xFF2196F3),
                    Color(0xFF7CB342),
                    AppTheme.colors.primary,
                ),
                animationDurationInMillis = 2 * 1000,
                backgroundColor = AppTheme.colors.surfaceContainerLow,
                shape = AppTheme.shapes.roundMedium,
                borderWidth = 1.5.dp,
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppIcon(
            tint = AppTheme.colors.onSurface,
            icon = Res.drawable.ic_payment,
        )

        BodyMediumText(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.subscription),
        )

        LabelMediumBoldText(
            modifier = Modifier
                .animateClickable(onPurchase)
                .background(
                    color = AppTheme.colors.primaryContainer, shape = AppTheme.shapes.roundSmall
                )
                .border(1.dp, color = AppTheme.colors.primary, shape = AppTheme.shapes.roundSmall)
                .padding(8.dp),
            text = stringResource(Res.string.purchase),
            color = AppTheme.colors.onPrimaryContainer
        )
    }
}

@Composable
fun ActiveSubscriptionContent(
    purchases: ImmutableList<Purchase>,
    onPurchase: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animatedBorder(
                borderColors = listOf(
                    Color(0xFFAB47BC),
                    Color(0xFF2196F3),
                    Color(0xFF7CB342),
                    AppTheme.colors.primary,
                ),
                animationDurationInMillis = 4 * 1000,
                backgroundColor = AppTheme.colors.surfaceContainerLow,
                shape = AppTheme.shapes.roundMedium,
                borderWidth = 1.5.dp,
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppIcon(
                tint = AppTheme.colors.onSurface,
                icon = Res.drawable.ic_payment
            )

            BodyMediumText(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                text = stringResource(Res.string.my_subscription),
                color = AppTheme.colors.onSurface
            )

            LabelSmallBoldText(
                modifier = Modifier
                    .animateClickable(onPurchase)
                    .background(
                        color = AppTheme.colors.primaryContainer, shape = AppTheme.shapes.roundSmall
                    )
                    .border(
                        1.dp,
                        color = AppTheme.colors.primary,
                        shape = AppTheme.shapes.roundSmall
                    )
                    .padding(8.dp),
                text = stringResource(Res.string.purchase),
                color = AppTheme.colors.onPrimaryContainer
            )
        }

        DualContentRow(
            modifier = Modifier.fillMaxWidth(),
            leftContent = {
                BodyMediumText(text = stringResource(Res.string.start_subscription))
            },
            rightContent = {
                BodyMediumBoldText(text = purchases.first().createdAt.toReadableString())
            },
            horizontalArrangement = Arrangement.SpaceBetween
        )

        DualContentRow(
            modifier = Modifier.fillMaxWidth(),
            leftContent = {
                BodyMediumText(text = stringResource(Res.string.expiration_subscription))
            },
            rightContent = {
                BodyMediumBoldText(text = purchases.last().expiryDate?.toReadableString() ?: "")
            },
            horizontalArrangement = Arrangement.SpaceBetween
        )


    }
}

@LightDarkPreview
@Composable
private fun NeedToBuyContentPreview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            PurchaseSection(
                modifier = Modifier.fillMaxWidth(),
                subscriptions = null,
                onAction = { },
                isFetching = false,
                isErrorFetching = false
            )

            PurchaseSection(
                modifier = Modifier.fillMaxWidth(),
                subscriptions = FakeData.providePurchases(),
                onAction = { },
                isFetching = false,
                isErrorFetching = false
            )

            PurchaseSection(
                modifier = Modifier.fillMaxWidth(),
                subscriptions = null,
                onAction = { },
                isFetching = false,
                isErrorFetching = true
            )

            PurchaseSection(
                modifier = Modifier.fillMaxWidth(),
                subscriptions = null,
                onAction = { },
                isFetching = true,
                isErrorFetching = true
            )
        }
    }
}

