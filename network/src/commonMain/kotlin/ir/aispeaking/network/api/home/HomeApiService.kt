package ir.aispeaking.network.api.home

import ir.aispeaking.network.dto.home.HomeResponse
import ir.aispeaking.network.model.NetworkResponse


interface HomeApiService {
    suspend fun getHome(): NetworkResponse<List<HomeResponse>>
}