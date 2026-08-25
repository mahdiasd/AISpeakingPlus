package ir.speaking.feature.user.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.user.db.UserDAO
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.user.db.toModel
import ir.speaking.feature.user.model.User
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.koin.core.annotation.Single
import java.util.*

@Single
class UserRepositoryImpl : UserRepository {
    override suspend fun getUserById(id: UUID): User? =
        suspendTransaction {
            UserDAO.findById(id)?.toModel()
        }

    override suspend fun getUserByMobile(mobile: String) =
        suspendTransaction {
            UserDAO.find { UserTable.mobile eq mobile }
                .firstOrNull()?.toModel()
        }

    override suspend fun createUser(user: User): User =
        suspendTransaction {
            UserDAO.new {
                nickName = user.nickName
                firstName = user.firstName
                lastName = user.lastName
                mobile = user.mobile
                gender = user.gender?.name
                age = user.age
                score = user.score
                languageLevel = user.languageLevel.toString()
                active = user.active
                avatar = user.avatar
                createdAt = Clock.System.now()
                updatedAt = Clock.System.now()
            }.toModel()
        }

    override suspend fun updateUser(user: User): User? {
        return suspendTransaction {
            UserDAO.findByIdAndUpdate(
                id = user.uid,
                block = {
                    it.nickName = user.nickName
                    it.firstName = user.firstName
                    it.lastName = user.lastName
                    it.mobile = user.mobile
                    it.gender = user.gender?.name
                    it.age = user.age
                    it.score = user.score
                    it.languageLevel = user.languageLevel.toString()
                    it.active = user.active
                    it.avatar = user.avatar
                    it.updatedAt = Clock.System.now()
                }
            )?.toModel()
        }
    }

    override suspend fun addScore(userId: UUID, score: Int): User? {
        return suspendTransaction {
            UserDAO.findByIdAndUpdate(
                id = userId,
                block = {
                    it.score += score
                }
            )?.toModel()
        }
    }

    override suspend fun getTopTen(): List<User> =
        suspendTransaction {
            UserDAO
                .all()
                .orderBy(UserTable.score to SortOrder.DESC)
                .limit(10)
                .map { it.toModel() }
        }

}