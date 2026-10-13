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
class AccountBlockedException(message: String) : RuntimeException(message)

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
        const val OTP_COOLDOWN_SECONDS = 60L
        const val MAX_FAILED_ATTEMPTS = 3
        const val BLOCK_DURATION_MINUTES = 5L
        const val ATTEMPTS_EXPIRATION_MINUTES = 5L
        const val RATE_LIMIT_MAX_REQUESTS = 5
        const val RATE_LIMIT_WINDOW_MINUTES = 15L
        const val RATE_LIMIT_WINDOW_MILLIS = RATE_LIMIT_WINDOW_MINUTES * 60 * 1000L

        private val globalTokenBlacklist = ConcurrentHashMap<String, Long>()
        @Volatile
        private var globalInstance: AuthService? = null

        fun isBlacklisted(jti: String): Boolean {
            globalInstance?.let { return it.isTokenBlacklisted(jti) }
            val exp = globalTokenBlacklist[jti] ?: return false
            return exp > System.currentTimeMillis()
        }
    }

    init {
        globalInstance = this
    }

    private val logger = LoggerFactory.getLogger("ir.speaking.feature.auth")

    // Fallback in-memory caches used only when Redis daemon is unreachable (e.g., local/CI unit tests)
    private val otpCache = ConcurrentHashMap<String, Pair<String, Long>>()
    private val rateLimitCache = ConcurrentHashMap<String, MutableList<Long>>()
    private val cooldownCache = ConcurrentHashMap<String, Long>()
    private val attemptsCache = ConcurrentHashMap<String, Pair<Int, Long>>()
    private val blockedCache = ConcurrentHashMap<String, Long>()
    private val tokenBlacklistCache = ConcurrentHashMap<String, Long>()

    suspend fun sendOtp(mobile: String): SendOtpResponse {
        val cleanMobile = mobile.trim()

        if (cleanMobile == BYPASS_MOBILE) {
            logger.info("Test bypass mobile {} detected; skipping SMS and using static OTP {}", cleanMobile, BYPASS_OTP)
            return SendOtpResponse(mobile = cleanMobile, expiresInSeconds = OTP_TTL_SECONDS.toInt())
        }

        if (isMobileBlocked(cleanMobile)) {
            throw AccountBlockedException("این شماره به دلیل تلاش‌های مکرر ناموفق مسدود شده است. لطفاً ۵ دقیقه دیگر دوباره تلاش کنید.")
        }

        if (isMobileInCooldown(cleanMobile)) {
            throw RateLimitExceededException("لطفاً ۶۰ ثانیه قبل از درخواست مجدد صبر کنید")
        }

        checkAndIncrementRateLimit(cleanMobile)

        val otpCode = smsApiService.generateOTP(5)
        storeOtpCode(cleanMobile, otpCode)
        setMobileCooldown(cleanMobile, OTP_COOLDOWN_SECONDS)
        logger.info("OTP generated for {} (valid for {}s)", cleanMobile, OTP_TTL_SECONDS)

        try {
            smsApiService.sendMessage(cleanMobile, otpCode)
        } catch (e: Exception) {
            logger.warn("SMS sending failed or skipped for {}: {}", cleanMobile, e.message)
        }

        return SendOtpResponse(mobile = cleanMobile, expiresInSeconds = OTP_TTL_SECONDS.toInt())
    }

    fun isMobileBlocked(mobile: String): Boolean {
        if (mobile == BYPASS_MOBILE) return false
        try {
            if (redissonClient != null) {
                val isBlocked = redissonClient.getBucket<String>("auth:blocked:$mobile").isExists
                if (isBlocked) return true
            }
        } catch (e: Exception) {
            logger.warn("Redis check blocked failed for {}: {}", mobile, e.message)
        }
        val blockedUntil = blockedCache[mobile] ?: return false
        if (blockedUntil > System.currentTimeMillis()) {
            return true
        } else {
            blockedCache.remove(mobile)
            return false
        }
    }

    fun isMobileInCooldown(mobile: String): Boolean {
        if (mobile == BYPASS_MOBILE) return false
        try {
            if (redissonClient != null) {
                val inCooldown = redissonClient.getBucket<String>("auth:cooldown:$mobile").isExists
                if (inCooldown) return true
            }
        } catch (e: Exception) {
            logger.warn("Redis check cooldown failed for {}: {}", mobile, e.message)
        }
        val cooldownUntil = cooldownCache[mobile] ?: return false
        if (cooldownUntil > System.currentTimeMillis()) {
            return true
        } else {
            cooldownCache.remove(mobile)
            return false
        }
    }

    private fun setMobileCooldown(mobile: String, seconds: Long = OTP_COOLDOWN_SECONDS) {
        if (mobile == BYPASS_MOBILE) return
        try {
            redissonClient?.getBucket<String>("auth:cooldown:$mobile")
                ?.set("active", Duration.ofSeconds(seconds))
        } catch (e: Exception) {
            logger.warn("Redis set cooldown failed for {}: {}", mobile, e.message)
        }
        cooldownCache[mobile] = System.currentTimeMillis() + (seconds * 1000L)
    }

    private fun recordFailedAttempt(mobile: String): Int {
        if (mobile == BYPASS_MOBILE) return 0
        var redisCount: Int? = null
        try {
            if (redissonClient != null) {
                val counter = redissonClient.getAtomicLong("auth:attempts:$mobile")
                val current = counter.incrementAndGet()
                if (current == 1L) {
                    counter.expire(Duration.ofMinutes(ATTEMPTS_EXPIRATION_MINUTES))
                }
                redisCount = current.toInt()
            }
        } catch (e: Exception) {
            logger.warn("Redis record failed attempt failed for {}: {}", mobile, e.message)
        }

        val now = System.currentTimeMillis()
        val expiry = now + (ATTEMPTS_EXPIRATION_MINUTES * 60 * 1000L)
        val updatedCount = attemptsCache.compute(mobile) { _, current ->
            if (current == null || current.second < now) {
                Pair(1, expiry)
            } else {
                Pair(current.first + 1, expiry)
            }
        }?.first ?: 1

        return redisCount ?: updatedCount
    }

    private fun blockMobile(mobile: String, minutes: Long = BLOCK_DURATION_MINUTES) {
        if (mobile == BYPASS_MOBILE) return
        try {
            if (redissonClient != null) {
                redissonClient.getBucket<String>("auth:blocked:$mobile")
                    .set("blocked", Duration.ofMinutes(minutes))
                redissonClient.getAtomicLong("auth:attempts:$mobile").delete()
            }
        } catch (e: Exception) {
            logger.warn("Redis block mobile failed for {}: {}", mobile, e.message)
        }
        blockedCache[mobile] = System.currentTimeMillis() + (minutes * 60 * 1000L)
        attemptsCache.remove(mobile)
    }

    private fun clearMobileAttemptsAndCooldown(mobile: String) {
        try {
            redissonClient?.getAtomicLong("auth:attempts:$mobile")?.delete()
            redissonClient?.getBucket<String>("auth:cooldown:$mobile")?.delete()
        } catch (e: Exception) {
            logger.warn("Redis clear attempts and cooldown failed for {}: {}", mobile, e.message)
        }
        attemptsCache.remove(mobile)
        cooldownCache.remove(mobile)
    }

    fun blacklistToken(jti: String, ttlSeconds: Long) {
        if (jti.isBlank()) return
        val effectiveTtl = if (ttlSeconds > 0) ttlSeconds else 3600L
        try {
            redissonClient?.getBucket<String>("auth:token_blacklist:$jti")
                ?.set("blacklisted", Duration.ofSeconds(effectiveTtl))
        } catch (e: Exception) {
            logger.warn("Redis token blacklist failed for jti {}: {}", jti, e.message)
        }
        val expiresAt = System.currentTimeMillis() + (effectiveTtl * 1000L)
        tokenBlacklistCache[jti] = expiresAt
        globalTokenBlacklist[jti] = expiresAt
    }

    fun isTokenBlacklisted(jti: String): Boolean {
        if (jti.isBlank()) return false
        try {
            if (redissonClient != null) {
                val exists = redissonClient.getBucket<String>("auth:token_blacklist:$jti").isExists
                if (exists) return true
            }
        } catch (e: Exception) {
            logger.warn("Redis token blacklist check failed for jti {}: {}", jti, e.message)
        }
        val expiresAt = tokenBlacklistCache[jti] ?: globalTokenBlacklist[jti] ?: return false
        if (expiresAt > System.currentTimeMillis()) {
            return true
        } else {
            tokenBlacklistCache.remove(jti)
            globalTokenBlacklist.remove(jti)
            return false
        }
    }

    fun resetCachesForTesting() {
        otpCache.clear()
        rateLimitCache.clear()
        cooldownCache.clear()
        attemptsCache.clear()
        blockedCache.clear()
        tokenBlacklistCache.clear()
        globalTokenBlacklist.clear()
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

        if (cleanMobile == BYPASS_MOBILE) {
            val isValidCode = cleanCode == BYPASS_OTP || verifyAndConsumeStoredOtp(cleanMobile, cleanCode)
            if (!isValidCode) {
                return null
            }
            if (userRepo.isUserSuspendedByMobile(cleanMobile)) {
                throw AccountSuspendedException("حساب کاربری شما تعلیق شده است.")
            }
            val user = userRepo.findOrCreateUserByMobile(cleanMobile)
            val token = tokenGenerator(user.id)
            return AuthResponse(token = token, user = user)
        }

        if (isMobileBlocked(cleanMobile)) {
            throw AccountBlockedException("این شماره به دلیل ۳ تلاش ناموفق به مدت ۵ دقیقه مسدود شده است.")
        }

        val isValidCode = verifyAndConsumeStoredOtp(cleanMobile, cleanCode)
        if (!isValidCode) {
            val attempts = recordFailedAttempt(cleanMobile)
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                blockMobile(cleanMobile, BLOCK_DURATION_MINUTES)
                throw AccountBlockedException("تعداد تلاش‌های ناموفق بیش از ۳ بار بوده است. شماره شما به مدت ۵ دقیقه مسدود شد.")
            }
            return null
        }

        clearMobileAttemptsAndCooldown(cleanMobile)

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
