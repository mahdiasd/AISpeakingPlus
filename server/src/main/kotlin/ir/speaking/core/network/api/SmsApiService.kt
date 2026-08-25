package ir.speaking.core.network.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
class SmsApiService(
    private val httpClient: HttpClient,
) {

    suspend fun sendMessage(
        mobile: String,
        otpCode: String,
//        hashCode: String = "VxmaRlfmLW3", // debug
        hashCode: String = "sEkR5IGvUNM", // release
    ) = httpClient.post {
        url("https://api.sms.ir/v1/send/verify")
        header("x-api-key" , "InIBEeagvNpmc1gpp53HqtZUpdEf9FCnCOH6G91O4ev31gsR")
        header("ACCEPT" , "application/json")
        setBody(
            SmsRequest(
                mobile = mobile,
                templateId = 715365,
                parameters = listOf(
                    SmsParameter(
                        name = "OTP_CODE",
                        value = otpCode
                    ),
                    SmsParameter(
                        name = "HASH_CODE",
                        value = "$hashCode"
                    )
                )
            )
        )
        expectSuccess = true
    }.body<SmsResponse>()

    fun generateOTP(length: Int = 5): String {
//        return "1234"
        return (0 until length)
            .map { kotlin.random.Random.nextInt(0, 9) }
            .joinToString("")
    }
}

@Serializable
data class SmsRequest(
    val mobile: String,
    val templateId: Int,
    val parameters: List<SmsParameter>
)

@Serializable
data class SmsParameter(
    val name: String,
    val value: String
)

@Serializable
data class SmsResponse(
    val status: Int,
    val message: String
)