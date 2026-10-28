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
    private val pendingChunks = mutableListOf<ByteArray>()

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
            val bufferedToFlush: List<ByteArray>
            sessionMutex.withLock {
                activeSession = session
                bufferedToFlush = pendingChunks.toList()
                pendingChunks.clear()
            }
            "STT WebSocket connected successfully".dLog(tag = "SttWebSocketClient")

            // Flush any initial audio frames captured while connecting so start of speech is preserved
            for (chunk in bufferedToFlush) {
                try {
                    session.send(Frame.Binary(fin = true, data = chunk))
                } catch (e: Throwable) {
                    "Failed to flush pending STT chunk: ${e.message}".dLog(tag = "SttWebSocketClient")
                }
            }

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
                pendingChunks.clear()
            }
            try {
                session?.close(CloseReason(CloseReason.Codes.NORMAL, "Session ended"))
            } catch (_: Throwable) {}
            "STT WebSocket session closed".dLog(tag = "SttWebSocketClient")
        }
    }

    /**
     * Sends a chunk of raw mono 16kHz PCM16 audio bytes as a binary frame.
     * If the session is still connecting, buffers the chunk so initial speech is not lost.
     */
    suspend fun sendAudioChunk(pcm16Bytes: ByteArray) {
        sessionMutex.withLock {
            val session = activeSession
            if (session != null) {
                session.send(Frame.Binary(fin = true, data = pcm16Bytes))
            } else {
                // Buffer up to 60 chunks (~7.5s) while establishing connection
                if (pendingChunks.size < 60) {
                    pendingChunks.add(pcm16Bytes)
                }
            }
        }
    }

    /**
     * Sends a stop/finish control frame to notify the server that audio input is complete,
     * triggering a final decode flush.
     */
    suspend fun sendStop() {
        sessionMutex.withLock {
            val session = activeSession
            if (session != null) {
                try {
                    session.send(Frame.Text("{\"type\":\"stop\"}"))
                } catch (_: Throwable) {}
            }
        }
    }

    /**
     * Closes the active WebSocket session if connected.
     */
    suspend fun close() {
        sessionMutex.withLock {
            pendingChunks.clear()
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
