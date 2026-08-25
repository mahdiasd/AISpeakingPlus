package ir.aispeaking.domain.repository.translation

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.translate.Translation
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    suspend fun translate(sourceText: String): Flow<Result<Translation>>

    suspend fun createTranslate(translation: Translation): Flow<DataResult<Translation>>

    suspend fun updateTranslate(translation: Translation): Flow<DataResult<Translation>>

    suspend fun delete(uid: String): Flow<DataResult<Boolean>>

    suspend fun getTranslations(
        searchText: String?,
        page: Int,
        pageSize: Int? = null
    ): Flow<DataResult<Paging<Translation>>>
}