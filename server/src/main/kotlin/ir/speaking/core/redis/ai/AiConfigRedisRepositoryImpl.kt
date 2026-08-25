package ir.speaking.core.redis.ai

import ir.speaking.core.network.model.AiPlatformConfig
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient

@Single
class AiConfigRedisRepositoryImpl(
    private val redissonClient: RedissonClient
) : AiConfigRedisRepository {
    
    private val bucketKey = "ai_platform_config"

    override suspend fun setConfig(config: AiPlatformConfig) {
        val bucket = redissonClient.getBucket<AiPlatformConfig>(bucketKey)
        bucket.set(config)
    }

    override suspend fun getConfig(): AiPlatformConfig? {
        return redissonClient.getBucket<AiPlatformConfig>(bucketKey).get()
    }

    override suspend fun clearConfig() {
        redissonClient.getBucket<AiPlatformConfig>(bucketKey).delete()
    }
}