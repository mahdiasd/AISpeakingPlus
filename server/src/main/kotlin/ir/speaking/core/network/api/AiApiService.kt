package ir.speaking.core.network.api

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import ir.speaking.core.network.di.AiClientManager
import ir.speaking.core.network.utils.PrintHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@Single
class AiApiService(
    private val aiClientManager: AiClientManager
) {

    fun streamRequest(
        messages: List<ChatMessage>,
        responseFormat: ChatResponseFormat = ChatResponseFormat.JsonObject,
    ): Flow<String> = flow {
        val (primaryModel, fallbackModel) = aiClientManager.getActiveModels()
        try {
            val openAi = aiClientManager.getOpenAiClient()
            val chatCompletionRequest = ChatCompletionRequest(
                responseFormat = responseFormat,
                model = ModelId(primaryModel),
                messages = messages
            )
            openAi.chatCompletions(
                chatCompletionRequest,
                requestOptions = RequestOptions(
                    timeout = Timeout(request = 35.seconds)
                )
            ).collect { chunk ->
                val content = chunk.choices.firstOrNull()?.delta?.content
                if (!content.isNullOrEmpty()) {
                    emit(content)
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            PrintHelper.warning("Primary model streaming failed: ${e.message}. Trying fallback $fallbackModel")
            val openAi = aiClientManager.getOpenAiClient()
            val chatCompletionRequest = ChatCompletionRequest(
                responseFormat = responseFormat,
                model = ModelId(fallbackModel),
                messages = messages
            )
            openAi.chatCompletions(
                chatCompletionRequest,
                requestOptions = RequestOptions(
                    timeout = Timeout(request = 35.seconds)
                )
            ).collect { chunk ->
                val content = chunk.choices.firstOrNull()?.delta?.content
                if (!content.isNullOrEmpty()) {
                    emit(content)
                }
            }
        }
    }

    suspend fun requestRaw(
        messages: List<ChatMessage>,
        responseFormat: ChatResponseFormat = ChatResponseFormat.JsonObject,
    ): Result<String> {
        val (primaryModel, fallbackModel) = aiClientManager.getActiveModels()
        return try {
            val openAi = aiClientManager.getOpenAiClient()
            val response = openAi.chatCompletion(
                ChatCompletionRequest(
                    model = ModelId(primaryModel),
                    messages = messages,
                    responseFormat = responseFormat
                )
            ).choices.firstOrNull()?.message?.content ?: ""
            Result.success(response)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            PrintHelper.warning("Primary model failed: ${e.message}. Trying fallback $fallbackModel")
            try {
                val openAi = aiClientManager.getOpenAiClient()
                val response = openAi.chatCompletion(
                    ChatCompletionRequest(
                        model = ModelId(fallbackModel),
                        messages = messages,
                        responseFormat = responseFormat
                    )
                ).choices.firstOrNull()?.message?.content ?: ""
                Result.success(response)
            } catch (fallbackEx: Exception) {
                if (fallbackEx is CancellationException) throw fallbackEx
                Result.failure(fallbackEx)
            }
        }
    }
}