package ir.speaking.feature.leitner.service

import ir.speaking.core.redis.di.RedisClientHolder
import ir.speaking.feature.leitner.dto.AddFlashcardRequest
import ir.speaking.feature.leitner.dto.LeitnerStatsResponse
import ir.speaking.feature.leitner.dto.ReminderTimeResponse
import ir.speaking.feature.leitner.model.UserFlashcard
import ir.speaking.feature.leitner.repository.UserFlashcardRepository
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Single
open class LeitnerService(
    private val flashcardRepository: UserFlashcardRepository,
    redisClientHolder: RedisClientHolder
) {
    private val logger = LoggerFactory.getLogger(LeitnerService::class.java)
    private val redissonClient: RedissonClient? = redisClientHolder.client
    private val inMemoryReminders = ConcurrentHashMap<String, ReminderTimeResponse>()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val REDIS_REMINDER_PREFIX = "leitner:reminder:"
        private const val CACHE_TTL_DAYS = 365L

        // Spaced repetition progression intervals (in days)
        val BOX_INTERVAL_DAYS = mapOf(
            1 to 1,
            2 to 3,
            3 to 7,
            4 to 14,
            5 to 30
        )
    }

    open suspend fun getCards(
        userId: UUID,
        box: Int? = null,
        dueOnly: Boolean = false
    ): List<UserFlashcard> {
        val now = Clock.System.now()
        return if (dueOnly) {
            val due = flashcardRepository.getDueCards(userId, now)
            if (box != null) {
                due.filter { it.boxNumber == box }
            } else {
                due
            }
        } else if (box != null) {
            flashcardRepository.getCardsByBox(userId, box)
        } else {
            flashcardRepository.getAllCards(userId)
        }
    }

    open suspend fun getStats(userId: UUID): LeitnerStatsResponse {
        val allCards = flashcardRepository.getAllCards(userId)
        val dueCardsCount = flashcardRepository.countDueCards(userId)
        val boxCounts = flashcardRepository.getBoxCounts(userId)

        return LeitnerStatsResponse(
            totalCards = allCards.size.toLong(),
            dueCards = dueCardsCount,
            boxCounts = boxCounts
        )
    }

    open suspend fun addCard(userId: UUID, request: AddFlashcardRequest): UserFlashcard {
        val lexiconUuid = request.lexiconId?.let {
            try {
                UUID.fromString(it)
            } catch (e: Exception) {
                null
            }
        }

        return flashcardRepository.addCard(
            userId = userId,
            lexiconId = lexiconUuid,
            customWord = request.customWord,
            customMeaning = request.customMeaning,
            customExample = request.customExample,
            customAudioUrl = request.customAudioUrl
        )
    }

    open suspend fun reviewCard(userId: UUID, cardId: UUID, remembered: Boolean): UserFlashcard? {
        return flashcardRepository.updateReviewResult(
            cardId = cardId,
            userId = userId,
            remembered = remembered
        )
    }

    open suspend fun deleteCard(userId: UUID, cardId: UUID): Boolean {
        return flashcardRepository.deleteCard(cardId, userId)
    }

    open suspend fun setReminderTime(
        userId: UUID,
        reminderTime: String,
        enabled: Boolean = true
    ): ReminderTimeResponse {
        val sanitizedTime = reminderTime.trim()
        val result = flashcardRepository.setReminderTime(userId, sanitizedTime, enabled)
        val response = ReminderTimeResponse(reminderTime = result.first, enabled = result.second)

        // Cache in Redis / in-memory
        val client = redissonClient
        if (client != null) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_REMINDER_PREFIX + userId)
                bucket.set(jsonParser.encodeToString(response), Duration.ofDays(CACHE_TTL_DAYS))
            } catch (e: Exception) {
                logger.warn("Redis write error for reminder: ${e.message}")
            }
        }
        inMemoryReminders[userId.toString()] = response
        return response
    }

    open suspend fun getReminderTime(userId: UUID): ReminderTimeResponse {
        val key = userId.toString()
        val client = redissonClient
        if (client != null) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_REMINDER_PREFIX + userId)
                val json = bucket.get()
                if (!json.isNullOrBlank()) {
                    return jsonParser.decodeFromString<ReminderTimeResponse>(json)
                }
            } catch (e: Exception) {
                logger.warn("Redis read error for reminder: ${e.message}")
            }
        }

        val inMem = inMemoryReminders[key]
        if (inMem != null) return inMem

        val dbResult = flashcardRepository.getReminderTime(userId)
        val response = ReminderTimeResponse(reminderTime = dbResult.first, enabled = dbResult.second)

        if (client != null) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_REMINDER_PREFIX + userId)
                bucket.set(jsonParser.encodeToString(response), Duration.ofDays(CACHE_TTL_DAYS))
            } catch (e: Exception) {
                logger.warn("Redis write error for reminder: ${e.message}")
            }
        }
        inMemoryReminders[key] = response
        return response
    }
}
