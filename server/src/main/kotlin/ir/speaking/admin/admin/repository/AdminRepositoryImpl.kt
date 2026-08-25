package ir.speaking.admin.admin.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import ir.speaking.admin.admin.db.AdminDAO
import ir.speaking.admin.admin.db.AdminTable
import ir.speaking.admin.admin.db.toModel
import ir.speaking.admin.admin.model.Admin
import ir.speaking.core.exeptions.AppException
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

@Single
class AdminRepositoryImpl : AdminRepository {

    override suspend fun createAdmin(username: String, password: String): Admin {
        return newSuspendedTransaction {
            AdminDAO.new {
                this.username = username
                this.passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            }.toModel()
        }
    }

    override suspend fun login(username: String, password: String): Admin {
        return newSuspendedTransaction {
            val model = AdminDAO.find { AdminTable.username eq username }.firstOrNull()
                ?: throw AppException.UnauthorizedAccess()

            if (validatePassword(password, model.passwordHash)) {
                model.toModel()
            } else {
                throw AppException.UnauthorizedAccess()
            }
        }
    }

    private fun validatePassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer()
            .verify(password.toCharArray(), hashedPassword)
            .verified
    }


    override suspend fun isAdminExist(): Boolean {
        return newSuspendedTransaction { AdminDAO.count() > 0 }
    }
}