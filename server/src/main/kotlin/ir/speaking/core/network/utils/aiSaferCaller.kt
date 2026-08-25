package ir.speaking.core.network.utils

import ir.speaking.core.network.model.ChatResponse
import ir.speaking.core.network.model.ChatSuggestResponse
import ir.speaking.core.network.model.GeminiResponse
import ir.speaking.core.utils.fromJson

suspend fun aiSaferCaller(execute: suspend () -> GeminiResponse?): Result<ChatResponse> {
    return try {
        val response = execute.invoke()
        response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.let { text ->
            text.fromJson<ChatResponse>()?.let { chatResponse ->
                return Result.success(chatResponse)
            }
        } ?: run {
            return Result.failure(Exception("Response was null!"))
        }
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

suspend fun aiSuggesterSaferCaller(execute: suspend () -> GeminiResponse?): Result<List<String>> {
    return try {
        val response = execute.invoke()
        response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.let { text ->
            text.fromJson<ChatSuggestResponse>()?.let { chatResponse ->
                return Result.success(chatResponse.suggests ?: emptyList())
            }
        } ?: run {
            return Result.failure(Exception("Response was null!"))
        }
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
