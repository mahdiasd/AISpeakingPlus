package ir.aispeaking.data.repository.chat

import ir.aispeaking.data.mapper.chat.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStreamResult
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.model.scenario.Role
import ir.aispeaking.domain.repository.chat.ChatRepository
import ir.aispeaking.network.api.chat.ChatApiServiceImpl
import ir.aispeaking.network.dto.chat.ChatRequest
import ir.aispeaking.network.dto.chat.ChatStreamEvent
import ir.aispeaking.network.dto.chat.ChatSuggestRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class ChatRepositoryImpl(private val apiService: ChatApiServiceImpl) : ChatRepository {

    override fun streamMessage(
        scenarioId: String,
        message: String,
        isFirstMessage: Boolean,
        isChallenge: Boolean,
        englishLevel: String,
        starter: Role,
        voiceId: Int,
        generateAudio: Boolean,
    ): Flow<ChatStreamResult> = flow {
        val request = ChatRequest(
            scenarioId = scenarioId,
            message = message,
            isFirstMessage = isFirstMessage,
            isChallenge = isChallenge,
            englishLevel = englishLevel,
            starter = starter.name,
            voiceId = voiceId,
            generateAudio = generateAudio,
        )
        apiService.streamMessage(request).collect { event ->
            when (event) {
                is ChatStreamEvent.Chunk -> {
                    emit(ChatStreamResult.Chunk(event.text))
                }
                is ChatStreamEvent.Done -> {
                    emit(ChatStreamResult.Done(event.chatResponse.toDomain() as Chat.Ai))
                }
                is ChatStreamEvent.Error -> {
                    emit(ChatStreamResult.Error(NetworkError.Unknown(message = event.message)))
                }
            }
        }
    }

    override suspend fun sendMessage(
        scenarioId: String,
        message: String,
        isFirstMessage: Boolean,
        isChallenge: Boolean,
        englishLevel: String,
        starter: Role,
        voiceId: Int,
        generateAudio: Boolean,
    ) = flow {
        when (val result =
            safeCall {
                apiService.sendMessage(
                    ChatRequest(
                        scenarioId = scenarioId,
                        message = message,
                        isFirstMessage = isFirstMessage,
                        isChallenge = isChallenge,
                        englishLevel = englishLevel,
                        starter = starter.name,
                        voiceId = voiceId,
                        generateAudio = generateAudio,
                    )
                )
            }) {

            is DataResult.Success -> {
                emit(DataResult.Success(result.data.toDomain()))
            }

            is DataResult.Failure -> {
                emit(DataResult.Failure(result.appError))
            }
        }
    }

    override suspend fun getSuggestions(
        lastAiMessage: String,
        tasks: List<String>,
        englishLevel: String,
        scenarioDescription: String,
    ): Flow<DataResult<List<String>>> = flow {
        when (val result =
            safeCall {
                apiService.getSuggestions(
                    ChatSuggestRequest(
                        lastAiMessage = lastAiMessage,
                        tasks = tasks,
                        englishLevel = englishLevel,
                        scenarioDescription = scenarioDescription,
                    )
                )
            }) {

            is DataResult.Success -> {
                emit(DataResult.Success(result.data))
            }

            is DataResult.Failure -> {
                emit(DataResult.Failure(result.appError))
            }
        }
    }
}