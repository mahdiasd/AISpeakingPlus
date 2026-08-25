package ir.aispeaking.domain.model.socket

sealed class SocketStatus {
    data object Connecting : SocketStatus()
    data object Connected : SocketStatus()
    data object Disconnected : SocketStatus()
    data class Error(val message: String) : SocketStatus()
}