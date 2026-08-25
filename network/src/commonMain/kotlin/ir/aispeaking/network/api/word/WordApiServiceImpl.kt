package ir.aispeaking.network.api.word

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import ir.aispeaking.network.dto.word.DailyWordResponse
import ir.aispeaking.network.dto.word.WordProgressResponse
import ir.aispeaking.network.model.NetworkResponse
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Single

@Single
class WordApiServiceImpl(private val httpClient: HttpClient) : WordApiService {

    override suspend fun getDailyWord() =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/word")
        }.body<NetworkResponse<DailyWordResponse>>()

    override suspend fun createWordProgress(
        wordId: String,
        selectedOptionIndex: Int,
        isCorrect: Boolean,
        wordScore: Int
    ): NetworkResponse<WordProgressResponse> =
        httpClient.post {
            url(platformBaseUrl() + "api/v1/word/progress")
            setBody(buildJsonObject {
                put("wordId", wordId)
                put("selectedOptionIndex", selectedOptionIndex)
                put("wordScore", wordScore)
                put("isCorrect", isCorrect)
            })
        }.body<NetworkResponse<WordProgressResponse>>()

}