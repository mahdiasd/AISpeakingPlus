package ir.aispeaking.chat.stt

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import ir.aispeaking.network.platformBaseUrl
import ir.aispeaking.utils.dLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Cross-platform WebSocket client for the server-side streaming STT endpoint.
 *
 * Opens a single persistent `webSocket {}` connection to `/api/v1/stt`,
 * authenticates using the provided [authToken], and pipes:
 *  - incoming audio (`Flow<ByteArray>` of PCM16 bytes) → binary frames
 *  - server text frames → parsed [SttMessage] events
 *
 * The native targets (Android, iOS, JVM) send the JWT as an `Authorization`
 * header — Ktor's client WebSocket supports setting request headers via the
 * `request { header(...) }` block. The browser target (JS / WASM) cannot set
 * custom headers on the WebSocket handshake, so we fall back to passing the
 * token as a `?token=<jwt>` query parameter ([passTokenInQuery] = true).
 *
 * Authentication transport is decided by the caller via [passTokenInQuery]
 * — pass `false` for native, `true` for web (see §2 and §9.1 of
 * STT_CLIENT_INTEGRATION.md).
 *
 * Each call to [session] opens exactly one WS connection. The returned
 * [Flow] completes when:
 *  - the upstream audio flow completes (caller stops capture), OR
 *  - the server closes the WS (capacity error, idle timeout, etc.).
 *
 * The caller is expected to cancel the collector to free the mic and the
 * WS slot on the server (server frees its stream on WS close — see §7).
 */
class SttClient(
    private val httpClient: HttpClient,
    private val authToken: String,
    private val json: Json = sttJson,
    private val passTokenInQuery: Boolean = false,
) {

    /**
     * Open a single STT session.
     *
     * @param audio Flow of PCM16 mono 16 kHz little-endian audio bytes. The
     *   client sends each emission as a binary WebSocket frame.
     */
    fun session(audio: Flow<ByteArray>): Flow<SttMessage> = channelFlow {
        if (authToken.isBlank()) {
            close(IllegalStateException("STT requires a non-empty user JWT"))
            return@channelFlow
        }

        val url = buildSttUrl(platformBaseUrl(), passTokenInQuery, authToken)
        "SttClient: connecting to $url".dLog(tag = "SttClient")

        val session = httpClient.webSocketSession {
            url(url)
            if (!passTokenInQuery) {
                header("Authorization", "Bearer $authToken")
            }
        }

        var audioJob: kotlinx.coroutines.Job? = null

        try {
            // Pump server -> JSON messages.
            for (frame in session.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val raw = frame.readText()
                        val msg = runCatching {
                            json.decodeFromString(SttMessage.serializer(), raw)
                        }.getOrElse { t ->
                            "SttClient: failed to decode '$raw': ${t.message}".dLog(tag = "SttClient")
                            SttMessage.Error(message = "client_decode_failed")
                        }

                        // Emit the message downstream
                        trySend(msg)

                        when (msg) {
                            is SttMessage.Ready -> {
                                // Server slot is ready; begin streaming binary audio frames.
                                if (audioJob == null || !audioJob.isActive) {
                                    audioJob = launch {
                                        audio.onEach { bytes ->
                                            if (bytes.isNotEmpty()) session.send(Frame.Binary(true, bytes))
                                        }.collect()
                                    }
                                }
                            }

                            is SttMessage.Error -> {
                                // Unrecoverable server error (e.g. server_busy);
                                // stop sending audio and close the flow.
                                audioJob?.cancel()
                                close()
                                return@channelFlow
                            }

                            else -> { /* Partial or Final transcript */ }
                        }
                    }

                    is Frame.Close -> {
                        "SttClient: server closed frame received"
                            .dLog(tag = "SttClient")
                        break
                    }

                    else -> { /* ignore ping/pong/binary frames from server */ }
                }
            }

            audioJob?.cancel()
            runCatching { session.close() }
        } catch (t: Throwable) {
            "SttClient: session failed: ${t.message}".dLog(tag = "SttClient")
            audioJob?.cancel()
            close(t)
        } finally {
            audioJob?.cancel()
            runCatching { session.close() }
        }
    }

    companion object {
        /**
         * Convert the REST base URL to a WebSocket URL pointing at
         * `/api/v1/stt`. Maps `https://...` → `wss://...` and
         * `http://...` → `ws://...`, then appends the path and (on web)
         * the `?token=<jwt>` query parameter.
         */
        internal fun buildSttUrl(base: String, tokenInQuery: Boolean, token: String): String {
            val withScheme = when {
                base.startsWith("https://") -> "wss://" + base.removePrefix("https://")
                base.startsWith("http://") -> "ws://" + base.removePrefix("http://")
                base.startsWith("wss://") || base.startsWith("ws://") -> base
                else -> base // fall through; dev environments may already be relative
            }
            val normalized = withScheme.trimEnd('/')
            val path = "$normalized/api/v1/stt"
            return if (tokenInQuery) {
                val sep = if (path.contains('?')) '&' else '?'
                "$path${sep}token=${encodeURIComponentCompat(token)}"
            } else {
                path
            }
        }
    }
}

/**
 * Cross-platform URL component encoder.
 *
 * On JS / WASM, delegates to the browser's `encodeURIComponent`. On other
 * targets (Android, iOS, JVM) the JWT is sent in the `Authorization`
 * header instead of the query string, so this function is never called —
 * but a stub is required to satisfy the `expect`.
 */
internal expect fun encodeURIComponentCompat(value: String): String
