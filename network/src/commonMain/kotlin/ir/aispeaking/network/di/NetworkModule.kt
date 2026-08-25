package ir.aispeaking.network.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.storage.preferences.token.TokenPreferences
import ir.aispeaking.utils.dLog
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

private const val TIME_OUT = 6000L

@Single
fun provideKtor(tokenPreferences: TokenPreferences): HttpClient {
    return HttpClient {

        install(HttpTimeout) {
            requestTimeoutMillis = TIME_OUT
            connectTimeoutMillis = TIME_OUT
            socketTimeoutMillis = TIME_OUT
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)

            tokenPreferences.read().takeIf { it.isNotEmpty() }?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (BuildConfig.DEBUG) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        message.dLog(tag = "ktor")
                    }
                }
                level = LogLevel.ALL
            }
        }

        install(WebSockets) {
            pingIntervalMillis = 20_000
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
    }
}