package ir.aispeaking.network.api.home

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import ir.aispeaking.network.dto.home.HomeResponse
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.platformBaseUrl
import org.koin.core.annotation.Single

@Single
class HomeApiServiceImpl(private val httpClient: HttpClient) : HomeApiService {

    override suspend fun getHome(): NetworkResponse<List<HomeResponse>> = httpClient.get {
        url("${platformBaseUrl()}api/v1/home")
    }.body<NetworkResponse<List<HomeResponse>>>()

}