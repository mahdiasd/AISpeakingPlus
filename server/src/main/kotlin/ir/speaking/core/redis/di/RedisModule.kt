package ir.speaking.core.redis.di

import org.koin.core.annotation.Single
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory

class RedisClientHolder(val client: RedissonClient?)

@Single
fun provideRedis(): RedisClientHolder {
    val redisHost = System.getenv("REDIS_HOST") ?: "127.0.0.1"
    val redisPort = System.getenv("REDIS_PORT")?.toInt() ?: 6379

    val client = try {
        val config = Config()
        config.useSingleServer()
            .setAddress("redis://$redisHost:$redisPort")
            .setConnectTimeout(1500)
            .setTimeout(1500)
            .setRetryAttempts(1)

        Redisson.create(config)
    } catch (e: Exception) {
        LoggerFactory.getLogger("ir.speaking.core.redis")
            .warn("Redis unavailable at $redisHost:$redisPort, using in-memory fallback: ${e.message}")
        null
    }
    return RedisClientHolder(client)
}
