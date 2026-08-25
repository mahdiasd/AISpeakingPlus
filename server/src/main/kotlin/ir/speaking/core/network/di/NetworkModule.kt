package ir.speaking.core.network.di

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
fun provideKtor(): HttpClient {
    return HttpClient(CIO) {
//        engine {
//            proxy = Proxy(
//                Proxy.Type.HTTP,
//                InetSocketAddress("127.0.0.1", 10808)
//            )
//        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

//        install(Logging) {
//            logger = object : Logger {
//                override fun log(message: String) {
//                    PrintHelper.debug(message)
//                }
//            }
//            level = LogLevel.BODY
//        }

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