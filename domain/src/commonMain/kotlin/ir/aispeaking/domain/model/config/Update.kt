package ir.aispeaking.domain.model.config

data class Update(
    val forceVersion: Int = 0,
    val lastVersion: Int = 0,
    val suggestVersion: Int = 0,
    val link: String = "",
    val message: String = ""
) {
    fun getState(versionCode: Int): UpdateState {
        return when {
            forceVersion > versionCode -> UpdateState.UpdateRequired
            suggestVersion > versionCode -> UpdateState.UpdateRecommended
            else -> UpdateState.UpToDate
        }
    }
}

sealed class UpdateState {
    data object UpdateRequired : UpdateState()
    data object UpdateRecommended : UpdateState()
    data object UpToDate : UpdateState()
}