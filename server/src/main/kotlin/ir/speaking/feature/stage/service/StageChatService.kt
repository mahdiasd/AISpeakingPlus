package ir.speaking.feature.stage.service

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.network.api.AiApiService
import ir.speaking.feature.stage.dto.HintMessageRequest
import ir.speaking.feature.stage.dto.HintResponse
import ir.speaking.feature.stage.dto.StageChatMessageDto
import ir.speaking.feature.stage.dto.StageChatRequest
import ir.speaking.feature.stage.dto.StageChatResponse
import ir.speaking.feature.stage.repository.StageRepository
import ir.speaking.feature.tts.model.KokoroVoices
import ir.speaking.feature.tts.service.TtsService
import ir.speaking.feature.stage.dto.StageDetailResponse
import ir.speaking.feature.user.repository.UserRepo
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
    private val aiApiService: AiApiService,
    private val userRepo: UserRepo
) {
    constructor(
        stageRepository: StageRepository,
        ttsService: TtsService,
        aiApiService: AiApiService
    ) : this(stageRepository, ttsService, aiApiService, UserRepo())

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

        val resolvedUserLevel = request.userLevel?.trim()?.takeIf { it.isNotBlank() }
            ?: (if (userId != null) userRepo.getUserLanguageLevel(userId) else null)
            ?: "A1"
        val normalizedLevel = resolvedUserLevel.uppercase()

        val voiceSid = resolveVoiceSid(stage)

        // If userMessage is empty, this is the AI starter greeting turn
        if (userMessage.isEmpty()) {
            val (initialGreeting, initialTranslation) = generateAiStarterTurn(stage, normalizedLevel)
            val audioUrl = trySynthesize(initialGreeting, voiceSid)

            return StageChatResponse(
                message = initialGreeting,
                translatedMessage = initialTranslation,
                audioUrl = audioUrl,
                hasGrammarError = false,
                correctedSentence = null,
                grammarFeedbackFa = "",
                objectiveCompleted = false
            )
        }

        // Generate response and evaluate grammar via AI
        val turnResult = generateAiChatTurn(
            stage = stage,
            userMessage = userMessage,
            history = history,
            level = normalizedLevel
        )

        val audioUrl = trySynthesize(turnResult.reply, voiceSid)

        return StageChatResponse(
            message = turnResult.reply,
            translatedMessage = turnResult.translation,
            audioUrl = audioUrl,
            hasGrammarError = turnResult.hasGrammarError,
            correctedSentence = turnResult.correctedSentence,
            grammarFeedbackFa = turnResult.grammarFeedback,
            objectiveCompleted = turnResult.isObjectiveDone
        )
    }

    suspend fun processChatTurnStream(
        stageId: String,
        userId: UUID?,
        request: StageChatRequest,
        onChunk: suspend (String) -> Unit
    ): StageChatResponse {
        val stage = stageRepository.getStageDetail(stageId, userId)
        val userMessage = request.message?.trim().orEmpty()
        val history = request.history

        val resolvedUserLevel = request.userLevel?.trim()?.takeIf { it.isNotBlank() }
            ?: (if (userId != null) userRepo.getUserLanguageLevel(userId) else null)
            ?: "A1"
        val normalizedLevel = resolvedUserLevel.uppercase()

        val voiceSid = resolveVoiceSid(stage)

        // If userMessage is empty, this is the AI starter greeting turn
        if (userMessage.isEmpty()) {
            val (initialGreeting, initialTranslation) = generateAiStarterTurnStream(stage, normalizedLevel, onChunk)
            val audioUrl = trySynthesize(initialGreeting, voiceSid)

            return StageChatResponse(
                message = initialGreeting,
                translatedMessage = initialTranslation,
                audioUrl = audioUrl,
                hasGrammarError = false,
                correctedSentence = null,
                grammarFeedbackFa = "",
                objectiveCompleted = false
            )
        }

        // Generate response and evaluate grammar via AI stream
        val turnResult = generateAiChatTurnStream(
            stage = stage,
            userMessage = userMessage,
            history = history,
            level = normalizedLevel,
            onChunk = onChunk
        )

        val audioUrl = trySynthesize(turnResult.reply, voiceSid)

        return StageChatResponse(
            message = turnResult.reply,
            translatedMessage = turnResult.translation,
            audioUrl = audioUrl,
            hasGrammarError = turnResult.hasGrammarError,
            correctedSentence = turnResult.correctedSentence,
            grammarFeedbackFa = turnResult.grammarFeedback,
            objectiveCompleted = turnResult.isObjectiveDone
        )
    }

    private fun resolveVoiceSid(stage: StageDetailResponse?): Int {
        val voiceByCode = KokoroVoices.findByCode(stage?.voiceId, isV1 = ttsService.isV1Model)?.id
        if (voiceByCode != null) return voiceByCode
        val numericId = stage?.voiceId?.toIntOrNull()
        if (numericId != null) return numericId
        return if (ttsService.isV1Model) {
            when (stage?.characterGender?.lowercase()) {
                "man", "male" -> 26 // bm_george
                else -> 21 // bf_emma
            }
        } else {
            when (stage?.characterGender?.lowercase()) {
                "man", "male" -> 9 // bm_george
                else -> 7 // bf_emma
            }
        }
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

    private fun buildCefrDirectives(level: String): String = buildString {
        appendLine("Learner English Level (CEFR): $level")
        appendLine("CRITICAL LEVEL ADAPTATION MANDATES:")
        when (level) {
            "A1" -> {
                appendLine("- LEVEL A1 (Beginner):")
                appendLine("  * Vocabulary: Strictly use ONLY high-frequency, basic A1 everyday words (e.g., want, like, have, go, eat, drink, please, thank you, hello).")
                appendLine("  * Sentence Length: Keep sentences very short (3 to 6 words each). Maximum 1-2 short sentences.")
                appendLine("  * Grammar: Use simple present tense or basic imperative/questions ('Do you want...', 'Here is...').")
                appendLine("  * Prohibited: NEVER use idioms, phrasal verbs, passive voice, formal vocabulary, or complex multi-clause sentences.")
                appendLine("  * Feedback: When correcting, focus only on fundamental beginner errors and explain gently and encouragingly in simple Persian.")
            }
            "A2" -> {
                appendLine("- LEVEL A2 (Elementary):")
                appendLine("  * Vocabulary: Use common everyday words and routine phrases.")
                appendLine("  * Sentence Length: Short sentences, connected with simple conjunctions (and, but, because).")
                appendLine("  * Grammar: Simple past, future with 'going to', basic modals.")
                appendLine("  * Prohibited: Avoid rare idioms or complex sentence structures.")
            }
            "B1" -> {
                appendLine("- LEVEL B1 (Intermediate):")
                appendLine("  * Vocabulary: Standard everyday conversational English.")
                appendLine("  * Flow: Connected sentences, common daily idioms, natural conversational pace.")
            }
            "B2" -> {
                appendLine("- LEVEL B2 (Upper-Intermediate):")
                appendLine("  * Vocabulary: Rich, expressive vocabulary and natural idioms.")
                appendLine("  * Flow: Fluent, authentic conversational flow with varied structures.")
            }
            "C1", "C2" -> {
                appendLine("- LEVEL $level (Advanced / Mastery):")
                appendLine("  * Sophisticated, native-like conversational English with nuance, idioms, and natural eloquence.")
            }
            else -> {
                appendLine("- Use simple and accessible English suitable for beginners.")
            }
        }
    }

    private suspend fun generateAiStarterTurn(stage: StageDetailResponse?, level: String = "A1"): Pair<String, String?> {
        return try {
            val systemPrompt = buildString {
                appendLine("You are an interactive conversational English learning assistant acting as a character in an English speaking simulation scenario.")
                appendLine("Current Stage: ${stage?.title ?: "English Speaking Stage"}")
                appendLine("Character Name: ${stage?.characterName ?: "Character"}")
                appendLine("Character Persona & Behavior: ${stage?.characterBehavior ?: "Friendly and helpful conversational partner"}")
                appendLine("Stage Target Objective: ${stage?.targetObjective ?: "Practice natural English conversation"}")
                appendLine()
                append(buildCefrDirectives(level))
                appendLine()
                appendLine("Task:")
                appendLine("Generate the OPENING greeting message from '${stage?.characterName ?: "Character"}' to start the conversation with the user in this scenario.")
                appendLine("The greeting MUST strictly match the learner's English level ($level) in vocabulary and sentence structure.")
                appendLine("Keep it natural, in-character, and concise (1 to 2 sentences max).")
                appendLine("IMPORTANT: Do NOT repeat fixed canned lines or memorized phrases. Dynamically formulate an original, natural opening that fits the character's persona, tone, and scenario context.")
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
            }
            val errorMsg = rawResult.exceptionOrNull()?.message ?: "AI starter request returned empty reply"
            logger.error("AI starter request failed: $errorMsg")
            throw AppException.ExternalServiceError("خطا در برقراری ارتباط با هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        } catch (e: Exception) {
            if (e is AppException) throw e
            logger.error("Error in generateAiStarterTurn: ${e.message}", e)
            throw AppException.ExternalServiceError("خطا در برقراری ارتباط با هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        }
    }


    private suspend fun generateAiChatTurn(
        stage: StageDetailResponse?,
        userMessage: String,
        history: List<StageChatMessageDto>,
        level: String = "A1"
    ): ChatTurnResult {
        return try {
            val systemPrompt = buildString {
                appendLine("You are an interactive conversational English learning assistant acting as a character in an English speaking simulation scenario.")
                appendLine("Current Stage: ${stage?.title ?: "English Speaking Stage"}")
                appendLine("Character Name: ${stage?.characterName ?: "Character"}")
                appendLine("Character Persona & Behavior: ${stage?.characterBehavior ?: "Friendly and helpful conversational partner"}")
                appendLine("Stage Target Objective: ${stage?.targetObjective ?: "Practice natural English conversation"}")
                appendLine()
                append(buildCefrDirectives(level))
                appendLine()
                appendLine("Instructions:")
                appendLine("1. Reply in-character as '${stage?.characterName ?: "Character"}' in spoken-style English strictly tailored to CEFR level $level. Keep your character reply concise (1 to 2 sentences) and conversational. Do NOT recite fixed scripts or memorized phrases; dynamically craft fresh, natural responses following the character's persona and conversational goals.")
                appendLine("2. Provide a fluent, natural Persian translation ('translation') for your English reply.")
                appendLine("3. Critically examine the user's latest message for English grammar, vocabulary, preposition, or phrasing errors suited to level $level:")
                appendLine("   - Pay close attention to word boundaries: never report words as glued or concatenated if standard spaces or punctuation separate them. Only report genuine spelling, grammar, preposition, or phrasing errors.")
                appendLine("   - If the user made ANY mistake: set 'has_grammar_error': true, provide the corrected English sentence in 'corrected_sentence' (matched to level $level), and provide a helpful, polite explanation in Persian ('grammar_feedback') explaining the issue simply and constructively.")
                appendLine("   - If the user's sentence is grammatically correct and natural: set 'has_grammar_error': false, 'corrected_sentence': null, and 'grammar_feedback': \"\".")
                appendLine("4. Stage Objective Completion & Wrap-up:")
                appendLine("   - Assess whether the user's latest response has completed the stage target objective ('${stage?.targetObjective ?: ""}').")
                appendLine("   - If the goal is met or the scenario has reached its natural conclusion:")
                appendLine("     * Wrap up the conversation in-character in English at level $level (e.g. warmly state that everything is sorted/completed for this stage and congratulate or wish them well).")
                appendLine("     * Provide the corresponding natural Persian translation in 'translation'.")
                appendLine("     * Set 'objective_completed': true.")
                appendLine("   - If the conversation is still ongoing and the objective is not yet reached, set 'objective_completed': false.")
                appendLine("5. ALWAYS return valid JSON matching this schema:")
                appendLine("{\n  \"reply\": \"English reply here\",\n  \"translation\": \"ترجمه فارسی پاسخ\",\n  \"has_grammar_error\": false,\n  \"corrected_sentence\": null,\n  \"grammar_feedback\": \"توضیح فارسی اشکال گرامری یا رشته خالی در صورت صحت\",\n  \"objective_completed\": false\n}")
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
                val grammarAnalysisObj = try { jsonObject["grammar_analysis"]?.jsonObject } catch (_: Exception) { null }
                val grammarFeedback = jsonObject["grammar_feedback"]?.jsonPrimitive?.content?.trim()
                    ?: grammarAnalysisObj?.get("error_details")?.jsonPrimitive?.content?.trim()
                    ?: ""
                val hasGrammarError = jsonObject["has_grammar_error"]?.jsonPrimitive?.booleanOrNull
                    ?: grammarAnalysisObj?.get("has_error")?.jsonPrimitive?.booleanOrNull
                    ?: grammarFeedback.isNotEmpty()
                val rawCorrected = jsonObject["corrected_sentence"]?.jsonPrimitive?.content?.trim()
                    ?: grammarAnalysisObj?.get("corrected_sentence")?.jsonPrimitive?.content?.trim()
                val correctedSentence = rawCorrected?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
                val objectiveCompleted = jsonObject["objective_completed"]?.jsonPrimitive?.booleanOrNull ?: false

                if (reply.isNotEmpty()) {
                    return ChatTurnResult(
                        reply = reply,
                        translation = translation.ifEmpty { null },
                        isObjectiveDone = objectiveCompleted,
                        hasGrammarError = hasGrammarError,
                        correctedSentence = if (hasGrammarError) correctedSentence else null,
                        grammarFeedback = if (hasGrammarError) grammarFeedback else ""
                    )
                }
            }
            val errorMsg = rawResult.exceptionOrNull()?.message ?: "AI chat turn returned empty reply"
            logger.error("AI chat turn failed: $errorMsg")
            throw AppException.ExternalServiceError("خطا در برقراری ارتباط با هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        } catch (e: Exception) {
            if (e is AppException) throw e
            logger.error("Error in generateAiChatTurn: ${e.message}", e)
            throw AppException.ExternalServiceError("خطا در برقراری ارتباط با هوش مصنوعی. لطفاً دوباره تلاش کنید.")
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
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace > firstBrace) {
            text = text.substring(firstBrace, lastBrace + 1)
        }
        return text.trim()
    }

    suspend fun generateHint(
        stageId: String,
        userId: UUID?,
        messages: List<HintMessageRequest>,
        userLevel: String? = null
    ): HintResponse {
        val stage = stageRepository.getStageDetail(stageId, userId)
        val resolvedUserLevel = userLevel?.trim()?.takeIf { it.isNotBlank() }
            ?: (if (userId != null) userRepo.getUserLanguageLevel(userId) else null)
            ?: "A1"
        val normalizedLevel = resolvedUserLevel.uppercase()

        return try {
            val systemPrompt = buildString {
                appendLine("You are an expert English conversation tutor assisting a learner in an interactive English speaking simulation scenario.")
                appendLine("Current Scenario / Stage: ${stage?.title ?: "English Speaking Stage"}")
                appendLine("Character Interacting With User: ${stage?.characterName ?: "Character"}")
                appendLine("Target Objective for the User: ${stage?.targetObjective ?: "Practice natural English conversation"}")
                appendLine()
                append(buildCefrDirectives(normalizedLevel))
                appendLine()
                appendLine("Task:")
                appendLine("Analyze the ongoing conversation between the user and the character.")
                appendLine("Generate a natural, polite, and helpful English sentence ('suggestion_en') that the USER can say right now to respond to the character and make progress towards the objective.")
                appendLine("The suggestion MUST strictly match the learner's proficiency level ($normalizedLevel) so they can comfortably understand and say it.")
                if (normalizedLevel == "A1") {
                    appendLine("Keep suggestion_en VERY SIMPLE (3 to 6 words maximum, only beginner A1 vocabulary).")
                }
                appendLine("Also provide a brief, clear Persian explanation ('explanation_fa') explaining why this phrase is appropriate or how it helps.")
                appendLine("ALWAYS return valid JSON matching this schema:")
                appendLine("{\n  \"suggestion_en\": \"English response suggestion\",\n  \"explanation_fa\": \"توضیح کوتاه فارسی برای زبان‌آموز\"\n}")
            }

            val chatMessages = mutableListOf<ChatMessage>()
            chatMessages.add(ChatMessage(role = ChatRole.System, content = systemPrompt))

            // Add conversation messages
            for (msg in messages.takeLast(6)) {
                val role = if (msg.role.equals("User", ignoreCase = true)) ChatRole.User else ChatRole.Assistant
                chatMessages.add(ChatMessage(role = role, content = msg.content))
            }

            val rawResult = aiApiService.requestRaw(chatMessages)
            if (rawResult.isSuccess) {
                val rawText = rawResult.getOrNull().orEmpty().trim()
                val cleanJson = cleanJsonResponse(rawText)
                val jsonObject = json.parseToJsonElement(cleanJson).jsonObject
                val suggestion = jsonObject["suggestion_en"]?.jsonPrimitive?.content?.trim().orEmpty()
                val explanation = jsonObject["explanation_fa"]?.jsonPrimitive?.content?.trim().orEmpty()
                if (suggestion.isNotEmpty() && explanation.isNotEmpty()) {
                    return HintResponse(suggestionEn = suggestion, explanationFa = explanation)
                }
            }
            val errorMsg = rawResult.exceptionOrNull()?.message ?: "AI hint request returned empty suggestion"
            logger.error("AI hint request failed: $errorMsg")
            throw AppException.ExternalServiceError("خطا در دریافت راهنما از هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        } catch (e: Exception) {
            if (e is AppException) throw e
            logger.error("Error in generateHint: ${e.message}", e)
            throw AppException.ExternalServiceError("خطا در دریافت راهنما از هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        }
    }

    private suspend fun generateAiStarterTurnStream(
        stage: StageDetailResponse?,
        level: String = "A1",
        onChunk: suspend (String) -> Unit
    ): Pair<String, String?> {
        return try {
            val systemPrompt = buildString {
                appendLine("You are an interactive conversational English learning assistant acting as a character in an English speaking simulation scenario.")
                appendLine("Current Stage: ${stage?.title ?: "English Speaking Stage"}")
                appendLine("Character Name: ${stage?.characterName ?: "Character"}")
                appendLine("Character Persona & Behavior: ${stage?.characterBehavior ?: "Friendly and helpful conversational partner"}")
                appendLine("Stage Target Objective: ${stage?.targetObjective ?: "Practice natural English conversation"}")
                appendLine()
                append(buildCefrDirectives(level))
                appendLine()
                appendLine("Task:")
                appendLine("Generate the OPENING greeting message from '${stage?.characterName ?: "Character"}' to start the conversation with the user in this scenario.")
                appendLine("The greeting MUST strictly match the learner's English level ($level) in vocabulary and sentence structure.")
                appendLine("Keep it natural, in-character, and concise (1 to 2 sentences max).")
                appendLine("IMPORTANT: Do NOT repeat fixed canned lines or memorized phrases. Dynamically formulate an original, natural opening that fits the character's persona, tone, and scenario context.")
                appendLine("Also provide a natural Persian translation.")
                appendLine("IMPORTANT: 'reply' MUST be the very first property in the returned JSON object.")
                appendLine("ALWAYS return valid JSON matching this schema:")
                appendLine("{\n  \"reply\": \"Opening greeting in English\",\n  \"translation\": \"ترجمه فارسی پیام آغازین\"\n}")
            }

            val chatMessages = listOf(ChatMessage(role = ChatRole.System, content = systemPrompt))
            val parser = ReplyStreamParser(onChunk)

            try {
                aiApiService.streamRequest(chatMessages, com.aallam.openai.api.chat.ChatResponseFormat.JsonObject).collect { chunk ->
                    parser.feed(chunk)
                }
            } catch (e: Exception) {
                if (e is AppException) throw e
                logger.warn("Streaming AI starter failed, attempting raw fallback: ${e.message}")
            }

            val rawText = parser.getAccumulated().trim()
            if (rawText.isNotEmpty()) {
                val cleanJson = cleanJsonResponse(rawText)
                val jsonObject = json.parseToJsonElement(cleanJson).jsonObject
                val reply = jsonObject["reply"]?.jsonPrimitive?.content?.trim().orEmpty()
                val translation = jsonObject["translation"]?.jsonPrimitive?.content?.trim().orEmpty()
                if (reply.isNotEmpty()) {
                    parser.finish(reply)
                    return Pair(reply, translation.ifEmpty { null })
                }
            }

            val rawResult = aiApiService.requestRaw(chatMessages)
            if (rawResult.isSuccess) {
                val cleanJson = cleanJsonResponse(rawResult.getOrNull().orEmpty())
                val jsonObject = json.parseToJsonElement(cleanJson).jsonObject
                val reply = jsonObject["reply"]?.jsonPrimitive?.content?.trim().orEmpty()
                val translation = jsonObject["translation"]?.jsonPrimitive?.content?.trim().orEmpty()
                if (reply.isNotEmpty()) {
                    parser.finish(reply)
                    return Pair(reply, translation.ifEmpty { null })
                }
            }

            throw AppException.ExternalServiceError("خطا در برقراری ارتباط با هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        } catch (e: Exception) {
            if (e is AppException) throw e
            logger.error("Error in generateAiStarterTurnStream: ${e.message}", e)
            throw AppException.ExternalServiceError("خطا در برقراری ارتباط با هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        }
    }

    private suspend fun generateAiChatTurnStream(
        stage: StageDetailResponse?,
        userMessage: String,
        history: List<StageChatMessageDto>,
        level: String = "A1",
        onChunk: suspend (String) -> Unit
    ): ChatTurnResult {
        return try {
            val systemPrompt = buildString {
                appendLine("You are an interactive conversational English learning assistant acting as a character in an English speaking simulation scenario.")
                appendLine("Current Stage: ${stage?.title ?: "English Speaking Stage"}")
                appendLine("Character Name: ${stage?.characterName ?: "Character"}")
                appendLine("Character Persona & Behavior: ${stage?.characterBehavior ?: "Friendly and helpful conversational partner"}")
                appendLine("Stage Target Objective: ${stage?.targetObjective ?: "Practice natural English conversation"}")
                appendLine()
                append(buildCefrDirectives(level))
                appendLine()
                appendLine("Instructions:")
                appendLine("1. Reply in-character as '${stage?.characterName ?: "Character"}' in spoken-style English strictly tailored to CEFR level $level. Keep your character reply concise (1 to 2 sentences) and conversational. Do NOT recite fixed scripts or memorized phrases; dynamically craft fresh, natural responses following the character's persona and conversational goals.")
                appendLine("2. Provide a fluent, natural Persian translation ('translation') for your English reply.")
                appendLine("3. Critically examine the user's latest message for English grammar, vocabulary, preposition, or phrasing errors suited to level $level:")
                appendLine("   - Pay close attention to word boundaries: never report words as glued or concatenated if standard spaces or punctuation separate them. Only report genuine spelling, grammar, preposition, or phrasing errors.")
                appendLine("   - If the user made ANY mistake: set 'has_grammar_error': true, provide the corrected English sentence in 'corrected_sentence' (matched to level $level), and provide a helpful, polite explanation in Persian ('grammar_feedback') explaining the issue simply and constructively.")
                appendLine("   - If the user's sentence is grammatically correct and natural: set 'has_grammar_error': false, 'corrected_sentence': null, and 'grammar_feedback': \"\".")
                appendLine("4. Stage Objective Completion & Wrap-up:")
                appendLine("   - Assess whether the user's latest response has completed the stage target objective ('${stage?.targetObjective ?: ""}').")
                appendLine("   - If the goal is met or the scenario has reached its natural conclusion:")
                appendLine("     * Wrap up the conversation in-character in English at level $level (e.g. warmly state that everything is sorted/completed for this stage and congratulate or wish them well).")
                appendLine("     * Provide the corresponding natural Persian translation in 'translation'.")
                appendLine("     * Set 'objective_completed': true.")
                appendLine("   - If the conversation is still ongoing and the objective is not yet reached, set 'objective_completed': false.")
                appendLine("5. IMPORTANT: 'reply' MUST be the very first property in the returned JSON object.")
                appendLine("6. ALWAYS return valid JSON matching this schema:")
                appendLine("{\n  \"reply\": \"English reply here\",\n  \"translation\": \"ترجمه فارسی پاسخ\",\n  \"has_grammar_error\": false,\n  \"corrected_sentence\": null,\n  \"grammar_feedback\": \"توضیح فارسی اشکال گرامری یا رشته خالی در صورت صحت\",\n  \"objective_completed\": false\n}")
            }

            val chatMessages = mutableListOf<ChatMessage>()
            chatMessages.add(ChatMessage(role = ChatRole.System, content = systemPrompt))

            val sanitizedHistory = history.filterIndexed { index, item ->
                !(index == history.lastIndex && item.role.equals("User", ignoreCase = true) && item.content.trim().equals(userMessage.trim(), ignoreCase = true))
            }.takeLast(6)

            for (item in sanitizedHistory) {
                val role = if (item.role.equals("User", ignoreCase = true)) ChatRole.User else ChatRole.Assistant
                chatMessages.add(ChatMessage(role = role, content = item.content))
            }

            chatMessages.add(ChatMessage(role = ChatRole.User, content = userMessage))

            val parser = ReplyStreamParser(onChunk)

            try {
                aiApiService.streamRequest(chatMessages, com.aallam.openai.api.chat.ChatResponseFormat.JsonObject).collect { chunk ->
                    parser.feed(chunk)
                }
            } catch (e: Exception) {
                if (e is AppException) throw e
                logger.warn("Streaming AI chat turn failed, attempting raw fallback: ${e.message}")
            }

            val rawText = parser.getAccumulated().trim()
            if (rawText.isNotEmpty()) {
                try {
                    val cleanJson = cleanJsonResponse(rawText)
                    val jsonObject = json.parseToJsonElement(cleanJson).jsonObject

                    val reply = jsonObject["reply"]?.jsonPrimitive?.content?.trim().orEmpty()
                    val translation = jsonObject["translation"]?.jsonPrimitive?.content?.trim().orEmpty()
                    val grammarAnalysisObj = try { jsonObject["grammar_analysis"]?.jsonObject } catch (_: Exception) { null }
                    val grammarFeedback = jsonObject["grammar_feedback"]?.jsonPrimitive?.content?.trim()
                        ?: grammarAnalysisObj?.get("error_details")?.jsonPrimitive?.content?.trim()
                        ?: ""
                    val hasGrammarError = jsonObject["has_grammar_error"]?.jsonPrimitive?.booleanOrNull
                        ?: grammarAnalysisObj?.get("has_error")?.jsonPrimitive?.booleanOrNull
                        ?: grammarFeedback.isNotEmpty()
                    val rawCorrected = jsonObject["corrected_sentence"]?.jsonPrimitive?.content?.trim()
                        ?: grammarAnalysisObj?.get("corrected_sentence")?.jsonPrimitive?.content?.trim()
                    val correctedSentence = rawCorrected?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
                    val objectiveCompleted = jsonObject["objective_completed"]?.jsonPrimitive?.booleanOrNull ?: false

                    if (reply.isNotEmpty()) {
                        parser.finish(reply)
                        return ChatTurnResult(
                            reply = reply,
                            translation = translation.ifEmpty { null },
                            isObjectiveDone = objectiveCompleted,
                            hasGrammarError = hasGrammarError,
                            correctedSentence = if (hasGrammarError) correctedSentence else null,
                            grammarFeedback = if (hasGrammarError) grammarFeedback else ""
                        )
                    }
                } catch (e: Exception) {
                    logger.warn("Parsing accumulated stream response failed (${e.message}), attempting raw fallback")
                }
            }

            val rawResult = aiApiService.requestRaw(chatMessages)
            if (rawResult.isSuccess) {
                val cleanJson = cleanJsonResponse(rawResult.getOrNull().orEmpty())
                val jsonObject = json.parseToJsonElement(cleanJson).jsonObject

                val reply = jsonObject["reply"]?.jsonPrimitive?.content?.trim().orEmpty()
                val translation = jsonObject["translation"]?.jsonPrimitive?.content?.trim().orEmpty()
                val grammarAnalysisObj = try { jsonObject["grammar_analysis"]?.jsonObject } catch (_: Exception) { null }
                val grammarFeedback = jsonObject["grammar_feedback"]?.jsonPrimitive?.content?.trim()
                    ?: grammarAnalysisObj?.get("error_details")?.jsonPrimitive?.content?.trim()
                    ?: ""
                val hasGrammarError = jsonObject["has_grammar_error"]?.jsonPrimitive?.booleanOrNull
                    ?: grammarAnalysisObj?.get("has_error")?.jsonPrimitive?.booleanOrNull
                    ?: grammarFeedback.isNotEmpty()
                val rawCorrected = jsonObject["corrected_sentence"]?.jsonPrimitive?.content?.trim()
                    ?: grammarAnalysisObj?.get("corrected_sentence")?.jsonPrimitive?.content?.trim()
                val correctedSentence = rawCorrected?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
                val objectiveCompleted = jsonObject["objective_completed"]?.jsonPrimitive?.booleanOrNull ?: false

                if (reply.isNotEmpty()) {
                    parser.finish(reply)
                    return ChatTurnResult(
                        reply = reply,
                        translation = translation.ifEmpty { null },
                        isObjectiveDone = objectiveCompleted,
                        hasGrammarError = hasGrammarError,
                        correctedSentence = if (hasGrammarError) correctedSentence else null,
                        grammarFeedback = if (hasGrammarError) grammarFeedback else ""
                    )
                }
            }

            throw AppException.ExternalServiceError("خطا در برقراری ارتباط با هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        } catch (e: Exception) {
            if (e is AppException) throw e
            logger.error("Error in generateAiChatTurnStream: ${e.message}", e)
            throw AppException.ExternalServiceError("خطا در برقراری ارتباط با هوش مصنوعی. لطفاً دوباره تلاش کنید.")
        }
    }
}

class ReplyStreamParser(
    private val onChunk: suspend (String) -> Unit
) {
    private val accumulated = StringBuilder()
    private var inReply = false
    private var replyFinished = false
    private var emittedLength = 0

    suspend fun feed(chunk: String) {
        accumulated.append(chunk)
        if (replyFinished) return

        val text = accumulated.toString()
        if (!inReply) {
            val replyKeyIndex = text.indexOf("\"reply\"")
            if (replyKeyIndex != -1) {
                val colonIndex = text.indexOf(':', replyKeyIndex + 7)
                if (colonIndex != -1) {
                    val openQuoteIndex = text.indexOf('"', colonIndex + 1)
                    if (openQuoteIndex != -1) {
                        inReply = true
                        processReplyContent(text.substring(openQuoteIndex + 1))
                    }
                }
            }
        } else {
            val replyStartIndex = findReplyStartInAccumulated()
            if (replyStartIndex != -1) {
                processReplyContent(text.substring(replyStartIndex))
            }
        }
    }

    private fun findReplyStartInAccumulated(): Int {
        val replyKeyIndex = accumulated.indexOf("\"reply\"")
        if (replyKeyIndex == -1) return -1
        val colonIndex = accumulated.indexOf(':', replyKeyIndex + 7)
        if (colonIndex == -1) return -1
        val openQuoteIndex = accumulated.indexOf('"', colonIndex + 1)
        if (openQuoteIndex == -1) return -1
        return openQuoteIndex + 1
    }

    private suspend fun processReplyContent(rawReplySoFar: String) {
        var isEscaped = false
        var closeQuoteIndex = -1
        for (i in rawReplySoFar.indices) {
            val c = rawReplySoFar[i]
            if (c == '\\' && !isEscaped) {
                isEscaped = true
                continue
            }
            if (c == '"' && !isEscaped) {
                closeQuoteIndex = i
                break
            }
            isEscaped = false
        }

        val currentReplyText = if (closeQuoteIndex != -1) {
            replyFinished = true
            rawReplySoFar.substring(0, closeQuoteIndex)
        } else {
            rawReplySoFar
        }

        val unescaped = unescapeJsonString(currentReplyText)
        if (unescaped.length > emittedLength) {
            val delta = unescaped.substring(emittedLength)
            emittedLength = unescaped.length
            if (delta.isNotEmpty()) {
                onChunk(delta)
            }
        }
    }

    suspend fun finish(finalReply: String) {
        val trimmed = finalReply.trim()
        if (emittedLength == 0 && trimmed.isNotEmpty()) {
            onChunk(trimmed)
            emittedLength = trimmed.length
        } else if (trimmed.length > emittedLength) {
            val delta = trimmed.substring(emittedLength)
            if (delta.isNotEmpty()) {
                onChunk(delta)
                emittedLength = trimmed.length
            }
        }
    }

    fun getAccumulated(): String = accumulated.toString()

    private fun unescapeJsonString(str: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < str.length) {
            val c = str[i]
            if (c == '\\' && i + 1 < str.length) {
                when (str[i + 1]) {
                    '"' -> { sb.append('"'); i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    '/' -> { sb.append('/'); i += 2 }
                    'b' -> { sb.append('\b'); i += 2 }
                    'f' -> { sb.append('\u000c'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    else -> { sb.append(c); i++ }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}

data class ChatTurnResult(
    val reply: String,
    val translation: String?,
    val isObjectiveDone: Boolean,
    val hasGrammarError: Boolean = false,
    val correctedSentence: String? = null,
    val grammarFeedback: String
)

