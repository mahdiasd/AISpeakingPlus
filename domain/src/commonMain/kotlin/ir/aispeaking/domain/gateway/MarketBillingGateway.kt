package ir.aispeaking.domain.gateway

import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.model.payment.MarketPurchaseResult

interface MarketBillingGateway {
    val market: AppMarket

    suspend fun startConnection(): Boolean
    suspend fun disconnect()

    suspend fun purchaseProduct(
        sku: String,
        payload: String? = null,
        dynamicPriceToken: String? = null
    ): MarketPurchaseResult

    suspend fun consumePurchase(purchaseToken: String): Boolean

    suspend fun queryUnconsumedPurchases(): List<MarketPurchaseResult.Success>
}
