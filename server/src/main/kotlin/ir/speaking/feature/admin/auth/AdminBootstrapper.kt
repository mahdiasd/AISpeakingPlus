package ir.speaking.feature.admin.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.feature.admin.db.AdminUserTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object AdminBootstrapper {

    suspend fun bootstrapSuperAdminIfEmpty() = newSuspendedTransaction(Dispatchers.IO) {
        val existingCount = AdminUserTable.selectAll().count()
        if (existingCount == 0L) {
            val email = System.getenv("ADMIN_INITIAL_EMAIL") ?: "admin@aispeaking.ir"
            val password = System.getenv("ADMIN_INITIAL_PASSWORD") ?: "Admin@123456!"
            val passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())

            AdminUserTable.insert {
                it[username] = email
                it[AdminUserTable.passwordHash] = passwordHash
                it[fullName] = "Super Admin"
                it[role] = "ROLE_SUPER_ADMIN"
                it[isActive] = true
            }

            PrintHelper.info("Initial SuperAdmin created: $email")
        }
    }
}
