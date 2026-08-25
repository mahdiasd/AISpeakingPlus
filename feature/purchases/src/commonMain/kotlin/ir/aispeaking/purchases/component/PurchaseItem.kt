package ir.aispeaking.purchases.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.domain.model.purchase.PurchaseStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.amount_paid
import ir.aispeaking.sharedui.date
import ir.aispeaking.sharedui.failure_payment
import ir.aispeaking.sharedui.success_payment
import ir.aispeaking.sharedui.ui.core.divider.HorizontalDashDivider
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.toPrice
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.unknown_payment
import ir.aispeaking.utils.time.toReadableString
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PurchaseItem(modifier: Modifier, item: Purchase) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppTheme.colors.surfaceContainerHighest,
                shape = AppTheme.shapes.roundSmall
            )
            .border(1.dp, AppTheme.colors.outline, shape = AppTheme.shapes.roundSmall)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        DualContentRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            leftContent = {
                LabelMediumBoldText(
                    modifier = Modifier
                        .widthIn(min = 50.dp)
                        .background(
                            color = when (item.status) {
                                PurchaseStatus.SUCCESS -> AppTheme.colors.success
                                PurchaseStatus.FAILED -> AppTheme.colors.error
                                null -> AppTheme.colors.outline
                            },
                            shape = AppTheme.shapes.roundSmall
                        )
                        .padding(4.dp),
                    text = stringResource(
                        when (item.status) {
                            PurchaseStatus.SUCCESS -> Res.string.success_payment
                            PurchaseStatus.FAILED -> Res.string.failure_payment
                            null -> Res.string.unknown_payment
                        }
                    ),
                    color = when (item.status) {
                        PurchaseStatus.SUCCESS -> AppTheme.colors.onSuccess
                        PurchaseStatus.FAILED -> AppTheme.colors.onError
                        null -> AppTheme.colors.onSurface
                    },
                    persianFont = true,
                    textAlign = TextAlign.Center
                )
            },
            rightContent = {
                BodyLargeBoldText(
                    text = item.plan.title,
                    textAlign = TextAlign.Center,
                    persianFont = true
                )
            }
        )

        HorizontalDashDivider(
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.outline,
            thickness = 1.dp
        )

        DualContentRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            leftContent = {
                BodyMediumBoldText(
                    text = item.amountPaid.toPrice(),
                    color = if (item.amountPaid == "0" || item.amountPaid.isEmpty()) {
                        AppTheme.colors.onSurface
                    } else {
                        AppTheme.colors.success
                    },
                    persianFont = true
                )
            },
            rightContent = {
                BodyMediumText(
                    text = stringResource(Res.string.amount_paid),
                    persianFont = true
                )
            }
        )

        DualContentRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            leftContent = {
                BodyMediumBoldText(
                    text = item.createdAt.toReadableString(),
                    persianFont = true
                )
            },
            rightContent = {
                BodyMediumText(
                    text = stringResource(Res.string.date),
                    persianFont = true
                )
            }
        )
    }
}

@LightDarkPreview
@Composable
private fun PurchaseItemSuccessPreview() {
    AppTheme {
        PurchaseItem(
            modifier = Modifier,
            item = FakeData.providePurchases().first()
        )
    }
}
