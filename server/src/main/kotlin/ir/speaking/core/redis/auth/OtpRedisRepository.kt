package ir.speaking.core.redis.auth

interface OtpRedisRepository {
    suspend fun saveOtp(mobile: String, otp: String)
    suspend fun readOtp(mobile: String): String?
}