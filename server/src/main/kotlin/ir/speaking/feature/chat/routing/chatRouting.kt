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

import io.ktor.server.response.*
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.core.utils.fromJson
import ir.speaking.feature.chat.dto.ChatStreamEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
                chatStream(
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
                val voiceId = request.voiceId ?: 0
                // Synthesize in background so cache is warm, but return URL immediately for zero latency
                CoroutineScope(Dispatchers.IO).launch {
                    ttsService.synthesize(text = chatResponse.message, sid = voiceId)
                }
                chatResponse.copy(
                    audioUrl = "/api/v1/tts/speak?text=${chatResponse.message}&voiceId=$voiceId",
                    voiceId = voiceId
                )
            } else {
                chatResponse
            }

            call.successRespond(finalResponse)
        }.onFailure {
            call.failureRespond(httpStatusCode = HttpStatusCode.InternalServerError, it.message.toString())
        }
    }
}

private fun Route.chatStream(
    chatRedisRepository: ChatRedisRepository,
    aiApiService: AiApiService,
    ttsService: TtsService
) {
    post("/stream") {
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

        call.response.cacheControl(CacheControl.NoCache(null))
        call.respondTextWriter(contentType = ContentType.Text.EventStream) {
            val fullAccumulator = StringBuilder()
            val messageAccumulator = StringBuilder()
            var isInMessage = false
            var messageStarted = false

            try {
                aiApiService.streamRequest(
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
                ).collect { token ->
                    fullAccumulator.append(token)
                    val currentText = fullAccumulator.toString()

                    if (!messageStarted) {
                        val messageKeyIndex = currentText.indexOf("\"message\"")
                        if (messageKeyIndex != -1) {
                            val colonIndex = currentText.indexOf(":", messageKeyIndex)
                            if (colonIndex != -1) {
                                val firstQuoteIndex = currentText.indexOf("\"", colonIndex + 1)
                                if (firstQuoteIndex != -1) {
                                    messageStarted = true
                                    isInMessage = true
                                    val afterFirstQuote = currentText.substring(firstQuoteIndex + 1)
                                    val endQuoteIndex = findUnescapedQuote(afterFirstQuote)
                                    val initialContent = if (endQuoteIndex != -1) {
                                        isInMessage = false
                                        afterFirstQuote.substring(0, endQuoteIndex)
                                    } else {
                                        afterFirstQuote
                                    }
                                    if (initialContent.isNotEmpty()) {
                                        messageAccumulator.append(initialContent)
                                        val unescaped = unescapeJsonString(initialContent)
                                        write("data: ${Json.encodeToString(ChatStreamEvent.Chunk(unescaped))}\n\n")
                                        flush()
                                    }
                                }
                            }
                        }
                    } else if (isInMessage) {
                        val afterKey = currentText.substring(currentText.indexOf("\"message\""))
                        val colon = afterKey.indexOf(":")
                        if (colon != -1) {
                            val firstQuote = afterKey.indexOf("\"", colon + 1)
                            if (firstQuote != -1) {
                                val contentSoFar = afterKey.substring(firstQuote + 1)
                                val endQuote = findUnescapedQuote(contentSoFar)
                                if (endQuote != -1) {
                                    isInMessage = false
                                    val finalMessage = contentSoFar.substring(0, endQuote)
                                    if (finalMessage.length > messageAccumulator.length) {
                                        val newChars = finalMessage.substring(messageAccumulator.length)
                                        messageAccumulator.append(newChars)
                                        val unescaped = unescapeJsonString(newChars)
                                        write("data: ${Json.encodeToString(ChatStreamEvent.Chunk(unescaped))}\n\n")
                                        flush()
                                    }
                                } else {
                                    if (contentSoFar.length > messageAccumulator.length) {
                                        val newChars = contentSoFar.substring(messageAccumulator.length)
                                        messageAccumulator.append(newChars)
                                        val unescaped = unescapeJsonString(newChars)
                                        write("data: ${Json.encodeToString(ChatStreamEvent.Chunk(unescaped))}\n\n")
                                        flush()
                                    }
                                }
                            }
                        }
                    }
                }

                // LLM completed, parse full JSON response
                val fullJson = fullAccumulator.toString()
                val parsedResponse = parseChatResponse(fullJson)

                if (parsedResponse != null) {
                    val voiceId = request.voiceId ?: 0
                    CoroutineScope(Dispatchers.IO).launch {
                        ttsService.synthesize(text = parsedResponse.message, sid = voiceId)
                    }
                    val finalResponse = parsedResponse.copy(
                        audioUrl = "/api/v1/tts/speak?text=${parsedResponse.message}&voiceId=$voiceId",
                        voiceId = voiceId
                    )
                    saveChats(chatRedisRepository, userId.toString(), request, finalResponse)
                    write("data: ${Json.encodeToString(ChatStreamEvent.Done(finalResponse))}\n\n")
                    flush()
                } else {
                    val fallbackResponse = ChatResponse(
                        message = messageAccumulator.toString().ifBlank { "Could not parse AI response" }
                    )
                    saveChats(chatRedisRepository, userId.toString(), request, fallbackResponse)
                    write("data: ${Json.encodeToString(ChatStreamEvent.Done(fallbackResponse))}\n\n")
                    flush()
                }
            } catch (e: Exception) {
                PrintHelper.error("Error in chat streaming: ${e.message}", e)
                write("data: ${Json.encodeToString(ChatStreamEvent.Error(e.message ?: "Streaming error"))}\n\n")
                flush()
            }
        }
    }
}

private fun parseChatResponse(jsonStr: String): ChatResponse? {
    return try {
        jsonStr.fromJson<ChatResponse>() ?: run {
            val start = jsonStr.indexOf('{')
            val end = jsonStr.lastIndexOf('}')
            if (start != -1 && end != -1 && end > start) {
                jsonStr.substring(start, end + 1).fromJson<ChatResponse>()
            } else null
        }
    } catch (_: Exception) {
        null
    }
}

private fun findUnescapedQuote(s: String): Int {
    var i = 0
    while (i < s.length) {
        if (s[i] == '\\') {
            i += 2
        } else if (s[i] == '"') {
            return i
        } else {
            i++
        }
    }
    return -1
}

private fun unescapeJsonString(input: String): String {
    return input
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
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