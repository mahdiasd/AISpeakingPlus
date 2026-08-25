package ir.aispeaking.domain.usecase.config

import ir.aispeaking.domain.model.config.Config
import ir.aispeaking.domain.model.config.ConfigRequest
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.config.ConfigRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetConfigUseCase(private val repo: ConfigRepository) {
    suspend operator fun invoke(configRequest: ConfigRequest): Flow<DataResult<Config>> {
        return repo.getConfig(configRequest)
    }
}