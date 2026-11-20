package ir.aispeaking.androidApp.notification

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.aispeaking.network.platformBaseUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.context.GlobalContext

@Serializable
data class RegisterTokenPayload(
    val token: String,
    val topic: String = "AISpeakingPublic",
    val platform: String = "android"
)

object DeviceTokenRegistrar {
    private val logger = Logger.withTag("DeviceTokenRegistrar")
    private val scope = CoroutineScope(Dispatchers.IO)

    fun registerToken(token: String, topic: String = "AISpeakingPublic") {
        if (token.isBlank()) return
        scope.launch {
            try {
                val httpClient = GlobalContext.get().getOrNull<HttpClient>() ?: HttpClient()
                val url = platformBaseUrl().trimEnd('/') + "/api/v1/notifications/register-token"
                val payload = RegisterTokenPayload(
                    token = token.trim(),
                    topic = topic,
                    platform = "android"
                )
                logger.i { "Registering FCM token to server: $url" }
                httpClient.post {
                    url(url)
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
                logger.i { "Successfully registered FCM token to server" }
            } catch (t: Throwable) {
                logger.w(t) { "Failed to register FCM token with server: ${t.message}" }
            }
        }
    }
}
