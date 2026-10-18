package ir.aispeaking.androidApp.payment

import android.content.Context
import androidx.activity.ComponentActivity
import ir.aispeaking.domain.gateway.MarketBillingGateway
import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.model.payment.MarketPurchaseResult
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BazaarBillingDriver(
    private val context: Context,
    rsaPublicKey: String? = null
) : MarketBillingGateway {

    override val market: AppMarket = AppMarket.CAFE_BAZAAR

    private val paymentConfig = PaymentConfiguration(
        localSecurityCheck = if (!rsaPublicKey.isNullOrBlank()) {
            SecurityCheck.Enable(rsaPublicKey = rsaPublicKey)
        } else {
            SecurityCheck.Disable
        }
    )

    private val payment = Payment(context = context, config = paymentConfig)
    private var connection: Connection? = null
    private var isConnected = false

    override suspend fun startConnection(): Boolean {
        if (isConnected) return true
        val deferred = CompletableDeferred<Boolean>()

        connection = payment.connect {
            connectionSucceed {
                isConnected = true
                if (!deferred.isCompleted) deferred.complete(true)
            }
            connectionFailed {
                isConnected = false
                if (!deferred.isCompleted) deferred.complete(false)
            }
            disconnected {
                isConnected = false
            }
        }

        return deferred.await()
    }

    override suspend fun disconnect() {
        try {
            connection?.disconnect()
        } catch (_: Exception) {}
        connection = null
        isConnected = false
    }

    override suspend fun purchaseProduct(
        sku: String,
        payload: String?,
        dynamicPriceToken: String?
    ): MarketPurchaseResult {
        if (!isConnected) {
            val connected = startConnection()
            if (!connected) {
                return MarketPurchaseResult.Error(
                    errorCode = -1,
                    message = "اتصال به سرویس پرداخت کافه بازار برقرار نشد. لطفاً نصب بودن کافه بازار را بررسی نمایید."
                )
            }
        }

        val activity = CurrentActivityHolder.currentActivity as? ComponentActivity
            ?: return MarketPurchaseResult.Error(
                errorCode = -2,
                message = "اکتیویتی فعال جهت نمایش درگاه پرداخت بازار یافت نشد"
            )

        return suspendCancellableCoroutine { continuation ->
            val purchaseRequest = PurchaseRequest(
                productId = sku,
                payload = payload ?: "",
                dynamicPriceToken = dynamicPriceToken
            )

            payment.purchaseProduct(
                registry = activity.activityResultRegistry,
                request = purchaseRequest
            ) {
                purchaseFlowBegan {
                    // Purchase dialog is open
                }
                failedToBeginFlow { throwable ->
                    if (continuation.isActive) {
                        continuation.resume(
                            MarketPurchaseResult.Error(
                                errorCode = -3,
                                message = throwable.message ?: "خطا در شروع فرآیند خرید کافه بازار"
                            )
                        )
                    }
                }
                purchaseSucceed { purchaseEntity ->
                    if (continuation.isActive) {
                        continuation.resume(
                            MarketPurchaseResult.Success(
                                purchaseToken = purchaseEntity.purchaseToken,
                                orderId = purchaseEntity.orderId,
                                sku = purchaseEntity.productId,
                                developerPayload = purchaseEntity.payload,
                                market = AppMarket.CAFE_BAZAAR
                            )
                        )
                    }
                }
                purchaseCanceled {
                    if (continuation.isActive) {
                        continuation.resume(MarketPurchaseResult.UserCancelled())
                    }
                }
                purchaseFailed { throwable ->
                    if (continuation.isActive) {
                        continuation.resume(
                            MarketPurchaseResult.Error(
                                errorCode = -4,
                                message = throwable.message ?: "پرداخت در کافه بازار ناموفق بود"
                            )
                        )
                    }
                }
            }
        }
    }

    override suspend fun consumePurchase(purchaseToken: String): Boolean {
        if (!isConnected) startConnection()
        return suspendCancellableCoroutine { continuation ->
            payment.consumeProduct(purchaseToken) {
                consumeSucceed {
                    if (continuation.isActive) continuation.resume(true)
                }
                consumeFailed {
                    if (continuation.isActive) continuation.resume(false)
                }
            }
        }
    }

    override suspend fun queryUnconsumedPurchases(): List<MarketPurchaseResult.Success> {
        if (!isConnected) {
            val connected = startConnection()
            if (!connected) return emptyList()
        }

        return suspendCancellableCoroutine { continuation ->
            payment.getPurchasedProducts {
                querySucceed { purchases ->
                    val results = purchases.map { p ->
                        MarketPurchaseResult.Success(
                            purchaseToken = p.purchaseToken,
                            orderId = p.orderId,
                            sku = p.productId,
                            developerPayload = p.payload,
                            market = AppMarket.CAFE_BAZAAR
                        )
                    }
                    if (continuation.isActive) continuation.resume(results)
                }
                queryFailed {
                    if (continuation.isActive) continuation.resume(emptyList())
                }
            }
        }
    }
}
