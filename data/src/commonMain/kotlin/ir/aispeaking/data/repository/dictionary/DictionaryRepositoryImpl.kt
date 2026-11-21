package ir.aispeaking.data.repository.dictionary

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.aispeaking.data.mapper.dictionary.toDomain
import ir.aispeaking.data.model.dictionary.DictionaryEntryDto
import ir.aispeaking.data.model.dictionary.DictionaryLookupRequestDto
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.domain.repository.dictionary.DictionaryRepository
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class DictionaryRepositoryImpl(
    private val client: HttpClient
) : DictionaryRepository {

    private val baseUrl: String
        get() = NetworkConfig.baseUrl

    override suspend fun lookupWord(word: String, sentenceContext: String?): DataResult<DictionaryEntry> {
        val result = safeCall {
            client.post("$baseUrl/api/dictionary/lookup") {
                contentType(ContentType.Application.Json)
                setBody(DictionaryLookupRequestDto(word = word, context = sentenceContext))
            }.body<NetworkResponse<DictionaryEntryDto>>()
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
}
