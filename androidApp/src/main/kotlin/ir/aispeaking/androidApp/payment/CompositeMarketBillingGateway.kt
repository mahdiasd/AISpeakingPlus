package ir.aispeaking.androidApp.payment

import android.content.Context
import ir.aispeaking.androidApp.payment.myket.MyketBillingDriver
import ir.aispeaking.data.payment.AndroidMarketDetector
import ir.aispeaking.data.payment.DefaultMarketBillingGateway
import ir.aispeaking.domain.gateway.MarketBillingGateway
import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.model.payment.MarketPurchaseResult

class CompositeMarketBillingGateway(
    private val context: Context,
    private val detector: AndroidMarketDetector
) : MarketBillingGateway {

    private val bazaarDriver by lazy {
        BazaarBillingDriver(
            context = context,
            rsaPublicKey = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDO/f9fBWHjwSbwoyMdohQF+PunlQSGUpFDJVlp/qPzcxRCaJIWFXhj6fSeZ/0/kqzzaFUsx1nVj7u/u0E5h8QbzcyK0SzzoBBOamCIfPVYAMExtueApXR/7QiT6ZxlN3O9FFhnuFYefZgtTWB7R7d2Fo+jCWLJIbxtlf5fNr43UutXWF9b1QSkdBxVtDTp5xueO+Micylg/vPwsnZLI5DfGgoeLad9O0OGMeTJYfMCAwEAAQ=="
        )
    }
    private val myketDriver by lazy { MyketBillingDriver(context) }
    private val defaultDriver by lazy { DefaultMarketBillingGateway() }

    private val activeGateway: MarketBillingGateway
        get() = when (detector.detectMarket()) {
            AppMarket.CAFE_BAZAAR -> bazaarDriver
            AppMarket.MYKET -> myketDriver
            else -> defaultDriver
        }

    override val market: AppMarket
        get() = activeGateway.market

    override suspend fun startConnection(): Boolean = activeGateway.startConnection()

    override suspend fun disconnect() = activeGateway.disconnect()

    override suspend fun purchaseProduct(
        sku: String,
        payload: String?,
        dynamicPriceToken: String?
    ): MarketPurchaseResult = activeGateway.purchaseProduct(sku, payload, dynamicPriceToken)

    override suspend fun consumePurchase(purchaseToken: String): Boolean =
        activeGateway.consumePurchase(purchaseToken)

    override suspend fun queryUnconsumedPurchases(): List<MarketPurchaseResult.Success> =
        activeGateway.queryUnconsumedPurchases()
}
