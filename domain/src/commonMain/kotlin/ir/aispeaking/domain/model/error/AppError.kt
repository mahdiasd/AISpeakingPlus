package ir.aispeaking.domain.model.error

sealed interface AppError

fun AppError.getErrorMessage(): String {
    return when (this) {
        is NetworkError -> this.message
        is DeviceError -> this.message
    }
}
