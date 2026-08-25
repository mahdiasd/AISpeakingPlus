package ir.aispeaking.domain.repository.config

import ir.aispeaking.domain.model.config.Config
import ir.aispeaking.domain.model.config.ConfigRequest
import ir.aispeaking.domain.model.data_result.DataResult
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    suspend fun getConfig(
        configRequest: ConfigRequest,
    ): Flow<DataResult<Config>>
}