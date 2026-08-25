package ir.aispeaking.data.repository.challenge

import ir.aispeaking.data.mapper.challenge.toDomain
import ir.aispeaking.data.mapper.user.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.challenge.ChallengeSummary
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.challenge.ChallengeRepository
import ir.aispeaking.domain.repository.user.UserRepository
import ir.aispeaking.network.api.challenge.ChallengeApiService
import ir.aispeaking.utils.constant.AppConstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class ChallengeRepositoryImpl(
    private val apiService: ChallengeApiService,
    private val userRepository: UserRepository
) : ChallengeRepository {

    override suspend fun getChallenge(): Flow<DataResult<ChallengeSummary>> = flow {
        when (val result = safeCall { apiService.getChallenge() }) {
            is DataResult.Success -> emit(DataResult.Success(result.data.toDomain()))
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun createProgress(challengeId: String, score: Int) = flow {
        when (val result = safeCall { apiService.createProgress(challengeId, score) }) {
            is DataResult.Success -> {
                AppConstant.needToFetchUser = true
                emit(DataResult.Success(result.data.toDomain()))
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }
}