package ir.speaking.core.network.api

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import ir.speaking.core.network.di.AiClientManager
import ir.speaking.core.network.model.ChatResponse
import ir.speaking.core.network.model.ChatSuggestResponse
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.core.utils.fromJson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@Single
class AiApiService(
    private val aiClientManager: AiClientManager
) {

    suspend fun request(
        messages: List<ChatMessage>,
        responseFormat: ChatResponseFormat = ChatResponseFormat.JsonObject,
    ): Result<ChatResponse> {
        val activeModels = aiClientManager.getActiveModels()
        val primaryModel = activeModels.first
        val fallbackModel = activeModels.second

        val primaryResult = performRequest(primaryModel, messages, responseFormat)

        if (primaryResult.isSuccess) {
            return primaryResult
        }

        if (!currentCoroutineContext().isActive) {
            return primaryResult
        }

        PrintHelper.info(
            message = "Primary model failed. Switching to Fallback.",
            details = "Error: ${primaryResult.exceptionOrNull()?.message}"
        )

        return performRequest(fallbackModel, messages, responseFormat)
    }

    suspend fun suggest(
        messages: ChatMessage,
        responseFormat: ChatResponseFormat = ChatResponseFormat.JsonObject,
    ): Result<List<String>> {
        val (primaryModel, fallbackModel) = aiClientManager.getActiveModels()

        val primaryResult = performSuggest(primaryModel, messages, responseFormat)

        if (primaryResult.isSuccess || !currentCoroutineContext().isActive) {
            return primaryResult
        }

        PrintHelper.info(
            message = "Primary model failed for Suggestion. Switching to Fallback.",
            details = "Error: ${primaryResult.exceptionOrNull()?.message}"
        )

        return performSuggest(fallbackModel, messages, responseFormat)
    }

    private suspend fun performSuggest(
        modelId: String,
        messages: ChatMessage,
        responseFormat: ChatResponseFormat,
    ): Result<List<String>> {
        try {
            val openAi = aiClientManager.getOpenAiClient()

            val chatCompletionRequest = ChatCompletionRequest(
                responseFormat = responseFormat,
                model = ModelId(modelId),
                messages = listOf(messages)
            )

            val completion = openAi.chatCompletion(chatCompletionRequest)
            val response = completion.choices.lastOrNull()?.message?.content.fromJson<ChatSuggestResponse>()

            return if (response != null) {
                Result.success(response.suggests ?: emptyList())
            } else {
                Result.failure(Throwable(message = "Cannot parse suggestion response of AI"))
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return Result.failure(Throwable(cause = e))
        }
    }

    private suspend fun performRequest(
        modelId: String,
        messages: List<ChatMessage>,
        responseFormat: ChatResponseFormat,
    ): Result<ChatResponse> {
        PrintHelper.info(
            message = "Starting AI request",
            details = "Model: $modelId"
        )

        val openAi = aiClientManager.getOpenAiClient()

        val chatCompletionRequest = ChatCompletionRequest(
            responseFormat = responseFormat,
            model = ModelId(modelId),
            messages = messages
        )

        try {
            val completion = openAi.chatCompletion(
                chatCompletionRequest,
                requestOptions = RequestOptions(
                    timeout = Timeout(
                        request = 25.seconds
                    )
                )
            )

            val response = completion.choices.lastOrNull()?.message?.content.fromJson<ChatResponse>()

            return if (response != null) {
                PrintHelper.success(
                    message = "AI Response received",
                    data = response.message
                )
                Result.success(response)
            } else {
                val errorMessage = "Cannot parse response of AI"
                PrintHelper.error(errorMessage)
                Result.failure(Throwable(message = errorMessage))
            }

        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }

            PrintHelper.error(
                message = "AI request failed for $modelId",
                throwable = e
            )
            return Result.failure(Throwable(message = e.message, cause = e))
        }
    }
}