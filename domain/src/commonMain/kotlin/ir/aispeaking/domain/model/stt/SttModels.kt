package ir.aispeaking.domain.model.stt

sealed interface SttStreamEvent {
    data class Ready(val message: String = "Stream ready") : SttStreamEvent
    data class PartialTranscript(val text: String) : SttStreamEvent
    data class FinalTranscript(val text: String) : SttStreamEvent
    data class Error(val message: String) : SttStreamEvent
    data object Disconnected : SttStreamEvent
}

enum class SttSessionStatus {
    IDLE,
    CONNECTING,
    CONNECTED,
    READY,
    RECORDING,
    ERROR,
    CLOSED
}

enum class SttEngine(val key: String) {
    APP("app"),
    BROWSER("browser");

    companion object {
        fun fromKey(key: String?): SttEngine {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: APP
        }
    }
}

enum class SttConnectionStatus {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    ERROR
}
