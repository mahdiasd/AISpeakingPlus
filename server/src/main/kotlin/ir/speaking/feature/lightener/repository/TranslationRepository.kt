package ir.speaking.feature.lightener.repository

import ir.speaking.core.response.PagedList
import ir.speaking.feature.lightener.dto.UpsertTranslationRequest
import ir.speaking.feature.lightener.model.Translation
import java.util.*

interface TranslationRepository {
    suspend fun create(userId: UUID, request: UpsertTranslationRequest): Translation
    suspend fun update(userId: UUID, request: UpsertTranslationRequest): Translation?
    suspend fun getPagedTranslation(userId: UUID, page: Int, searchText: String?, pageSize: Int): PagedList<Translation>
    suspend fun deleteTranslation(userId: UUID, id: UUID): Boolean
    suspend fun getBySourceText(sourceText: String, userId: UUID): Translation?
}