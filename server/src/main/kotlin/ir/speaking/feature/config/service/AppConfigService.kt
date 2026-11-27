package ir.speaking.feature.config.service

import ir.speaking.core.redis.di.RedisClientHolder
import ir.speaking.feature.config.dto.AppVersionConfigDto
import ir.speaking.feature.config.dto.UpdateAppVersionConfigRequest
import ir.speaking.feature.config.dto.UpdateResponseDto
import ir.speaking.feature.config.repository.AppVersionConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.time.Duration

@Single
open class AppConfigService(
    private val repository: AppVersionConfigRepository,
    redisClientHolder: RedisClientHolder
) {
    constructor(
        repository: AppVersionConfigRepository = AppVersionConfigRepository(),
        redissonClient: RedissonClient? = null
    ) : this(repository, RedisClientHolder(redissonClient))

    private val logger = LoggerFactory.getLogger(AppConfigService::class.java)
    private val redissonClient: RedissonClient? = redisClientHolder.client
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    companion object {
        const val DEFAULT_PLATFORM = "android"
        const val DEFAULT_LAST_VERSION = 10
        const val DEFAULT_MIN_VERSION = 10
        const val DEFAULT_SUGGEST_VERSION = 10
        const val DEFAULT_LINK = "https://cafebazaar.ir/app/ir.aispeaking"
        const val DEFAULT_MESSAGE =
            "نسخه جدید AI Speaking با مراحل داستانی و هوش مصنوعی پیشرفته منتشر شد. برای ادامه لطفاً برنامه را بروزرسانی نمایید."

        private const val REDIS_KEY_PREFIX = "app:config:version:"
        private const val CACHE_TTL_MINUTES = 10L
    }

    fun defaultConfig(platform: String = DEFAULT_PLATFORM): AppVersionConfigDto {
        return AppVersionConfigDto(
            platform = platform,
            lastVersion = DEFAULT_LAST_VERSION,
            minVersion = DEFAULT_MIN_VERSION,
            suggestVersion = DEFAULT_SUGGEST_VERSION,
            link = DEFAULT_LINK,
            message = DEFAULT_MESSAGE,
            updatedAt = null
        )
    }

    open suspend fun getAppVersionConfig(platform: String = DEFAULT_PLATFORM): AppVersionConfigDto {
        val normalizedPlatform = platform.trim().lowercase().ifBlank { DEFAULT_PLATFORM }

        readFromCache(normalizedPlatform)?.let { return it }

        val fromDb = repository.findByPlatform(normalizedPlatform)
        if (fromDb != null) {
            writeToCache(normalizedPlatform, fromDb)
            return fromDb
        }

        return defaultConfig(normalizedPlatform)
    }

    open suspend fun getUpdateResponseDto(platform: String = DEFAULT_PLATFORM): UpdateResponseDto {
        val config = getAppVersionConfig(platform)
        return UpdateResponseDto(
            forceVersion = config.minVersion,
            lastVersion = config.lastVersion,
            suggestVersion = config.suggestVersion,
            link = config.link,
            message = config.message
        )
    }

    open suspend fun updateAppVersionConfig(
        req: UpdateAppVersionConfigRequest,
        platform: String = DEFAULT_PLATFORM
    ): AppVersionConfigDto {
        val normalizedPlatform = platform.trim().lowercase().ifBlank { DEFAULT_PLATFORM }
        val trimmedLink = req.link.trim()
        val trimmedMessage = req.message.trim()

        require(req.lastVersion >= 1) {
            "آخرین نسخه منتشرشده باید عددی بزرگ‌تر از صفر باشد"
        }
        require(req.minVersion >= 0) {
            "حداقل نسخه مجاز (اجباری) نمی‌تواند منفی باشد"
        }
        require(req.suggestVersion >= 0) {
            "نسخه پیشنهادی نمی‌تواند منفی باشد"
        }
        require(req.minVersion <= req.lastVersion) {
            "حداقل نسخه مجاز (اجباری) نمی‌تواند از آخرین نسخه منتشرشده بزرگ‌تر باشد"
        }
        require(req.suggestVersion <= req.lastVersion) {
            "نسخه پیشنهادی نمی‌تواند از آخرین نسخه منتشرشده بزرگ‌تر باشد"
        }
        require(
            trimmedLink.isNotBlank() &&
                (trimmedLink.startsWith("https://") || trimmedLink.startsWith("http://"))
        ) {
            "لینک دانلود معتبر نیست و باید با http:// یا https:// شروع شود"
        }
        require(trimmedMessage.isNotBlank()) {
            "متن پیام به‌روزرسانی نمی‌تواند خالی باشد"
        }

        val normalizedReq = req.copy(
            link = trimmedLink,
            message = trimmedMessage
        )

        val saved = repository.upsert(normalizedPlatform, normalizedReq)
        writeToCache(normalizedPlatform, saved)
        return saved
    }

    open suspend fun invalidateCache(platform: String = DEFAULT_PLATFORM) {
        val client = redissonClient ?: return
        val normalizedPlatform = platform.trim().lowercase().ifBlank { DEFAULT_PLATFORM }
        withContext(Dispatchers.IO) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_KEY_PREFIX + normalizedPlatform)
                bucket.delete()
            } catch (e: Exception) {
                logger.warn("Redis cache delete error for platform '$normalizedPlatform': ${e.message}")
            }
        }
    }

    private suspend fun readFromCache(platform: String): AppVersionConfigDto? {
        val client = redissonClient ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_KEY_PREFIX + platform)
                val cachedJson = bucket.get()
                if (!cachedJson.isNullOrBlank()) {
                    json.decodeFromString<AppVersionConfigDto>(cachedJson)
                } else {
                    null
                }
            } catch (e: Exception) {
                logger.warn("Redis cache read error for platform '$platform': ${e.message}")
                null
            }
        }
    }

    private suspend fun writeToCache(platform: String, config: AppVersionConfigDto) {
        val client = redissonClient ?: return
        withContext(Dispatchers.IO) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_KEY_PREFIX + platform)
                bucket.set(json.encodeToString(config), Duration.ofMinutes(CACHE_TTL_MINUTES))
            } catch (e: Exception) {
                logger.warn("Redis cache write error for platform '$platform': ${e.message}")
            }
        }
    }
}
