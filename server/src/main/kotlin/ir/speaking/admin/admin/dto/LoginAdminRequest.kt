package ir.speaking.admin.admin.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginAdminRequest(
    val username: String,
    val password: String,
)