package ir.aispeaking.data.repository.health

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.repository.health.HealthRepository
import ir.aispeaking.network.api.health.HealthApi
import org.koin.core.annotation.Single

@Single
class HealthRepositoryImpl(
    private val healthApi: HealthApi
) : HealthRepository {

    override suspend fun checkHealth(): DataResult<Boolean> {
        return try {
            val response = healthApi.getHealth()
            val isUp = response.status.equals("UP", ignoreCase = true)
            DataResult.Success(isUp)
        } catch (e: Throwable) {
            DataResult.Failure(
                NetworkError.InternalServer(httpStatus = 500, message = e.message ?: "Health check failed")
            )
        }
    }
}
