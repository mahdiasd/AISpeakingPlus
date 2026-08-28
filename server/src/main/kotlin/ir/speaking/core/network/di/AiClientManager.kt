package ir.speaking.core.network.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import io.ktor.client.engine.cio.endpoint
import ir.speaking.core.network.model.AiPlatformConfig
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient
import kotlin.time.Duration.Companion.seconds

@Single
class AiClientManager(
    private val redissonClient: RedissonClient
) {
    private val bucketKey = "ai_platform_config"

    private var currentClient: OpenAI? = null
    private var lastConfigHash: Int? = null

    private val fallbackConfig = AiPlatformConfig(
        baseUrl = System.getenv("AI_BASE_URL") ?: "http://host.docker.internal:20128/v1/",
        apiKey = System.getenv("AI_API_KEY") ?: "sk-3c185114b476049c-z4geo6-d954b610",
        primaryModel = System.getenv("AI_PRIMARY_MODEL") ?: "MyCombo",
        fallbackModel = System.getenv("AI_FALLBACK_MODEL") ?: "MyCombo"
    )

    suspend fun getOpenAiClient(): OpenAI {
        val config = getActiveConfig()
        val currentHash = config.hashCode()

        if (currentClient == null || lastConfigHash != currentHash) {
            currentClient?.close()
            currentClient = createClient(config)
            lastConfigHash = currentHash
        }

        return currentClient!!
    }

    suspend fun getActiveModels(): Pair<String, String> {
        val config = getActiveConfig()
        return Pair(config.primaryModel, config.fallbackModel)
    }

    private fun getActiveConfig(): AiPlatformConfig {
        return try {
            val bucket = redissonClient.getBucket<AiPlatformConfig>(bucketKey)
            bucket.get() ?: fallbackConfig
        } catch (e: Exception) {
            fallbackConfig
        }
    }

    private fun createClient(config: AiPlatformConfig): OpenAI {
        return OpenAI(
            token = "",
            host = OpenAIHost(baseUrl = config.baseUrl),
            headers = mapOf("Authorization" to "Bearer ${config.apiKey}"),
            logging = LoggingConfig(LogLevel.All, logger = Logger.Simple),
            timeout = Timeout(socket = 60.seconds),
            httpClientConfig = {
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 60_000
                    socketTimeoutMillis = 60_000
                    connectTimeoutMillis = 15_000
                }

                engine {
                    if (this is io.ktor.client.engine.cio.CIOEngineConfig) {
                        endpoint {
                            keepAliveTime = 5000
                            connectTimeout = 5000
                            maxConnectionsPerRoute = 100
                            pipelineMaxSize = 20
                        }
                    }
                }
            }
        )
    }
}
