package ir.speaking.feature.user.repository

import ir.speaking.feature.user.model.User
import java.util.*

interface UserRepository {

    suspend fun getUserById(id: UUID): User?

    suspend fun getUserByMobile(mobile: String): User?

    suspend fun createUser(user: User): User

    suspend fun updateUser(user: User): User?

    suspend fun getTopTen(): List<User>

    suspend fun addScore(userId: UUID, score: Int): User?
}