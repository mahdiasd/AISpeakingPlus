package ir.speaking.feature.chat.routing

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.network.api.AiApiService
import ir.speaking.core.network.model.ChatResponse
import ir.speaking.core.redis.chat.ChatRedisRepository
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.feature.chat.dto.ChatRequest
import ir.speaking.feature.chat.dto.ChatSuggestRequest
import ir.speaking.feature.chat.model.Chat
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.tts.service.TtsService
import org.koin.ktor.ext.inject

fun Application.chatRouting() {
    val chatRedisRepository by inject<ChatRedisRepository>()
    val aiApiService by inject<AiApiService>()
    val ttsService by inject<TtsService>()

    routing {
        route("/api/v1/chat") {
            authenticate(MyConstant.USER_JWT_NAME) {
                chatAnswer(
                    chatRedisRepository = chatRedisRepository,
                    aiApiService = aiApiService,
                    ttsService = ttsService
                )
                chatSuggest(aiApiService)
            }
        }
    }
}

private fun Route.chatSuggest(aiApiService: AiApiService) {
    post("/suggest") {
        val request = call.receive<ChatSuggestRequest>()

        val remainingTasksText = if (request.tasks.isEmpty()) {
            "None - conversation can be concluded"
        } else {
            request.tasks.joinToString(", ")
        }

        val suggestionsPrompt = MyConstant.SUGGESTION_PROMPT
            .replace("[LAST_AI_MESSAGE]", request.lastAiMessage)
            .replace("[USER_LEVEL]", request.englishLevel)
            .replace("[REMAINING_TASKS]", remainingTasksText)
            .replace("[SCENARIO_DESCRIPTION]", request.scenarioDescription)
            .trimIndent()

        val response = aiApiService.suggest(ChatMessage(content = suggestionsPrompt, role = ChatRole.User))

        response
            .onSuccess { suggestionResponse ->
                call.successRespond(suggestionResponse)
            }.onFailure {
                call.failureRespond(HttpStatusCode.InternalServerError, it.message.toString())
            }
    }
}


private fun Route.chatAnswer(
    chatRedisRepository: ChatRedisRepository,
    aiApiService: AiApiService,
    ttsService: TtsService
) {
    post {
        val request = call.receive<ChatRequest>()
        val userId = call.getUserUid()
        val allChats: MutableList<Chat> = mutableListOf()


        if (request.isFirstMessage) {
            chatRedisRepository.clearChats(userId = userId.toString(), scenarioId = request.scenarioId)
            val systemPrompt = getSystemPrompt(chatRedisRepository, userId.toString(), request)

            allChats.add(
                Chat(
                    userId = userId.toString(),
                    scenarioId = request.scenarioId,
                    message = systemPrompt,
                    role = Role.System
                )
            )


            handleFirstMessage(chatRedisRepository, userId.toString(), request, systemPrompt)
        } else {
            allChats.addAll(
                chatRedisRepository.getChats(userId.toString(), request.scenarioId)
            )
        }

        allChats.add(
            Chat(
                userId = userId.toString(),
                scenarioId = request.scenarioId,
                message = request.message.ifEmpty { "Lets start." },
                role = Role.User
            )
        )

        val response = aiApiService.request(
            messages = allChats.map {
                ChatMessage(
                    role = when (it.role) {
                        Role.System -> ChatRole.System
                        Role.Model -> ChatRole.Assistant
                        Role.User -> ChatRole.User
                    },
                    content = it.message
                )
            }
        )

        response.onSuccess { chatResponse ->
            saveChats(chatRedisRepository, userId.toString(), request, chatResponse)

            val finalResponse = if (request.generateAudio && chatResponse.message.isNotBlank()) {
                val ttsResult = ttsService.synthesize(
                    text = chatResponse.message,
                    sid = request.voiceId ?: 0
                )
                if (ttsResult != null) {
                    chatResponse.copy(
                        audioUrl = ttsResult.audioUrl,
                        voiceId = ttsResult.voiceId,
                        durationMs = ttsResult.durationMs
                    )
                } else {
                    chatResponse
                }
            } else {
                chatResponse
            }

            call.successRespond(finalResponse)
        }.onFailure {
            call.failureRespond(httpStatusCode = HttpStatusCode.InternalServerError, it.message.toString())
        }
    }
}

private suspend fun getSystemPrompt(
    chatRedisRepository: ChatRedisRepository,
    userId: String,
    request: ChatRequest
): String {
    val storedChats = chatRedisRepository.getChats(userId, request.scenarioId)
    return storedChats.firstOrNull { it.role == Role.System }?.message ?: run {
        chatRedisRepository.getSystemPrompt(
            userId = userId,
            scenarioId = request.scenarioId,
            isChallenge = request.isChallenge
        ).replace("[USER_LEVEL]", request.englishLevel)
    }
}

private suspend fun handleFirstMessage(
    chatRedisRepository: ChatRedisRepository,
    userId: String,
    request: ChatRequest,
    systemPrompt: String
) {
    chatRedisRepository.clearChats(userId, request.scenarioId)
    chatRedisRepository.addChat(
        Chat(
            scenarioId = request.scenarioId,
            role = Role.System,
            userId = userId,
            message = systemPrompt
        )
    )
    if (request.isFirstMessage) {
        chatRedisRepository.addChat(
            Chat(
                scenarioId = request.scenarioId,
                role = Role.User,
                userId = userId,
                message = request.message.ifEmpty { "Lets start" }
            )
        )
    }
}

private suspend fun prepareChats(
    chatRedisRepository: ChatRedisRepository,
    userId: String,
    request: ChatRequest,
    systemPrompt: String
): List<Chat> {
    val storedChats = chatRedisRepository.getChats(userId, request.scenarioId).toMutableList()

    if (!request.isFirstMessage) {
        storedChats.add(
            Chat(
                userId = userId,
                scenarioId = request.scenarioId,
                message = request.message,
                role = Role.User
            )
        )
    }
    return storedChats
}

private suspend fun saveChats(
    chatRedisRepository: ChatRedisRepository,
    userId: String,
    request: ChatRequest,
    chatResponse: ChatResponse
) {
    if (!request.isFirstMessage) {
        chatRedisRepository.addChat(
            Chat(
                userId = userId,
                scenarioId = request.scenarioId,
                message = request.message,
                role = Role.User
            )
        )
    }
    chatRedisRepository.addChat(
        Chat(
            userId = userId,
            scenarioId = request.scenarioId,
            message = chatResponse.message,
            role = Role.Model
        )
    )
}