package ir.speaking.feature.stage.service

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import ir.speaking.core.network.api.AiApiService
import ir.speaking.feature.stage.dto.StageChatMessageDto
import ir.speaking.feature.stage.dto.StageChatRequest
import ir.speaking.feature.stage.dto.StageChatResponse
import ir.speaking.feature.stage.repository.StageRepository
import ir.speaking.feature.tts.model.KokoroVoices
import ir.speaking.feature.tts.service.TtsService
import ir.speaking.feature.stage.dto.StageDetailResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import java.util.UUID

@Single
class StageChatService(
    private val stageRepository: StageRepository,
    private val ttsService: TtsService,
    private val aiApiService: AiApiService
) {
    private val logger = LoggerFactory.getLogger(StageChatService::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun processChatTurn(
        stageId: String,
        userId: UUID?,
        request: StageChatRequest
    ): StageChatResponse {
        val stage = stageRepository.getStageDetail(stageId, userId)
        val userMessage = request.message?.trim().orEmpty()
        val history = request.history

        val voiceSid = KokoroVoices.ALL.firstOrNull { it.code.equals(stage?.voiceId, ignoreCase = true) }?.id ?: 0

        // If userMessage is empty, this is the AI starter greeting turn
        if (userMessage.isEmpty()) {
            val (initialGreeting, initialTranslation) = generateAiStarterTurn(stage)
            val audioUrl = trySynthesize(initialGreeting, voiceSid)

            return StageChatResponse(
                message = initialGreeting,
                translatedMessage = initialTranslation,
                audioUrl = audioUrl,
                grammarFeedbackFa = "",
                objectiveCompleted = false,
                finishTaskIndexes = emptyList()
            )
        }

        // Generate response and evaluate grammar via AI (with resilient local fallback)
        val turnResult = generateAiChatTurn(
            stage = stage,
            userMessage = userMessage,
            history = history
        )

        val audioUrl = trySynthesize(turnResult.reply, voiceSid)

        return StageChatResponse(
            message = turnResult.reply,
            translatedMessage = turnResult.translation,
            audioUrl = audioUrl,
            grammarFeedbackFa = turnResult.grammarFeedback,
            objectiveCompleted = turnResult.isObjectiveDone,
            finishTaskIndexes = if (turnResult.isObjectiveDone) listOf(0, 1) else emptyList()
        )
    }

    private suspend fun trySynthesize(text: String, voiceSid: Int): String? {
        return try {
            val result = ttsService.synthesize(text = text, sid = voiceSid, speed = 1.0f)
            result?.audioUrl
        } catch (e: Exception) {
            logger.warn("TTS synthesis error: ${e.message}")
            null
        }
    }

    private suspend fun generateAiStarterTurn(stage: StageDetailResponse?): Pair<String, String?> {
        return try {
            val systemPrompt = buildString {
                appendLine("You are an interactive conversational English learning assistant acting as a character in an English speaking simulation scenario.")
                appendLine("Current Stage: ${stage?.title ?: "English Speaking Stage"}")
                appendLine("Character Name: ${stage?.characterName ?: "Character"}")
                appendLine("Character Persona & Behavior: ${stage?.characterBehavior ?: "Friendly and helpful conversational partner"}")
                appendLine("Stage Target Objective: ${stage?.targetObjective ?: "Practice natural English conversation"}")
                appendLine()
                appendLine("Task:")
                appendLine("Generate the OPENING greeting message from '${stage?.characterName ?: "Character"}' to start the conversation with the user in this scenario.")
                appendLine("Keep it natural, in-character, and concise (1 to 2 sentences max).")
                appendLine("Also provide a natural Persian translation.")
                appendLine("ALWAYS return valid JSON matching this schema:")
                appendLine("{\n  \"reply\": \"Opening greeting in English\",\n  \"translation\": \"ترجمه فارسی پیام آغازین\"\n}")
            }

            val chatMessages = listOf(ChatMessage(role = ChatRole.System, content = systemPrompt))
            val rawResult = aiApiService.requestRaw(chatMessages)
            if (rawResult.isSuccess) {
                val rawText = rawResult.getOrNull().orEmpty().trim()
                val cleanJson = cleanJsonResponse(rawText)
                val jsonObject = json.parseToJsonElement(cleanJson).jsonObject
                val reply = jsonObject["reply"]?.jsonPrimitive?.content?.trim().orEmpty()
                val translation = jsonObject["translation"]?.jsonPrimitive?.content?.trim().orEmpty()
                if (reply.isNotEmpty()) {
                    return Pair(reply, translation.ifEmpty { null })
                }
            } else {
                logger.warn("AI starter request failed: ${rawResult.exceptionOrNull()?.message}")
            }
            getInitialGreeting(stage?.orderIndex, stage?.characterName)
        } catch (e: Exception) {
            logger.warn("Error in generateAiStarterTurn: ${e.message}, using fallback starter")
            getInitialGreeting(stage?.orderIndex, stage?.characterName)
        }
    }

    private fun getInitialGreeting(orderIndex: Int?, characterName: String?): Pair<String, String> {
        return when (orderIndex) {
            1 -> Pair(
                "Good evening! Welcome aboard. Would you like the grilled chicken with rice, or the vegetarian pasta tonight?",
                "عصر بخیر! به پرواز خوش آمدید. امشب مرغ گریل شده با برنج میل دارید یا پاستای گیاهی؟"
            )
            2 -> Pair(
                "Good day. What is the purpose of your visit to the United Kingdom?",
                "روز بخیر. هدف شما از سفر به بریتانیا چیست؟"
            )
            3 -> Pair(
                "Hello! How can I help you today? Did your luggage not arrive on the carousel?",
                "سلام! چطور می‌توانم کمکتان کنم؟ آیا چمدانتان روی نوار نقاله نیامده است؟"
            )
            4 -> Pair(
                "Hello there! Welcome to the London Underground. Do you need an Oyster card or a standard travelcard?",
                "سلام! به متروی لندن خوش آمدید. کارت اویستر می‌خواهید یا بلیت استاندارد سفر؟"
            )
            else -> Pair(
                "Hello! Welcome. How can I help you today?",
                "سلام! خوش آمدید. چطور می‌توانم امروز کمکتان کنم؟"
            )
        }
    }

    private fun evaluateGrammar(userText: String): String {
        val lower = userText.lowercase()

        // Common ESL grammar issues check
        if (lower.contains("i wants")) {
            return "اشکال در فاعل و فعل: برای ضمیر «I» از فعل ساده بدون s استفاده کنید: I want"
        }
        if (lower.contains("i flying") || lower.contains("i going") || lower.contains("i travelling") || lower.contains("i studying")) {
            return "اشکال در زمان استمراری: بعد از «I» باید فعل کمکی «am» قرار گیرد: I am flying / I am going"
        }
        if (lower.contains("he want ") || lower.contains("she want ")) {
            return "اشکال در سوم‌شخص: برای «he / she» فعل باید با s بیاید: He wants / She wants"
        }
        if (lower.contains("they is") || lower.contains("we is")) {
            return "اشکال در تطابق فاعل و فعل: برای فاعل جمع از «are» استفاده کنید: They are / We are"
        }
        if (lower.contains("i would to") || lower.contains("would like to order of")) {
            return "اشکال ساختار: بعد از «would like» شکل ساده فعل می‌آید: I would like to order"
        }
        if (lower.contains("give me food") || lower.contains("give me chicken")) {
            return "نکته کاربردی: در زبان انگلیسی برای سفارش غذا بهتر است از عبارات مودبانه مثل «I would like...» یا «Could I please have...» استفاده کنید."
        }

        // Return empty string if grammar is fine (this enables the green "گرامر درسته" badge)
        return ""
    }

    private suspend fun generateNextTurn(
        stageOrderIndex: Int,
        characterName: String,
        characterBehavior: String,
        userMessage: String,
        history: List<StageChatMessageDto>
    ): Triple<String, String, Boolean> {
        val lower = userMessage.lowercase()
        val userTurnCount = history.count { it.role.equals("User", ignoreCase = true) } + 1

        // Stage 1: In-Flight to London
        if (stageOrderIndex == 1) {
            return when {
                userTurnCount == 1 || lower.contains("chicken") || lower.contains("pasta") || lower.contains("vegetarian") || lower.contains("rice") -> {
                    Triple(
                        "Certainly! And what can I get you to drink with that? We have orange juice, water, apple juice, or hot tea.",
                        "حتماً! و مایلید همراه آن چه نوشیدنی‌ای برایتان بیاورم؟ آب پرتقال، آب، آب سیب یا چای داغ داریم.",
                        false
                    )
                }
                userTurnCount == 2 || lower.contains("juice") || lower.contains("water") || lower.contains("tea") || lower.contains("coke") -> {
                    Triple(
                        "Here you are, enjoy your meal! It's quite a long journey tonight—are you traveling to London for a holiday or for university studies?",
                        "بفرمایید، نوش جان! امشب سفر طولانی‌ای در پیش است؛ آیا برای تعطیلات به لندن می‌روید یا برای تحصیلات دانشگاهی؟",
                        false
                    )
                }
                else -> {
                    Triple(
                        "That's wonderful! London is an amazing city for students. Best of luck with your studies, enjoy your meal, and let me know if you need anything else!",
                        "فوق‌العاده است! لندن شهری بی‌نظیر برای دانشجویان است. با آرزوی موفقیت در تحصیلات، از غذایتان لذت ببرید و اگر چیز دیگری لازم داشتید به من بفرمایید!",
                        true
                    )
                }
            }
        }

        // Stage 2: Heathrow Border Control
        if (stageOrderIndex == 2) {
            return when {
                userTurnCount == 1 || lower.contains("study") || lower.contains("student") || lower.contains("university") -> {
                    Triple(
                        "Which university will you be attending, and what course are you studying?",
                        "در کدام دانشگاه تحصیل خواهید کرد و رشته تحصیلی‌تان چیست؟",
                        false
                    )
                }
                userTurnCount == 2 || lower.contains("london") || lower.contains("college") || lower.contains("engineering") || lower.contains("science") -> {
                    Triple(
                        "Where will you be staying while studying in London, and how long is your course?",
                        "در طول تحصیل در لندن کجا اقامت خواهید داشت و دوره تحصیلی شما چقدر طول می‌کشد؟",
                        false
                    )
                }
                else -> {
                    Triple(
                        "Everything is in order. Welcome to the United Kingdom, and all the best with your academic studies.",
                        "همه مدارک کامل و مرتب است. به پادشاهی متحده خوش آمدید و با آرزوی موفقیت در تحصیلات دانشگاهی‌تان.",
                        true
                    )
                }
            }
        }

        // Generic fallback turn
        return Triple(
            "Thank you for sharing that. Is there anything else I can help you with regarding this?",
            "ممنون از توضیحتان. آیا مورد دیگری هست که در این زمینه بتوانم کمکتان کنم؟",
            userTurnCount >= 3
        )
    }

    private suspend fun generateAiChatTurn(
        stage: StageDetailResponse?,
        userMessage: String,
        history: List<StageChatMessageDto>
    ): ChatTurnResult {
        return try {
            val systemPrompt = buildString {
                appendLine("You are an interactive conversational English learning assistant acting as a character in an English speaking simulation scenario.")
                appendLine("Current Stage: ${stage?.title ?: "English Speaking Stage"}")
                appendLine("Character Name: ${stage?.characterName ?: "Character"}")
                appendLine("Character Persona & Behavior: ${stage?.characterBehavior ?: "Friendly and helpful conversational partner"}")
                appendLine("Stage Target Objective: ${stage?.targetObjective ?: "Practice natural English conversation"}")
                appendLine()
                appendLine("Instructions:")
                appendLine("1. Reply in-character as '${stage?.characterName ?: "Character"}' in spoken-style English. Keep your character reply concise (1 to 2 sentences) and conversational.")
                appendLine("2. Provide a fluent, natural Persian translation ('translation') for your English reply.")
                appendLine("3. Critically examine the user's latest message for English grammar, vocabulary, preposition, or phrasing errors:")
                appendLine("   - Pay close attention to word boundaries: never report words as glued or concatenated if standard spaces or punctuation separate them. Only report genuine spelling, grammar, preposition, or phrasing errors.")
                appendLine("   - If the user made ANY mistake: provide a helpful, polite explanation in Persian ('grammar_feedback') explaining the issue and giving the correct sentence.")
                appendLine("   - If the user's sentence is grammatically correct and natural: 'grammar_feedback' MUST be an empty string \"\".")
                appendLine("4. Check if the user has completed or progressed towards the stage objective ('${stage?.targetObjective ?: ""}'). Set 'objective_completed' to true if achieved or at final step, else false.")
                appendLine("5. ALWAYS return valid JSON matching this schema:")
                appendLine("{\n  \"reply\": \"English reply here\",\n  \"translation\": \"ترجمه فارسی پاسخ\",\n  \"grammar_feedback\": \"توضیح فارسی اشکال گرامری یا رشته خالی در صورت صحت\",\n  \"objective_completed\": false\n}")
            }

            val chatMessages = mutableListOf<ChatMessage>()
            chatMessages.add(ChatMessage(role = ChatRole.System, content = systemPrompt))

            // Add recent history for context, ensuring we don't repeat the current user message
            val sanitizedHistory = history.filterIndexed { index, item ->
                !(index == history.lastIndex && item.role.equals("User", ignoreCase = true) && item.content.trim().equals(userMessage.trim(), ignoreCase = true))
            }.takeLast(6)

            for (item in sanitizedHistory) {
                val role = if (item.role.equals("User", ignoreCase = true)) ChatRole.User else ChatRole.Assistant
                chatMessages.add(ChatMessage(role = role, content = item.content))
            }

            chatMessages.add(ChatMessage(role = ChatRole.User, content = userMessage))

            val rawResult = aiApiService.requestRaw(chatMessages)
            if (rawResult.isSuccess) {
                val rawText = rawResult.getOrNull().orEmpty().trim()
                val cleanJson = cleanJsonResponse(rawText)
                val jsonObject = json.parseToJsonElement(cleanJson).jsonObject

                val reply = jsonObject["reply"]?.jsonPrimitive?.content?.trim().orEmpty()
                val translation = jsonObject["translation"]?.jsonPrimitive?.content?.trim().orEmpty()
                val grammarFeedback = jsonObject["grammar_feedback"]?.jsonPrimitive?.content?.trim().orEmpty()
                val objectiveCompleted = jsonObject["objective_completed"]?.jsonPrimitive?.booleanOrNull ?: false

                if (reply.isNotEmpty()) {
                    return ChatTurnResult(
                        reply = reply,
                        translation = translation.ifEmpty { null },
                        isObjectiveDone = objectiveCompleted,
                        grammarFeedback = grammarFeedback
                    )
                }
            } else {
                logger.warn("AI API request failed: ${rawResult.exceptionOrNull()?.message}")
            }
            fallbackChatTurn(stage, userMessage, history)
        } catch (e: Exception) {
            logger.warn("Error in generateAiChatTurn: ${e.message}, falling back to rule-based engine")
            fallbackChatTurn(stage, userMessage, history)
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        return text.trim()
    }

    private suspend fun fallbackChatTurn(
        stage: StageDetailResponse?,
        userMessage: String,
        history: List<StageChatMessageDto>
    ): ChatTurnResult {
        val grammar = evaluateGrammar(userMessage)
        val (reply, translation, isObjectiveDone) = generateNextTurn(
            stageOrderIndex = stage?.orderIndex ?: 1,
            characterName = stage?.characterName ?: "Character",
            characterBehavior = stage?.characterBehavior ?: "",
            userMessage = userMessage,
            history = history
        )
        return ChatTurnResult(
            reply = reply,
            translation = translation,
            isObjectiveDone = isObjectiveDone,
            grammarFeedback = grammar
        )
    }
}

data class ChatTurnResult(
    val reply: String,
    val translation: String?,
    val isObjectiveDone: Boolean,
    val grammarFeedback: String
)
