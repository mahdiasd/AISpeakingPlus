package ir.aispeaking.network.api.translation

import ir.aispeaking.network.dto.translate.TranslationResponse
import ir.aispeaking.network.dto.translate.UpsertTranslationRequest
import ir.aispeaking.network.model.NetworkResponse


interface TranslationApiService {
    suspend fun create(upsertTranslationRequest: UpsertTranslationRequest):
            NetworkResponse<TranslationResponse>

    suspend fun update(upsertTranslationRequest: UpsertTranslationRequest):
            NetworkResponse<TranslationResponse>

    suspend fun delete(id: String): NetworkResponse<Boolean>

    suspend fun getTranslations(
        searchText: String?,
        page: Int,
        pageSize: Int?
    ): NetworkResponse<List<TranslationResponse>>
}