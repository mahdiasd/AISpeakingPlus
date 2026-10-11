package ir.speaking.feature.user.service

import ir.speaking.core.network.api.SmsApiService
import ir.speaking.core.redis.di.RedisClientHolder
import ir.speaking.feature.user.dto.AuthResponse
import ir.speaking.feature.user.dto.SendOtpResponse
import ir.speaking.feature.user.dto.UserProfileResponse
import ir.speaking.feature.user.repository.UserRepo
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class RateLimitExceededException(message: String) : RuntimeException(message)
class AccountSuspendedException(message: String) : RuntimeException(message)

@Single
class AuthService(
    private val userRepo: UserRepo,
    private val smsApiService: SmsApiService,
    redisClientHolder: RedisClientHolder
) {
    constructor(
        userRepo: UserRepo,
        smsApiService: SmsApiService,
        redissonClient: RedissonClient? = null
    ) : this(userRepo, smsApiService, RedisClientHolder(redissonClient))

    private val redissonClient: RedissonClient? = redisClientHolder.client

    companion object {
        const val BYPASS_MOBILE = "09152413498"
        const val BYPASS_OTP = "87799"
        const val OTP_TTL_SECONDS = 120L
        const val RATE_LIMIT_MAX_REQUESTS = 5
        const val RATE_LIMIT_WINDOW_MINUTES = 15L
        const val RATE_LIMIT_WINDOW_MILLIS = RATE_LIMIT_WINDOW_MINUTES * 60 * 1000L
    }

    private val logger = LoggerFactory.getLogger("ir.speaking.feature.auth")

    // Fallback in-memory caches used only when Redis daemon is unreachable (e.g., local/CI unit tests)
    private val otpCache = ConcurrentHashMap<String, Pair<String, Long>>()
    private val rateLimitCache = ConcurrentHashMap<String, MutableList<Long>>()

    suspend fun sendOtp(mobile: String): SendOtpResponse {
        val cleanMobile = mobile.trim()

        if (cleanMobile == BYPASS_MOBILE) {
            logger.info("Test bypass mobile {} detected; skipping SMS and using static OTP {}", cleanMobile, BYPASS_OTP)
            return SendOtpResponse(mobile = cleanMobile, expiresInSeconds = OTP_TTL_SECONDS.toInt())
        }

        checkAndIncrementRateLimit(cleanMobile)

        val otpCode = smsApiService.generateOTP(5)
        storeOtpCode(cleanMobile, otpCode)
        logger.info("OTP generated for {} (valid for {}s)", cleanMobile, OTP_TTL_SECONDS)

        try {
            smsApiService.sendMessage(cleanMobile, otpCode)
        } catch (e: Exception) {
            logger.warn("SMS sending failed or skipped for {}: {}", cleanMobile, e.message)
        }

        return SendOtpResponse(mobile = cleanMobile, expiresInSeconds = OTP_TTL_SECONDS.toInt())
    }

    private fun checkAndIncrementRateLimit(mobile: String) {
        val redisHandled = try {
            if (redissonClient != null) {
                val key = "otp:ratelimit:$mobile"
                val counter = redissonClient.getAtomicLong(key)
                val currentCount = counter.incrementAndGet()
                if (currentCount == 1L) {
                    counter.expire(Duration.ofMinutes(RATE_LIMIT_WINDOW_MINUTES))
                }
                if (currentCount > RATE_LIMIT_MAX_REQUESTS) {
                    throw RateLimitExceededException("تعداد درخواست‌های پیامک بیش از حد مجاز است (حداکثر ۵ پیامک در ۱۵ دقیقه). لطفاً کمی صبر کنید.")
                }
                true
            } else {
                false
            }
        } catch (e: RateLimitExceededException) {
            throw e
        } catch (e: Exception) {
            logger.warn("Redis rate limit check failed for {}, using fallback rate limiter: {}", mobile, e.message)
            false
        }

        if (!redisHandled) {
            val now = System.currentTimeMillis()
            val windowStart = now - RATE_LIMIT_WINDOW_MILLIS
            val timestamps = rateLimitCache.computeIfAbsent(mobile) { mutableListOf() }
            synchronized(timestamps) {
                timestamps.removeAll { it < windowStart }
                if (timestamps.size >= RATE_LIMIT_MAX_REQUESTS) {
                    throw RateLimitExceededException("تعداد درخواست‌های پیامک بیش از حد مجاز است (حداکثر ۵ پیامک در ۱۵ دقیقه). لطفاً کمی صبر کنید.")
                }
                timestamps.add(now)
            }
        }
    }

    private fun storeOtpCode(mobile: String, otpCode: String) {
        val expiresAt = System.currentTimeMillis() + (OTP_TTL_SECONDS * 1000L)
        otpCache[mobile] = Pair(otpCode, expiresAt)

        try {
            redissonClient?.getBucket<String>("otp:code:$mobile")
                ?.set(otpCode, Duration.ofSeconds(OTP_TTL_SECONDS))
        } catch (e: Exception) {
            logger.warn("Redis OTP store failed for {}, using fallback cache: {}", mobile, e.message)
        }
    }

    private fun verifyAndConsumeStoredOtp(mobile: String, code: String): Boolean {
        try {
            if (redissonClient != null) {
                val bucket = redissonClient.getBucket<String>("otp:code:$mobile")
                val stored = bucket.get()
                return if (stored != null && stored == code) {
                    bucket.delete()
                    otpCache.remove(mobile)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            logger.warn("Redis OTP read failed for {}, checking fallback cache: {}", mobile, e.message)
        }

        val cached = otpCache[mobile]
        val matched = cached != null && cached.first == code && cached.second > System.currentTimeMillis()
        if (matched) {
            otpCache.remove(mobile)
        }
        return matched
    }

    fun getStoredOtpForTesting(mobile: String): String? {
        val cleanMobile = mobile.trim()
        if (cleanMobile == BYPASS_MOBILE) return BYPASS_OTP
        try {
            val redisVal = redissonClient?.getBucket<String>("otp:code:$cleanMobile")?.get()
            if (redisVal != null) return redisVal
        } catch (_: Exception) {
        }
        return otpCache[cleanMobile]?.takeIf { it.second > System.currentTimeMillis() }?.first
    }

    suspend fun verifyOtp(
        mobile: String,
        otpCode: String,
        tokenGenerator: (uid: String) -> String
    ): AuthResponse? {
        val cleanMobile = mobile.trim()
        val cleanCode = otpCode.trim()

        val isValidCode = if (cleanMobile == BYPASS_MOBILE) {
            cleanCode == BYPASS_OTP || verifyAndConsumeStoredOtp(cleanMobile, cleanCode)
        } else {
            verifyAndConsumeStoredOtp(cleanMobile, cleanCode)
        }

        if (!isValidCode) {
            return null
        }

        if (userRepo.isUserSuspendedByMobile(cleanMobile)) {
            throw AccountSuspendedException("حساب کاربری شما تعلیق شده است.")
        }

        val user = userRepo.findOrCreateUserByMobile(cleanMobile)
        val token = tokenGenerator(user.id)

        return AuthResponse(
            token = token,
            user = user
        )
    }

    suspend fun isUserSuspended(userId: java.util.UUID): Boolean {
        return userRepo.isUserSuspended(userId)
    }

    suspend fun getUserProfile(userId: java.util.UUID): UserProfileResponse? {
        return userRepo.getUserProfile(userId)
    }
}
