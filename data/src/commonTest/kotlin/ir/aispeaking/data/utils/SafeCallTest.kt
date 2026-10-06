package ir.aispeaking.data.utils

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.network.model.NetworkResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SafeCallTest {

    @Test
    fun testSafeCallSuccess() = runTest {
        val result = safeCall {
            NetworkResponse(
                data = "success_payload",
                message = "OK"
            )
        }
        assertIs<DataResult.Success<String>>(result)
        assertEquals("success_payload", result.data)
        assertEquals("OK", result.message)
    }

    @Test
    fun testSafeCallErrorCodeMapping() = runTest {
        val resultUnauthorized = safeCall<String> {
            NetworkResponse(
                errorCode = 401,
                errorMessage = "Unauthorized token"
            )
        }
        assertIs<DataResult.Failure<String>>(resultUnauthorized)
        assertIs<NetworkError.Unauthorized>(resultUnauthorized.appError)

        val resultPayment = safeCall<String> {
            NetworkResponse(
                errorCode = 402,
                errorMessage = "Subscription required"
            )
        }
        assertIs<DataResult.Failure<String>>(resultPayment)
        assertIs<NetworkError.PaymentRequired>(resultPayment.appError)
    }

    @Test
    fun testGetApiErrorMapping() {
        val err400 = getApiError(400, "Bad Request")
        assertIs<NetworkError.BadRequest>(err400)

        val err401 = getApiError(401, "Auth Required")
        assertIs<NetworkError.Unauthorized>(err401)

        val err402 = getApiError(402, "Payment Required")
        assertIs<NetworkError.PaymentRequired>(err402)

        val err404 = getApiError(404, "Not Found")
        assertIs<NetworkError.NotFound>(err404)

        val err503 = getApiError(503, "Service Busy")
        assertIs<NetworkError.ServiceUnavailable>(err503)
    }
}
