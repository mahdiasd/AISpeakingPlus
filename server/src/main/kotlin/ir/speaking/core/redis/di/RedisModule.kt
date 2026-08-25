package ir.speaking.core.redis.di

import org.koin.core.annotation.Single
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

@Single
fun provideRedis(): RedissonClient {
    val redisHost = System.getenv("REDIS_HOST") ?: "127.0.0.1"
    val redisPort = System.getenv("REDIS_PORT")?.toInt() ?: 6379

    val config = Config()
    config.useSingleServer().address = "redis://$redisHost:$redisPort"

    return Redisson.create(config)
}
