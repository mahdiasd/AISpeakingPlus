package ir.speaking.feature.user.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.user.dto.UserProfileResponse
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.*

@Single
class UserRepo {

    suspend fun findOrCreateUserByMobile(mobile: String): UserProfileResponse = suspendTransaction {
        val existing = UserTable.selectAll()
            .where { UserTable.mobile eq mobile }
            .firstOrNull()

        if (existing != null) {
            UserProfileResponse(
                id = existing[UserTable.id].value.toString(),
                phoneNumber = existing[UserTable.mobile],
                nickName = existing[UserTable.nickName],
                avatar = existing[UserTable.avatar],
                score = existing[UserTable.score]
            )
        } else {
            val newId = UserTable.insertAndGetId { row ->
                row[UserTable.mobile] = mobile
                row[UserTable.nickName] = "Learner"
                row[UserTable.avatar] = "default_avatar"
                row[UserTable.score] = 0
            }

            UserProfileResponse(
                id = newId.value.toString(),
                phoneNumber = mobile,
                nickName = "Learner",
                avatar = "default_avatar",
                score = 0
            )
        }
    }

    suspend fun getUserProfile(userId: UUID): UserProfileResponse? = suspendTransaction {
        UserTable.selectAll()
            .where { UserTable.id eq userId }
            .firstOrNull()
            ?.let { row ->
                UserProfileResponse(
                    id = row[UserTable.id].value.toString(),
                    phoneNumber = row[UserTable.mobile],
                    nickName = row[UserTable.nickName],
                    avatar = row[UserTable.avatar],
                    score = row[UserTable.score]
                )
            }
    }
}
