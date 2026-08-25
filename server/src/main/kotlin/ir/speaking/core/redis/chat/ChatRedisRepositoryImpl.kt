package ir.speaking.core.redis.chat

import ir.speaking.core.exeptions.AppException
import ir.speaking.core.utils.getPrompt
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.challenge.challenge.repository.ChallengeRepository
import ir.speaking.feature.chat.model.Chat
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.scenario.scenario.repository.ScenarioRepository
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient
import org.redisson.api.StreamMessageId
import org.redisson.api.stream.StreamAddArgs
import java.time.Duration

@Single
class ChatRedisRepositoryImpl(
    private val redissonClient: RedissonClient,
    private val scenarioRepository: ScenarioRepository,
    private val challengeRepository: ChallengeRepository
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
        when (isChallenge) {
            true -> {
                challengeRepository.get(scenarioId)?.let { challenge ->
                    val prompt = challenge.getPrompt()
                    return prompt
                } ?: run{
                    throw AppException.NotFound("challenge not found!")
                }
            }

            false -> {
                scenarioRepository.getScenarioById(id = scenarioId.toUUID())?.let { scenario ->
                    val prompt = scenario.getPrompt()
                    return prompt
                } ?: run {
                    throw AppException.NotFound("scenario not found!")
                }
            }
        }

    }


}