package ir.aispeaking.data.repository.word

import ir.aispeaking.data.mapper.word.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.word.DailyWord
import ir.aispeaking.domain.model.word.DailyWordProgress
import ir.aispeaking.domain.repository.word.WordRepository
import ir.aispeaking.network.api.word.WordApiService
import ir.aispeaking.utils.constant.AppConstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class WordRepositoryImpl(
    private val apiService: WordApiService,
) : WordRepository {

    override suspend fun getDailyWord(): Flow<DataResult<DailyWord>> = flow {
        when (val result = safeCall { apiService.getDailyWord() }) {
            is DataResult.Success -> emit(DataResult.Success(result.data.toDomain()))

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun createWordProgress(
        wordId: String,
        selectedOptionIndex: Int,
        isCorrect: Boolean,
        wordScore: Int,
    ): Flow<DataResult<DailyWordProgress>> = flow {
        when (val result = safeCall {
            apiService.createWordProgress(
                wordId = wordId,
                selectedOptionIndex = selectedOptionIndex,
                isCorrect = isCorrect,
                wordScore = wordScore
            )
        }) {
            is DataResult.Success -> {
                AppConstant.needToFetchUser = true
                emit(DataResult.Success(result.data.toDomain()))
            }

            is DataResult.Failure -> {
                AppConstant.needToFetchUser = true
                emit(DataResult.Failure(result.appError))
            }
        }
    }
}