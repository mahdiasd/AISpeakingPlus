package ir.aispeaking.network.websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.stt.dto.SttMessageDto
import ir.aispeaking.storage.preferences.token.TokenPreferences
import ir.aispeaking.utils.dLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class SttWebSocketClient(
    private val client: HttpClient,
    private val tokenPreferences: TokenPreferences
) {
    private val baseUrl get() = NetworkConfig.baseUrl
    private var activeSession: DefaultClientWebSocketSession? = null
    private val sessionMutex = Mutex()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        classDiscriminator = "type"
    }

    /**
     * Connects to the STT live streaming WebSocket endpoint (`/api/v2/stt/live`)
     * and returns a [Flow] that emits incoming [SttMessageDto] messages.
     *
     * The connection remains open while the flow is collected. When collection is
     * cancelled or ends, the session is cleanly closed.
     */
    fun connect(): Flow<SttMessageDto> = flow {
        val token = tokenPreferences.read()
        val wsUrl = buildWebSocketUrl("/api/v2/stt/live", token)
        "Connecting to STT WebSocket: $wsUrl".dLog(tag = "SttWebSocketClient")

        var session: DefaultClientWebSocketSession? = null
        try {
            session = client.webSocketSession(urlString = wsUrl)
            sessionMutex.withLock {
                activeSession = session
            }
            "STT WebSocket connected successfully".dLog(tag = "SttWebSocketClient")

            for (frame in session.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        try {
                            val message = json.decodeFromString<SttMessageDto>(text)
                            emit(message)
                        } catch (e: Throwable) {
                            "Failed to decode STT message: $text (error: ${e.message})".dLog(tag = "SttWebSocketClient")
                        }
                    }
                    is Frame.Close -> {
                        "STT WebSocket received close frame".dLog(tag = "SttWebSocketClient")
                        break
                    }
                    else -> Unit
                }
            }
        } catch (e: CancellationException) {
            "STT WebSocket collection cancelled".dLog(tag = "SttWebSocketClient")
            throw e
        } catch (e: Throwable) {
            "STT WebSocket error: ${e.message}".dLog(tag = "SttWebSocketClient")
            emit(SttMessageDto.Error(message = e.message ?: "WebSocket connection error"))
        } finally {
            sessionMutex.withLock {
                if (activeSession === session) {
                    activeSession = null
                }
            }
            try {
                session?.close(CloseReason(CloseReason.Codes.NORMAL, "Session ended"))
            } catch (_: Throwable) {}
            "STT WebSocket session closed".dLog(tag = "SttWebSocketClient")
        }
    }

    /**
     * Sends a chunk of raw mono 16kHz PCM16 audio bytes as a binary frame.
     */
    suspend fun sendAudioChunk(pcm16Bytes: ByteArray) {
        sessionMutex.withLock {
            val session = activeSession
            if (session != null) {
                session.send(Frame.Binary(fin = true, data = pcm16Bytes))
            } else {
                "Cannot send audio chunk: STT WebSocket session is not active".dLog(tag = "SttWebSocketClient")
            }
        }
    }

    /**
     * Closes the active WebSocket session if connected.
     */
    suspend fun close() {
        sessionMutex.withLock {
            val session = activeSession
            activeSession = null
            try {
                session?.close(CloseReason(CloseReason.Codes.NORMAL, "Client closed"))
            } catch (_: Throwable) {}
        }
    }

    /**
     * Returns true if an active WebSocket session is connected.
     */
    val isConnected: Boolean
        get() = activeSession != null

    private fun buildWebSocketUrl(path: String, token: String?): String {
        val baseWithoutSlash = baseUrl.trimEnd('/')
        val wsBase = when {
            baseWithoutSlash.startsWith("https://") -> "wss://" + baseWithoutSlash.removePrefix("https://")
            baseWithoutSlash.startsWith("http://") -> "ws://" + baseWithoutSlash.removePrefix("http://")
            baseWithoutSlash.startsWith("wss://") || baseWithoutSlash.startsWith("ws://") -> baseWithoutSlash
            else -> "ws://$baseWithoutSlash"
        }
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        val fullUrl = "$wsBase$cleanPath"
        return if (!token.isNullOrBlank()) {
            val separator = if (fullUrl.contains("?")) "&" else "?"
            "$fullUrl${separator}token=$token"
        } else {
            fullUrl
        }
    }
}
