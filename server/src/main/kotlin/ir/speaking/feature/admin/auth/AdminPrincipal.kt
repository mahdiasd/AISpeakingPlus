package ir.speaking.feature.admin.auth

import io.ktor.server.auth.Principal
import java.util.UUID

data class AdminPrincipal(
    val id: UUID,
    val username: String,
    val fullName: String,
    val role: String
) : Principal
