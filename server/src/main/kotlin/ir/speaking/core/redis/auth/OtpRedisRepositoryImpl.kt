package ir.speaking.core.redis.auth

import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient
import java.time.Duration

@Single
class OtpRedisRepositoryImpl(private val redissonClient: RedissonClient) : OtpRedisRepository {
    override suspend fun saveOtp(mobile: String, otp: String) {
        val bucket = redissonClient.getBucket<String>("otp:$mobile")
        bucket.set(otp, Duration.ofMinutes(1))
    }

    override suspend fun readOtp(mobile: String): String? {
        val bucket = redissonClient.getBucket<String>("otp:$mobile")
        return bucket.get()
    }
}