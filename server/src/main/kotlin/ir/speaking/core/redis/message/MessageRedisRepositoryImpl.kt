package ir.speaking.core.redis.message

import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient
import org.redisson.api.stream.StreamAddArgs
import java.time.Duration


@Single
class MessageRedisRepositoryImpl(
    private val redissonClient: RedissonClient,
) : MessageRedisRepository {
    private val bucketKey = "welcome_message_data"

    override suspend fun addMessage(messageDto: MessageDto) {
        val bucket = redissonClient.getBucket<MessageDto>(bucketKey)
        bucket.set(messageDto)
    }

    override suspend fun getMessage(): MessageDto? {
        return redissonClient.getBucket<MessageDto?>(bucketKey).get()
    }

    override suspend fun clear() {
        redissonClient.getBucket<MessageDto>(bucketKey).delete()
    }
}