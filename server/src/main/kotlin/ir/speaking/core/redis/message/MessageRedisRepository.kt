package ir.speaking.core.redis.message

interface MessageRedisRepository {
    suspend fun addMessage(messageDto: MessageDto)

    suspend fun clear()

    suspend fun getMessage(): MessageDto?
}