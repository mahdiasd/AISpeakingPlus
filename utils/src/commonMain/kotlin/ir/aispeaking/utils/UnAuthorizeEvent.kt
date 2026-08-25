package ir.aispeaking.utils

import kotlinx.coroutines.flow.Flow

interface UnAuthorizeEvent {
   suspend fun onEvent(): Flow<AuthEvent>
   suspend fun setEvent(authEvent: AuthEvent)
}

sealed class AuthEvent {
    data object MoveToLogin : AuthEvent()
}