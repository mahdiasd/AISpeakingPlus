package ir.speaking.core.extension

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import ir.speaking.core.testModule
import kotlinx.serialization.json.Json

fun ApplicationTestBuilder.getClient(dropTables: Boolean = false): HttpClient {
    application {
        testModule(dropTables = dropTables)
    }
    return createClient {
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