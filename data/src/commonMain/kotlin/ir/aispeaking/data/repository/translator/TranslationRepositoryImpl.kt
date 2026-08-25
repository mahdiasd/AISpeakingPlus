package ir.aispeaking.data.repository.translator

import ir.aispeaking.data.mapper.paginate.toPaging
import ir.aispeaking.data.mapper.translation.toDomain
import ir.aispeaking.data.mapper.translation.toRequest
import ir.aispeaking.data.translate.TranslationService
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.DeviceError
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.domain.repository.translation.TranslationRepository
import ir.aispeaking.network.api.translation.TranslationApiService
import ir.aispeaking.utils.constant.AppConstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class TranslationRepositoryImpl(
    private val service: TranslationService,
    private val apiService: TranslationApiService,
) : TranslationRepository {

    override suspend fun translate(sourceText: String): Flow<Result<Translation>> = flow {
        TODO("implement translator")
    }

    override suspend fun createTranslate(translation: Translation) = flow {
        when (val result = safeCall { apiService.create(translation.toRequest()) }) {
            is DataResult.Success -> {
                AppConstant.needToFetchTranslations = true
                emit(DataResult.Success(result.data.toDomain()))
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun updateTranslate(translation: Translation) = flow {
        when (val result = safeCall { apiService.update(translation.toRequest()) }) {
            is DataResult.Success -> {
                AppConstant.needToFetchTranslations = true
                emit(DataResult.Success(result.data.toDomain()))
            }
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun delete(uid: String) = flow {
        when (val result = safeCall { apiService.delete(uid) }) {
            is DataResult.Success -> {
                AppConstant.needToFetchTranslations = true
                emit(DataResult.Success(result.data))
            }
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun getTranslations(searchText: String?, page: Int, pageSize: Int?) = flow {
        when (val result = safeCall {
            apiService.getTranslations(
                searchText = searchText,
                page = page,
                pageSize = pageSize
            )
        }) {
            is DataResult.Success -> {
                AppConstant.needToFetchTranslations = false
                result.pagingMeta?.let { pagingMeta ->
                    emit(DataResult.Success(pagingMeta.toPaging(result.data.map { it.toDomain() })))
                } ?: emit(DataResult.Failure(DeviceError.MetadataNotFound))
            }

            is DataResult.Failure -> {
                emit(DataResult.Failure(result.appError))
            }
        }
    }

}