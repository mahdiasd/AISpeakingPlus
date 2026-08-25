package ir.speaking.admin.admin.repository

import ir.speaking.admin.admin.model.Admin

interface AdminRepository {
    suspend fun createAdmin(username: String, password: String): Admin

    suspend fun login(username: String, password: String): Admin

    suspend fun isAdminExist(): Boolean
}