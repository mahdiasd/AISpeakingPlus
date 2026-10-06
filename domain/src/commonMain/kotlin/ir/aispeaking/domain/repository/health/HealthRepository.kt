package ir.aispeaking.domain.repository.health

import ir.aispeaking.domain.model.data_result.DataResult

interface HealthRepository {
    suspend fun checkHealth(): DataResult<Boolean>
}
