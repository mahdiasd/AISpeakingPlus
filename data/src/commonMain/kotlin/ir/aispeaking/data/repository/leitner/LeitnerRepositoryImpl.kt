package ir.aispeaking.data.repository.leitner

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.aispeaking.data.mapper.leitner.toDomain
import ir.aispeaking.data.model.leitner.AddFlashcardRequestDto
import ir.aispeaking.data.model.leitner.CardReviewRequestDto
import ir.aispeaking.data.model.leitner.FlashcardDto
import ir.aispeaking.data.model.leitner.LeitnerReminderRequestDto
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class LeitnerRepositoryImpl(
    private val client: HttpClient
) : LeitnerRepository {

    private val baseUrl: String
        get() = NetworkConfig.baseUrl

    override suspend fun addCard(
        lexiconId: String?,
        customWord: String?,
        customMeaning: String?,
        customExample: String?,
        customAudioUrl: String?
    ): DataResult<Flashcard> {
        val request = AddFlashcardRequestDto(
            lexiconId = lexiconId,
            customWord = customWord,
            customMeaning = customMeaning,
            customExample = customExample,
            customAudioUrl = customAudioUrl
        )

        val result = safeCall {
            client.post("$baseUrl/api/leitner/cards") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<NetworkResponse<FlashcardDto>>()
        }

        return when (result) {
            is DataResult.Success -> DataResult.Success(
                data = result.data.toDomain(),
                pagingMeta = result.pagingMeta,
                message = result.message
            )
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun getDueCards(limit: Int): DataResult<List<Flashcard>> {
        val result = safeCall {
            client.get("$baseUrl/api/leitner/cards") {
                parameter("due", "true")
                parameter("limit", limit.toString())
            }.body<NetworkResponse<List<FlashcardDto>>>()
        }

        return when (result) {
            is DataResult.Success -> DataResult.Success(
                data = result.data.map { it.toDomain() },
                pagingMeta = result.pagingMeta,
                message = result.message
            )
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun getCardsByBox(boxNumber: Int): DataResult<List<Flashcard>> {
        val result = safeCall {
            client.get("$baseUrl/api/leitner/cards") {
                parameter("box", boxNumber.toString())
            }.body<NetworkResponse<List<FlashcardDto>>>()
        }

        return when (result) {
            is DataResult.Success -> DataResult.Success(
                data = result.data.map { it.toDomain() },
                pagingMeta = result.pagingMeta,
                message = result.message
            )
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun getAllCards(): DataResult<List<Flashcard>> {
        val result = safeCall {
            client.get("$baseUrl/api/leitner/cards").body<NetworkResponse<List<FlashcardDto>>>()
        }

        return when (result) {
            is DataResult.Success -> DataResult.Success(
                data = result.data.map { it.toDomain() },
                pagingMeta = result.pagingMeta,
                message = result.message
            )
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun submitCardReview(cardId: String, remembered: Boolean): DataResult<Flashcard> {
        val result = safeCall {
            client.post("$baseUrl/api/leitner/review") {
                contentType(ContentType.Application.Json)
                setBody(CardReviewRequestDto(cardId = cardId, remembered = remembered))
            }.body<NetworkResponse<FlashcardDto>>()
        }

        return when (result) {
            is DataResult.Success -> DataResult.Success(
                data = result.data.toDomain(),
                pagingMeta = result.pagingMeta,
                message = result.message
            )
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun deleteCard(cardId: String): DataResult<Boolean> {
        val result = safeCall {
            client.delete("$baseUrl/api/leitner/cards/$cardId").body<NetworkResponse<Boolean>>()
        }

        return when (result) {
            is DataResult.Success -> DataResult.Success(
                data = result.data,
                pagingMeta = result.pagingMeta,
                message = result.message
            )
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun getBoxCounts(): DataResult<Map<Int, Long>> {
        val result = safeCall {
            client.get("$baseUrl/api/leitner/box-counts").body<NetworkResponse<Map<String, Long>>>()
        }

        return when (result) {
            is DataResult.Success -> {
                val map = result.data.mapNotNull { (k, v) ->
                    k.toIntOrNull()?.let { it to v }
                }.toMap()
                DataResult.Success(
                    data = map,
                    pagingMeta = result.pagingMeta,
                    message = result.message
                )
            }
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun getReminderTime(): DataResult<String?> {
        val result = safeCall {
            client.get("$baseUrl/api/leitner/reminder-time").body<NetworkResponse<String?>>()
        }

        return when (result) {
            is DataResult.Success -> DataResult.Success(
                data = result.data,
                pagingMeta = result.pagingMeta,
                message = result.message
            )
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun setReminderTime(time: String): DataResult<Boolean> {
        val result = safeCall {
            client.post("$baseUrl/api/leitner/reminder-time") {
                contentType(ContentType.Application.Json)
                setBody(LeitnerReminderRequestDto(reminderTime = time))
            }.body<NetworkResponse<Boolean>>()
        }

        return when (result) {
            is DataResult.Success -> DataResult.Success(
                data = result.data,
                pagingMeta = result.pagingMeta,
                message = result.message
            )
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }
}
