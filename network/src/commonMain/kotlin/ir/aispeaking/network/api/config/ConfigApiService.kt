package ir.aispeaking.network.api.config

import ir.aispeaking.network.dto.config.ConfigRequest
import ir.aispeaking.network.dto.config.ConfigResponse
import ir.aispeaking.network.model.NetworkResponse

interface ConfigApiService {
    suspend fun getConfig(
        configRequest: ConfigRequest
    ): NetworkResponse<ConfigResponse>
}

