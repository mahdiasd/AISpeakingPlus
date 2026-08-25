package ir.aispeaking.sharedui.ui.model.permission

sealed class PermissionState {
    data object Granted : PermissionState()
    data object Denied : PermissionState()
    data object Requesting : PermissionState()
}