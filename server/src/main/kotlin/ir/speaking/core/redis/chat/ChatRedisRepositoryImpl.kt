package ir.speaking.core.redis.chat

import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.chat.model.Chat
import ir.speaking.feature.chat.model.Role
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient
import org.redisson.api.StreamMessageId
import org.redisson.api.stream.StreamAddArgs
import java.time.Duration

@Single
class ChatRedisRepositoryImpl(
    private val redissonClient: RedissonClient,
) : ChatRedisRepository {

    override suspend fun addChat(chat: Chat) {
        val streamKey = "chat:${chat.userId}:${chat.scenarioId}"
        val stream = redissonClient.getStream<String, String>(streamKey)

        // Add message to stream with "message" field
        stream.add(
            StreamAddArgs.entries(
                mapOf(
                    "message" to chat.message,
                    "role" to chat.role.name
                )
            )
        )

        // Set TTL of 2 hours for the entire stream
        stream.expire(Duration.ofHours(2))
    }

    override suspend fun clearChats(userId: String, scenarioId: String) {
        val streamKey = "chat:$userId:$scenarioId"
        val stream = redissonClient.getStream<String, String>(streamKey)
        if (stream.size() > 0) {
            stream.delete()
        }
    }

    override suspend fun getChats(userId: String, scenarioId: String): List<Chat> {
        val streamKey = "chat:$userId:$scenarioId"
        val stream = redissonClient.getStream<String, String>(streamKey)

        // Read all messages from the beginning to end of stream
        val messages = stream.range(StreamMessageId.MIN, StreamMessageId.MAX)

        return messages.entries.map { (_, data) ->
            Chat(
                userId = userId,
                scenarioId = scenarioId,
                message = data["message"] ?: "",
                role = data["role"]?.let {
                    Role.valueOf(it)
                } ?: Role.User // Default to User if missing
            )
        }
    }

    override suspend fun getSystemPrompt(userId: String, scenarioId: String, isChallenge: Boolean): String {
        return MyConstant.BASE_PROMPT
            .replace("[AI_NAME]", "Assistant")
            .replace("[AI_ROLE]", "Conversation Partner")
            .replace("[SCENARIO_DESCRIPTION]", "Free conversation practice")
            .replace("[STARTER_ROLE]", "AI")
            .replace("[USER_TASKS]", "Practice English conversation naturally")
            .trimIndent()
    }


}