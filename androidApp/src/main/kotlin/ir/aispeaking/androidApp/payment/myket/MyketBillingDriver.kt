package ir.aispeaking.androidApp.payment.myket

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import ir.aispeaking.androidApp.payment.CurrentActivityHolder
import ir.aispeaking.domain.gateway.MarketBillingGateway
import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.model.payment.MarketPurchaseResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import kotlin.coroutines.resume

class MyketBillingDriver(
    private val context: Context
) : MarketBillingGateway {

    override val market: AppMarket = AppMarket.MYKET

    private var billingService: IInAppBillingService? = null
    private var isConnected = false
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            billingService = IInAppBillingService.Stub.asInterface(service)
            isConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            billingService = null
            isConnected = false
        }
    }

    override suspend fun startConnection(): Boolean = withContext(Dispatchers.Main) {
        if (isConnected && billingService != null) return@withContext true

        val deferred = CompletableDeferred<Boolean>()
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                billingService = IInAppBillingService.Stub.asInterface(service)
                isConnected = true
                if (!deferred.isCompleted) deferred.complete(true)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                billingService = null
                isConnected = false
                if (!deferred.isCompleted) deferred.complete(false)
            }
        }

        try {
            val intent = Intent("ir.mservices.market.InAppBillingService.BIND").apply {
                setPackage("ir.mservices.market")
            }
            val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            if (!bound && !deferred.isCompleted) {
                deferred.complete(false)
            }
        } catch (_: Exception) {
            if (!deferred.isCompleted) deferred.complete(false)
        }

        deferred.await()
    }

    override suspend fun disconnect() = withContext(Dispatchers.Main) {
        try {
            if (isConnected) {
                context.unbindService(serviceConnection)
            }
        } catch (_: Exception) {}
        billingService = null
        isConnected = false
    }

    override suspend fun purchaseProduct(
        sku: String,
        payload: String?,
        dynamicPriceToken: String?
    ): MarketPurchaseResult {
        if (!isConnected || billingService == null) {
            val connected = startConnection()
            if (!connected) {
                return MarketPurchaseResult.Error(
                    errorCode = -1,
                    message = "اتصال به سرویس پرداخت مایکت برقرار نشد. لطفاً نصب بودن برنامه مایکت را بررسی نمایید."
                )
            }
        }

        val activity = CurrentActivityHolder.currentActivity as? ComponentActivity
            ?: return MarketPurchaseResult.Error(
                errorCode = -2,
                message = "اکتیویتی فعال جهت نمایش درگاه پرداخت مایکت یافت نشد"
            )

        val buyBundle = withContext(Dispatchers.IO) {
            try {
                billingService?.getBuyIntent(
                    apiVersion = 3,
                    packageName = context.packageName,
                    sku = sku,
                    type = "inapp",
                    developerPayload = payload ?: ""
                )
            } catch (e: Exception) {
                null
            }
        } ?: return MarketPurchaseResult.Error(
            errorCode = -3,
            message = "خطا در دریافت اطلاعات درگاه پرداخت از مایکت"
        )

        val responseCode = buyBundle.getInt("RESPONSE_CODE", -1)
        if (responseCode != 0) {
            val errorMsg = when (responseCode) {
                1 -> "خرید توسط کاربر لغو شد"
                3 -> "پرداخت در نسخه مایکت نصب‌شده روی این دستگاه پشتیبانی نمی‌شود"
                4 -> "محصول مورد نظر در مایکت یافت نشد یا غیرفعال است"
                7 -> "این محصول قبلاً خریداری شده است"
                else -> "خطای پرداخت مایکت (کد $responseCode)"
            }
            return if (responseCode == 1) {
                MarketPurchaseResult.UserCancelled(errorMsg)
            } else {
                MarketPurchaseResult.Error(responseCode, errorMsg)
            }
        }

        @Suppress("DEPRECATION")
        val pendingIntent = buyBundle.getParcelable<android.app.PendingIntent>("BUY_INTENT")
            ?: return MarketPurchaseResult.Error(-4, "شناسه اینتنت پرداخت از مایکت دریافت نشد")

        return suspendCancellableCoroutine { continuation ->
            val launcherKey = "myket_purchase_${UUID.randomUUID()}"
            var launcher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>? = null

            launcher = activity.activityResultRegistry.register(
                launcherKey,
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                launcher?.unregister()
                if (result.resultCode == Activity.RESULT_OK) {
                    val purchaseData = result.data?.getStringExtra("INAPP_PURCHASE_DATA")
                    if (purchaseData != null) {
                        try {
                            val parsed = json.decodeFromString<JsonObject>(purchaseData)
                            val token = parsed["purchaseToken"]?.jsonPrimitive?.content ?: ""
                            val orderId = parsed["orderId"]?.jsonPrimitive?.content
                            val prodId = parsed["productId"]?.jsonPrimitive?.content ?: sku
                            val devPayload = parsed["developerPayload"]?.jsonPrimitive?.content

                            if (continuation.isActive) {
                                continuation.resume(
                                    MarketPurchaseResult.Success(
                                        purchaseToken = token,
                                        orderId = orderId,
                                        sku = prodId,
                                        developerPayload = devPayload,
                                        market = AppMarket.MYKET
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    MarketPurchaseResult.Error(
                                        errorCode = -5,
                                        message = "خطا در تجزیه رسید پرداخت مایکت: ${e.message}"
                                    )
                                )
                            }
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(
                                MarketPurchaseResult.Error(
                                    errorCode = -6,
                                    message = "رسید خرید از مایکت دریافت نشد"
                                )
                            )
                        }
                    }
                } else {
                    if (continuation.isActive) {
                        continuation.resume(MarketPurchaseResult.UserCancelled())
                    }
                }
            }

            try {
                launcher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            } catch (e: Exception) {
                launcher.unregister()
                if (continuation.isActive) {
                    continuation.resume(
                        MarketPurchaseResult.Error(
                            errorCode = -7,
                            message = "خطا در باز کردن درگاه پرداخت مایکت: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    override suspend fun consumePurchase(purchaseToken: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected || billingService == null) startConnection()
        try {
            val code = billingService?.consumePurchase(3, context.packageName, purchaseToken)
            code == 0
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun queryUnconsumedPurchases(): List<MarketPurchaseResult.Success> = withContext(Dispatchers.IO) {
        if (!isConnected || billingService == null) {
            val connected = startConnection()
            if (!connected) return@withContext emptyList()
        }

        try {
            val bundle = billingService?.getPurchases(3, context.packageName, "inapp", null) ?: return@withContext emptyList()
            val responseCode = bundle.getInt("RESPONSE_CODE", -1)
            if (responseCode != 0) return@withContext emptyList()

            val purchaseDataList = bundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST") ?: return@withContext emptyList()
            purchaseDataList.mapNotNull { jsonStr ->
                try {
                    val parsed = json.decodeFromString<JsonObject>(jsonStr)
                    val token = parsed["purchaseToken"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val prodId = parsed["productId"]?.jsonPrimitive?.content ?: ""
                    val orderId = parsed["orderId"]?.jsonPrimitive?.content
                    val payload = parsed["developerPayload"]?.jsonPrimitive?.content

                    MarketPurchaseResult.Success(
                        purchaseToken = token,
                        orderId = orderId,
                        sku = prodId,
                        developerPayload = payload,
                        market = AppMarket.MYKET
                    )
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
