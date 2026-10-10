package ir.speaking.feature.admin.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import ir.speaking.feature.admin.db.AdminUserTable
import ir.speaking.feature.admin.model.AdminInfoDto
import ir.speaking.feature.admin.model.AdminLoginRequest
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AdminAuthService {

    data class AuthenticatedAdmin(
        val id: UUID,
        val username: String,
        val fullName: String,
        val role: String
    )

    suspend fun login(request: AdminLoginRequest): AuthenticatedAdmin? = newSuspendedTransaction(Dispatchers.IO) {
        val inputUser = request.username.trim().lowercase()
        val row = AdminUserTable
            .selectAll()
            .where { AdminUserTable.username eq inputUser }
            .firstOrNull() ?: return@newSuspendedTransaction null

        val isActive = row[AdminUserTable.isActive]
        if (!isActive) return@newSuspendedTransaction null

        val storedHash = row[AdminUserTable.passwordHash]
        val verifyResult = BCrypt.verifyer().verify(request.password.toCharArray(), storedHash.toCharArray())
        if (!verifyResult.verified) return@newSuspendedTransaction null

        AuthenticatedAdmin(
            id = row[AdminUserTable.id].value,
            username = row[AdminUserTable.username],
            fullName = row[AdminUserTable.fullName],
            role = row[AdminUserTable.role]
        )
    }

    suspend fun getProfile(adminId: UUID): AdminInfoDto? = newSuspendedTransaction(Dispatchers.IO) {
        val row = AdminUserTable
            .selectAll()
            .where { AdminUserTable.id eq adminId }
            .singleOrNull() ?: return@newSuspendedTransaction null

        AdminInfoDto(
            id = row[AdminUserTable.id].value.toString(),
            username = row[AdminUserTable.username],
            fullName = row[AdminUserTable.fullName],
            role = row[AdminUserTable.role]
        )
    }
}
