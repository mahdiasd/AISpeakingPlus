package ir.aispeaking.data.payment

import ir.aispeaking.domain.gateway.MarketBillingGateway
import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.model.payment.MarketPurchaseResult
import org.koin.core.annotation.Single

@Single
class DefaultMarketBillingGateway(
    override val market: AppMarket = AppMarket.DIRECT_WEB
) : MarketBillingGateway {

    override suspend fun startConnection(): Boolean = true
    override suspend fun disconnect() {}

    override suspend fun purchaseProduct(
        sku: String,
        payload: String?,
        dynamicPriceToken: String?
    ): MarketPurchaseResult {
        return MarketPurchaseResult.Error(
            errorCode = -1,
            message = "پرداخت درون‌برنامه‌ای در این پلتفرم پشتیبانی نمی‌شود. لطفاً از پرداخت تحت وب استفاده کنید."
        )
    }

    override suspend fun consumePurchase(purchaseToken: String): Boolean = true

    override suspend fun queryUnconsumedPurchases(): List<MarketPurchaseResult.Success> = emptyList()
}
