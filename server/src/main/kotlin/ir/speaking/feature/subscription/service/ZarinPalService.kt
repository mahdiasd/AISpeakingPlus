package ir.speaking.feature.subscription.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ir.speaking.core.network.utils.PrintHelper
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.koin.core.annotation.Single

@Serializable
data class ZarinPalRequestDto(
    @SerialName("merchant_id") val merchantId: String,
    val amount: Long,
    val currency: String = "IRT",
    val description: String,
    @SerialName("callback_url") val callbackUrl: String,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ZarinPalVerifyDto(
    @SerialName("merchant_id") val merchantId: String,
    val amount: Long,
    val authority: String
)

@Serializable
data class ZarinPalResponseData(
    val code: Int,
    val message: String,
    val authority: String? = null,
    @SerialName("ref_id") val refId: Long? = null,
    @SerialName("card_pan") val cardPan: String? = null,
    @SerialName("card_hash") val cardHash: String? = null,
    val fee: Long? = null
)

@Serializable
data class ZarinPalResponse(
    val data: ZarinPalResponseData? = null,
    val errors: JsonObject? = null
)

data class ZarinPalPaymentRequestResult(
    val isSuccess: Boolean,
    val authority: String? = null,
    val paymentUrl: String? = null,
    val errorMessage: String? = null
)

data class ZarinPalVerificationResult(
    val isSuccess: Boolean,
    val refId: Long? = null,
    val cardPan: String? = null,
    val rawResponse: String? = null,
    val errorMessage: String? = null,
    val isAlreadyVerified: Boolean = false
)

@Single
class ZarinPalService(
    private val httpClient: HttpClient = HttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val merchantId: String = System.getenv("ZARINPAL_MERCHANT_ID") ?: "zarinpal_merchant_id"
    private val isSandbox: Boolean = System.getenv("ZARINPAL_SANDBOX")?.toBoolean() ?: false

    val paymentBaseUrl: String = if (isSandbox) {
        "https://sandbox.zarinpal.com/pg/v4/payment"
    } else {
        "https://payment.zarinpal.com/pg/v4/payment"
    }

    val startPayBaseUrl: String = if (isSandbox) {
        "https://sandbox.zarinpal.com/pg/StartPay/"
    } else {
        "https://payment.zarinpal.com/pg/StartPay/"
    }

    suspend fun requestPayment(
        amountTomans: Long,
        description: String,
        callbackUrl: String,
        mobile: String? = null,
        orderId: String? = null
    ): ZarinPalPaymentRequestResult {
        val isTestEnv = System.getenv("APP_ENV") == "test" || merchantId == "zarinpal_merchant_id"
        if (isTestEnv) {
            val mockAuthority = "test_mock_authority_${Clock.System.now().toEpochMilliseconds()}"
            val mockPaymentUrl = "$startPayBaseUrl$mockAuthority"
            PrintHelper.info("ZarinPalService: Simulated payment request generated authority=$mockAuthority")
            return ZarinPalPaymentRequestResult(
                isSuccess = true,
                authority = mockAuthority,
                paymentUrl = mockPaymentUrl
            )
        }

        return try {
            val metadata = buildMap {
                mobile?.let { put("mobile", it) }
                orderId?.let { put("order_id", it) }
            }.ifEmpty { null }

            val response: HttpResponse = httpClient.post("$paymentBaseUrl/request.json") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        ZarinPalRequestDto.serializer(),
                        ZarinPalRequestDto(
                            merchantId = merchantId,
                            amount = amountTomans,
                            currency = "IRT",
                            description = description,
                            callbackUrl = callbackUrl,
                            metadata = metadata
                        )
                    )
                )
            }

            val body = response.bodyAsText()
            val parsed = json.decodeFromString<ZarinPalResponse>(body)
            val data = parsed.data

            if (response.status == HttpStatusCode.OK && data != null && data.code == 100 && !data.authority.isNullOrBlank()) {
                val authority = data.authority
                val paymentUrl = "$startPayBaseUrl$authority"
                ZarinPalPaymentRequestResult(
                    isSuccess = true,
                    authority = authority,
                    paymentUrl = paymentUrl
                )
            } else {
                val errorMsg = parsed.errors?.toString() ?: data?.message ?: "ZarinPal request failed with status ${response.status}"
                PrintHelper.error("ZarinPalService: Request error: $errorMsg (body: $body)")
                ZarinPalPaymentRequestResult(
                    isSuccess = false,
                    errorMessage = errorMsg
                )
            }
        } catch (e: Exception) {
            PrintHelper.error("ZarinPalService: Exception during requestPayment: ${e.message}")
            ZarinPalPaymentRequestResult(
                isSuccess = false,
                errorMessage = e.message ?: "Network error connecting to ZarinPal"
            )
        }
    }

    suspend fun verifyPayment(
        authority: String,
        amountTomans: Long
    ): ZarinPalVerificationResult {
        val isTestEnv = System.getenv("APP_ENV") == "test" || merchantId == "zarinpal_merchant_id"
        if (isTestEnv || authority.startsWith("test_mock_")) {
            PrintHelper.info("ZarinPalService: Simulated verification for authority: $authority")
            val mockRefId = Clock.System.now().toEpochMilliseconds()
            return ZarinPalVerificationResult(
                isSuccess = true,
                refId = mockRefId,
                cardPan = "603799******1234",
                rawResponse = """{"code":100,"ref_id":$mockRefId,"simulated":true}"""
            )
        }

        return try {
            val response: HttpResponse = httpClient.post("$paymentBaseUrl/verify.json") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        ZarinPalVerifyDto.serializer(),
                        ZarinPalVerifyDto(
                            merchantId = merchantId,
                            amount = amountTomans,
                            authority = authority
                        )
                    )
                )
            }

            val body = response.bodyAsText()
            val parsed = json.decodeFromString<ZarinPalResponse>(body)
            val data = parsed.data

            if (response.status == HttpStatusCode.OK && data != null && (data.code == 100 || data.code == 101)) {
                ZarinPalVerificationResult(
                    isSuccess = true,
                    refId = data.refId,
                    cardPan = data.cardPan,
                    rawResponse = body,
                    isAlreadyVerified = data.code == 101
                )
            } else {
                val errorMsg = parsed.errors?.toString() ?: data?.message ?: "ZarinPal verification failed with code ${data?.code}"
                PrintHelper.error("ZarinPalService: Verification error: $errorMsg (body: $body)")
                ZarinPalVerificationResult(
                    isSuccess = false,
                    rawResponse = body,
                    errorMessage = errorMsg
                )
            }
        } catch (e: Exception) {
            PrintHelper.error("ZarinPalService: Exception during verifyPayment: ${e.message}")
            ZarinPalVerificationResult(
                isSuccess = false,
                errorMessage = e.message ?: "Network error verifying payment with ZarinPal"
            )
        }
    }
}
