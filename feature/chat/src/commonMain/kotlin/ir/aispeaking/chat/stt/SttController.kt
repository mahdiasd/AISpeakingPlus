package ir.aispeaking.chat.stt

import io.ktor.client.HttpClient
import ir.aispeaking.storage.preferences.token.TokenPreferences
import ir.aispeaking.utils.dLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

/**
 * Events emitted by [SttController] to notify observers (such as [ir.aispeaking.chat.ChatViewModel])
 * of discrete one-off occurrences like committed utterances or errors.
 */
sealed interface SttEvent {
    /** A final utterance recognized and committed by the server. */
    data class FinalTranscript(val text: String) : SttEvent
    /** An error reported by the server or client pipeline. */
    data class Error(val message: String) : SttEvent
    /** Recording session has started. */
    data object SessionStarted : SttEvent
    /** Recording session has ended. */
    data object SessionStopped : SttEvent
}

/**
 * UI-facing state shape for the STT mic button and live transcript preview.
 */
data class SttUiState(
    val isRecording: Boolean = false,
    val connected: Boolean = false,
    val partial: String = "",
    val error: String? = null,
)

/**
 * Orchestrates one microphone -> WebSocket session at a time.
 *
 * Lifecycle:
 *  1. `start()`  - opens WS, begins audio capture, streams bytes, collects
 *     transcription events and updates [_uiState].
 *  2. `stop()`   - cancels capture + WS; any pending `partial` text is emitted
 *     as a [SttEvent.FinalTranscript] so the user does not lose their last words.
 *  3. `reset()`  - clears [SttUiState] (e.g. when the chat screen is left).
 *
 * The controller is registered as a Koin `@Single`; callers may keep the
 * same instance alive for the whole chat screen and call `start/stop`
 * repeatedly when the user interacts with the mic.
 */
@Single
class SttController(
    private val httpClient: HttpClient,
    private val tokenPreferences: TokenPreferences,
) {

    private val _uiState = MutableStateFlow(SttUiState())
    val uiState: StateFlow<SttUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SttEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SttEvent> = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob())
    private var sessionJob: Job? = null
    private var activeCapture: AudioCapture? = null

    /**
     * Begin a STT session.
     *
     * No-op if a session is already active. Returns immediately; results
     * land in [uiState] and [events].
     */
    fun start(passTokenInQuery: Boolean = false) {
        if (sessionJob?.isActive == true) {
            "SttController: session already active".dLog(tag = "SttController")
            return
        }
        val token = tokenPreferences.read()
        if (token.isBlank()) {
            val err = "Not signed in"
            _uiState.update { it.copy(error = err, isRecording = false) }
            _events.tryEmit(SttEvent.Error(err))
            return
        }

        val client = SttClient(
            httpClient = httpClient,
            authToken = token,
            passTokenInQuery = passTokenInQuery,
        )

        _uiState.value = SttUiState(isRecording = true)
        _events.tryEmit(SttEvent.SessionStarted)

        val capture = AudioCapture()
        activeCapture = capture

        sessionJob = scope.launch {
            try {
                val audioFlow = capture.start()
                client.session(audioFlow).collect { msg ->
                    handleMessage(msg)
                }
            } catch (t: Throwable) {
                val errMsg = t.message ?: "STT session failed"
                "SttController: session failed: $errMsg".dLog(tag = "SttController")
                _uiState.update {
                    it.copy(error = errMsg, connected = false, isRecording = false)
                }
                _events.tryEmit(SttEvent.Error(errMsg))
            } finally {
                runCatching { capture.stop() }
                _uiState.update { it.copy(connected = false, isRecording = false) }
                if (activeCapture === capture) activeCapture = null
            }
        }
    }

    /**
     * End the active session. Cancels the WS, stops audio capture. Any
     * pending `partial` text currently in [uiState] is emitted as a
     * [SttEvent.FinalTranscript] before resetting.
     */
    fun stop() {
        val remainingPartial = _uiState.value.partial.trim()
        if (remainingPartial.isNotEmpty()) {
            _events.tryEmit(SttEvent.FinalTranscript(remainingPartial))
        }

        sessionJob?.cancel()
        sessionJob = null
        runCatching { activeCapture?.stop() }
        _uiState.update { it.copy(connected = false, isRecording = false, partial = "") }
        _events.tryEmit(SttEvent.SessionStopped)
    }

    /**
     * Clear all transient state. Call when leaving the chat screen.
     */
    fun reset() {
        stop()
        _uiState.value = SttUiState()
    }

    private fun handleMessage(msg: SttMessage) {
        when (msg) {
            is SttMessage.Ready -> {
                _uiState.update {
                    it.copy(connected = true, error = null)
                }
            }
            is SttMessage.Partial -> {
                _uiState.update {
                    it.copy(partial = msg.text)
                }
            }
            is SttMessage.Final -> {
                _uiState.update {
                    it.copy(partial = "")
                }
                if (msg.text.isNotBlank()) {
                    _events.tryEmit(SttEvent.FinalTranscript(msg.text))
                }
            }
            is SttMessage.Error -> {
                _uiState.update {
                    it.copy(error = msg.message, connected = false, isRecording = false)
                }
                _events.tryEmit(SttEvent.Error(msg.message))
            }
        }
    }

    /**
     * Free resources — call from the owning scope's `onCleared` if any.
     */
    fun shutdown() {
        scope.cancel()
    }
}

