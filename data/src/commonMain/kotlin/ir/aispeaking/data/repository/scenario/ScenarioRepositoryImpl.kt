package ir.aispeaking.data.repository.scenario

import ir.aispeaking.data.mapper.paginate.toPaging
import ir.aispeaking.data.mapper.scenario.toDomain
import ir.aispeaking.data.mapper.scenario.toShared
import ir.aispeaking.data.mapper.user.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.DeviceError
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.model.scenario.ScenarioDetail
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.domain.repository.scenario.ScenarioRepository
import ir.aispeaking.domain.repository.user.UserRepository
import ir.aispeaking.network.api.scenario.ScenarioApiService
import ir.aispeaking.storage.preferences.scenario.ScenarioPreferences

import ir.aispeaking.utils.constant.AppConstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class ScenarioRepositoryImpl(
    private val apiService: ScenarioApiService,
    private val scenarioPreferences: ScenarioPreferences,
    private val userRepository: UserRepository
) : ScenarioRepository {

    override suspend fun getSharedPrefScenario(): Scenario? {
        return scenarioPreferences.read()?.toDomain()
    }

    override suspend fun saveScenarioToSharedPref(scenario: Scenario) {
        scenarioPreferences.save(scenario.toShared())
    }

    override suspend fun getScenario(id: String): Flow<DataResult<ScenarioDetail>> = flow {
        when (val result = safeCall { apiService.getScenario(id = id) }) {
            is DataResult.Success -> {
                AppConstant.needToRefreshScenarioDetail = false
                emit(DataResult.Success(result.data.toDomain()))
            }
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun getChallengeDetail(challengeId: String): Flow<DataResult<ScenarioDetail>> = flow {
        when (val result = safeCall { apiService.getChallengeDetail(challengeId) }) {
            is DataResult.Success -> {
                AppConstant.needToRefreshScenarioDetail = false
                emit(DataResult.Success(result.data.toDomain()))
            }
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun createScenarioProgress(scenarioId: String, score: Int) = flow {
        when (val result = safeCall { apiService.createScenarioProgress(scenarioId, score) }) {
            is DataResult.Success -> {
                AppConstant.needToFetchUser = true
                emit(DataResult.Success(result.data.toDomain()))
            }
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun getScenarios(
        searchText: String?,
        categoryId: String?,
        page: Int,
        pageSize: Int?
    ): Flow<DataResult<Paging<ScenarioSummary>>> = flow {
        when (val result = safeCall {
            apiService.getScenarios(
                searchText = searchText,
                categoryId = categoryId,
                page = page,
                pageSize = pageSize
            )
        }) {
            is DataResult.Success -> {
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