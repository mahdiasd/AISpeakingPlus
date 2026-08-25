package ir.speaking.core.redis.ai

import ir.speaking.core.network.model.AiPlatformConfig

interface AiConfigRedisRepository {
    suspend fun setConfig(config: AiPlatformConfig)
    suspend fun getConfig(): AiPlatformConfig?
    suspend fun clearConfig()
}