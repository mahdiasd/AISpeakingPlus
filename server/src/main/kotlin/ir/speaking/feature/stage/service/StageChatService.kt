package ir.speaking.feature.stage.service

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
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
    private val userRepo: UserRepo = UserRepo()
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

        val resolvedUserLevel = request.userLevel?.trim()?.takeIf { it.isNotBlank() }
            ?: (if (userId != null) userRepo.getUserLanguageLevel(userId) else null)
            ?: "A1"
        val normalizedLevel = resolvedUserLevel.uppercase()

        val voiceSid = KokoroVoices.ALL.firstOrNull { it.code.equals(stage?.voiceId, ignoreCase = true) }?.id ?: 0

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
                objectiveCompleted = false,
                finishTaskIndexes = emptyList()
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
            getInitialGreeting(stage?.orderIndex, stage?.characterName, level)
        } catch (e: Exception) {
            logger.warn("Error in generateAiStarterTurn: ${e.message}, using fallback starter")
            getInitialGreeting(stage?.orderIndex, stage?.characterName, level)
        }
    }

    private fun getInitialGreeting(orderIndex: Int?, characterName: String?, level: String = "A1"): Pair<String, String> {
        val isBeginner = level.equals("A1", ignoreCase = true)
        return when (orderIndex) {
            1 -> if (isBeginner) {
                Pair(
                    "Hello! Welcome aboard. Do you want chicken or pasta tonight?",
                    "سلام! به پرواز خوش آمدید. امشب مرغ می‌خواهید یا پاستا؟"
                )
            } else {
                Pair(
                    "Good evening! Welcome aboard. Would you like the grilled chicken with rice, or the vegetarian pasta tonight?",
                    "عصر بخیر! به پرواز خوش آمدید. امشب مرغ گریل شده با برنج میل دارید یا پاستای گیاهی؟"
                )
            }
            2 -> if (isBeginner) {
                Pair(
                    "Hello. Why are you visiting the UK?",
                    "سلام. چرا به بریتانیا سفر می‌کنید؟"
                )
            } else {
                Pair(
                    "Good day. What is the purpose of your visit to the United Kingdom?",
                    "روز بخیر. هدف شما از سفر به بریتانیا چیست؟"
                )
            }
            3 -> if (isBeginner) {
                Pair(
                    "Hello! Can I help you with your bags?",
                    "سلام! می‌توانم در مورد چمدان‌هایتان کمکتان کنم؟"
                )
            } else {
                Pair(
                    "Hello! How can I help you today? Did your luggage not arrive on the carousel?",
                    "سلام! چطور می‌توانم کمکتان کنم؟ آیا چمدانتان روی نوار نقاله نیامده است؟"
                )
            }
            4 -> if (isBeginner) {
                Pair(
                    "Hello! Welcome to the train station. Do you need a ticket?",
                    "سلام! به ایستگاه قطار خوش آمدید. آیا بلیت می‌خواهید؟"
                )
            } else {
                Pair(
                    "Hello there! Welcome to the London Underground. Do you need an Oyster card or a standard travelcard?",
                    "سلام! به متروی لندن خوش آمدید. کارت اویستر می‌خواهید یا بلیت استاندارد سفر؟"
                )
            }
            5 -> if (isBeginner) {
                Pair(
                    "Hello! Welcome to Russell Hall. Are you checking in today?",
                    "سلام! به خوابگاه راسل خوش آمدید. آیا امروز برای تحویل اتاق آمده‌اید؟"
                )
            } else {
                Pair(
                    "Welcome to Russell Hall! You must be exhausted from traveling. Are you checking in today?",
                    "به خوابگاه راسل خوش آمدید! حتماً از سفر خسته شده‌اید. آیا امروز برای تحویل اتاق تشریف آورده‌اید؟"
                )
            }
            6 -> if (isBeginner) {
                Pair(
                    "Hi! Are you my new flatmate? I am Liam.",
                    "سلام! هم‌اتاقی جدید من هستی؟ من لیام هستم."
                )
            } else {
                Pair(
                    "Hey there! Welcome to the flat. You must be my new flatmate—I'm Liam!",
                    "سلام! به آپارتمان خوش آمدی. حتماً هم‌اتاقی جدیدم هستی—من لیام هستم!"
                )
            }
            7 -> if (isBeginner) {
                Pair(
                    "Hello! Do you need a UK SIM card for your phone?",
                    "سلام! آیا برای گوشیتان سیم‌کارت بریتانیا می‌خواهید؟"
                )
            } else {
                Pair(
                    "Hello! Welcome to EE Mobile. Are you looking to set up a new UK phone plan or SIM card today?",
                    "سلام! به فروشگاه ای‌ای خوش آمدید. آیا به دنبال راه‌اندازی سیم‌کارت یا طرح مخابراتی در بریتانیا هستید؟"
                )
            }
            8 -> if (isBeginner) {
                Pair(
                    "Hello! Can I help you find something in the supermarket?",
                    "سلام! می‌توانم کمکتان کنم چیزی را در سوپرمارکت پیدا کنید؟"
                )
            } else {
                Pair(
                    "Hi! Welcome to Sainsbury's. Are you looking for any particular groceries or household items today?",
                    "سلام! به سینزبریز خوش آمدید. آیا به دنبال اقلام خوراکی یا وسایل خاصی می‌گردید؟"
                )
            }
            9 -> if (isBeginner) {
                Pair(
                    "Hello! Do you want to open a student bank account?",
                    "سلام! آیا می‌خواهید حساب بانکی دانشجویی باز کنید؟"
                )
            } else {
                Pair(
                    "Good morning! Welcome to Barclays. Are you here to open a student bank account with us today?",
                    "صبح بخیر! به بارکلیز خوش آمدید. آیا برای افتتاح حساب بانکی دانشجویی تشریف آورده‌اید؟"
                )
            }
            10 -> if (isBeginner) {
                Pair(
                    "Hello! Welcome to campus. Are you here for student registration?",
                    "سلام! به دانشگاه خوش آمدید. برای ثبت‌نام دانشجویی آمده‌اید؟"
                )
            } else {
                Pair(
                    "Hello and welcome to university registration day! Have you got your enrollment letter and passport with you?",
                    "سلام و به روز ثبت‌نام دانشگاه خوش آمدید! آیا برگه ثبت‌نام و گذرنامه‌تان را همراه دارید؟"
                )
            }
            11 -> if (isBeginner) {
                Pair(
                    "Hello! Welcome to the library. Do you need help finding books?",
                    "سلام! به کتابخانه خوش آمدید. برای پیدا کردن کتاب‌ها راهنمایی می‌خواهید؟"
                )
            } else {
                Pair(
                    "Good afternoon! Welcome to the central university library. How can I assist you with your library account or book searches?",
                    "عصر بخیر! به کتابخانه مرکزی دانشگاه خوش آمدید. چگونه می‌توانم در مورد حساب کاربری یا جستجوی کتاب کمکتان کنم؟"
                )
            }
            12 -> if (isBeginner) {
                Pair(
                    "Hi! What coffee or food do you want today?",
                    "سلام! امروز چه قهوه یا خوراکی‌ای می‌خواهید؟"
                )
            } else {
                Pair(
                    "Hey there! Welcome to the campus cafe. What can I get started for you today—coffee, tea, or a hot snack?",
                    "سلام! به کافه پردیس خوش آمدید. چه چیزی برایتان آماده کنم—قهوه، چای یا یک میان‌وعده داغ؟"
                )
            }
            13 -> if (isBeginner) {
                Pair(
                    "Hello! Are you here for the student job interview?",
                    "سلام! برای مصاحبه کاری دانشجویی آمده‌اید؟"
                )
            } else {
                Pair(
                    "Good morning! Thanks for coming in. Are you ready for your student assistant job interview today?",
                    "صبح بخیر! ممنون از حضورتان. آیا برای مصاحبه شغلی دستیار دانشجویی آماده هستید؟"
                )
            }
            14 -> if (isBeginner) {
                Pair(
                    "Hello. Please sit down. How do you feel today?",
                    "سلام. لطفاً بنشینید. امروز چه احساسی دارید؟"
                )
            } else {
                Pair(
                    "Good morning, come take a seat! What symptoms or health concerns bring you in today?",
                    "صبح بخیر، بفرمایید بنشینید! چه علائم یا مشکلات سلامتی شما را به درمانگاه آورده است؟"
                )
            }
            15 -> if (isBeginner) {
                Pair(
                    "Hello! Welcome to the presentation room. Are you ready to present?",
                    "سلام! به اتاق ارائه خوش آمدید. آیا برای ارائه آماده‌اید؟"
                )
            } else {
                Pair(
                    "Welcome everyone to the seminar presentation symposium! Is your team ready to present your term project?",
                    "به همایش ارائه پروژه سمینار خوش آمدید! آیا تیم شما برای ارائه پروژه پایان ترم آماده است؟"
                )
            }
            else -> {
                val who = characterName?.takeIf { it.isNotBlank() } ?: "there"
                if (isBeginner) {
                    Pair(
                        "Hello! Welcome. How can I help you?",
                        "سلام! خوش آمدید. چطور می‌توانم کمکتان کنم؟"
                    )
                } else {
                    Pair(
                        "Hello $who! Welcome. How can I help you today?",
                        "سلام! خوش آمدید. چطور می‌توانم امروز کمکتان کنم؟"
                    )
                }
            }
        }
    }


    private suspend fun generateNextTurn(
        stageOrderIndex: Int,
        characterName: String,
        characterBehavior: String,
        userMessage: String,
        history: List<StageChatMessageDto>,
        level: String = "A1"
    ): Triple<String, String, Boolean> {
        val lower = userMessage.lowercase()
        val userTurnCount = history.count { it.role.equals("User", ignoreCase = true) } + 1
        val isBeginner = level.equals("A1", ignoreCase = true)

        // Stage 1: In-Flight to London
        if (stageOrderIndex == 1) {
            return when {
                userTurnCount == 1 || lower.contains("chicken") || lower.contains("pasta") || lower.contains("vegetarian") || lower.contains("rice") -> {
                    if (isBeginner) {
                        Triple(
                            "Sure! What do you want to drink? We have water, tea, or juice.",
                            "حتماً! چه نوشیدنی‌ای می‌خواهید؟ آب، چای یا آب‌میوه داریم.",
                            false
                        )
                    } else {
                        Triple(
                            "Certainly! And what can I get you to drink with that? We have orange juice, water, apple juice, or hot tea.",
                            "حتماً! و مایلید همراه آن چه نوشیدنی‌ای برایتان بیاورم؟ آب پرتقال، آب، آب سیب یا چای داغ داریم.",
                            false
                        )
                    }
                }
                userTurnCount == 2 || lower.contains("juice") || lower.contains("water") || lower.contains("tea") || lower.contains("coke") -> {
                    if (isBeginner) {
                        Triple(
                            "Here is your drink. Are you traveling to London for holiday or school?",
                            "بفرمایید این نوشیدنی شماست. آیا برای تعطیلات به لندن می‌روید یا دانشگاه؟",
                            false
                        )
                    } else {
                        Triple(
                            "Here you are, enjoy your meal! It's quite a long journey tonight—are you traveling to London for a holiday or for university studies?",
                            "بفرمایید، نوش جان! امشب سفر طولانی‌ای در پیش است؛ آیا برای تعطیلات به لندن می‌روید یا برای تحصیلات دانشگاهی؟",
                            false
                        )
                    }
                }
                else -> {
                    if (isBeginner) {
                        Triple(
                            "Great! Welcome to London. Enjoy your food! We have finished this mission.",
                            "عالی است! به لندن خوش آمدید. نوش جان! ماموریت این مرحله به پایان رسید.",
                            true
                        )
                    } else {
                        Triple(
                            "That's wonderful! London is an amazing city for students. Best of luck with your studies and enjoy your meal! We have accomplished our mission for this flight.",
                            "فوق‌العاده است! لندن شهری بی‌نظیر برای دانشجویان است. با آرزوی موفقیت در تحصیلات و نوش جان! هدف این مرحله از مکالمه با موفقیت به پایان رسید.",
                            true
                        )
                    }
                }
            }
        }

        // Stage 2: Heathrow Border Control
        if (stageOrderIndex == 2) {
            return when {
                userTurnCount == 1 || lower.contains("study") || lower.contains("student") || lower.contains("university") -> {
                    if (isBeginner) {
                        Triple(
                            "What school do you go to, and what do you study?",
                            "به کدام دانشگاه می‌روید و چه می‌خوانید؟",
                            false
                        )
                    } else {
                        Triple(
                            "Which university will you be attending, and what course are you studying?",
                            "در کدام دانشگاه تحصیل خواهید کرد و رشته تحصیلی‌تان چیست؟",
                            false
                        )
                    }
                }
                userTurnCount == 2 || lower.contains("london") || lower.contains("college") || lower.contains("engineering") || lower.contains("science") -> {
                    if (isBeginner) {
                        Triple(
                            "Where will you stay in London?",
                            "در لندن کجا اقامت خواهید داشت؟",
                            false
                        )
                    } else {
                        Triple(
                            "Where will you be staying while studying in London, and how long is your course?",
                            "در طول تحصیل در لندن کجا اقامت خواهید داشت و دوره تحصیلی شما چقدر طول می‌کشد؟",
                            false
                        )
                    }
                }
                else -> {
                    if (isBeginner) {
                        Triple(
                            "Everything is good. Welcome to the UK! Your border check is finished.",
                            "همه چیز خوب است. به بریتانیا خوش آمدید! بررسی مدارک شما به پایان رسید.",
                            true
                        )
                    } else {
                        Triple(
                            "Everything is in order. Welcome to the United Kingdom, and all the best with your academic studies! Your border clearance is complete.",
                            "همه مدارک کامل و مرتب است. به پادشاهی متحده خوش آمدید و با آرزوی موفقیت در تحصیلات دانشگاهی‌تان! بررسی مدارک با موفقیت انجام شد.",
                            true
                        )
                    }
                }
            }
        }

        // Generic fallback turn
        return Triple(
            "Thank you for sharing that! We have accomplished the conversation goal for this stage. You can finish or continue practicing.",
            "ممنون از همراهی شما! هدف مکالمه در این مرحله با موفقیت محقق شد. می‌توانید مرحله را به پایان برسانید یا به تمرین ادامه دهید.",
            userTurnCount >= 3
        )
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
                appendLine("1. Reply in-character as '${stage?.characterName ?: "Character"}' in spoken-style English strictly tailored to CEFR level $level. Keep your character reply concise (1 to 2 sentences) and conversational.")
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
            } else {
                logger.warn("AI API request failed: ${rawResult.exceptionOrNull()?.message}")
            }
            fallbackChatTurn(stage, userMessage, history, level)
        } catch (e: Exception) {
            logger.warn("Error in generateAiChatTurn: ${e.message}, falling back to offline turn")
            fallbackChatTurn(stage, userMessage, history, level)
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
        history: List<StageChatMessageDto>,
        level: String = "A1"
    ): ChatTurnResult {
        val (reply, translation, isObjectiveDone) = generateNextTurn(
            stageOrderIndex = stage?.orderIndex ?: 1,
            characterName = stage?.characterName ?: "Character",
            characterBehavior = stage?.characterBehavior ?: "",
            userMessage = userMessage,
            history = history,
            level = level
        )
        return ChatTurnResult(
            reply = reply,
            translation = translation,
            isObjectiveDone = isObjectiveDone,
            hasGrammarError = false,
            correctedSentence = null,
            grammarFeedback = ""
        )
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
            } else {
                logger.warn("AI Hint request failed: ${rawResult.exceptionOrNull()?.message}")
            }
            fallbackHint(stage, messages, normalizedLevel)
        } catch (e: Exception) {
            logger.warn("Error in generateHint: ${e.message}, using fallback hint")
            fallbackHint(stage, messages, normalizedLevel)
        }
    }

    private fun fallbackHint(
        stage: StageDetailResponse?,
        messages: List<HintMessageRequest>,
        level: String = "A1"
    ): HintResponse {
        val lastAssistantMessage = messages.lastOrNull {
            it.role.equals("Model", ignoreCase = true) || it.role.equals("Assistant", ignoreCase = true)
        }?.content?.lowercase().orEmpty()

        val isBeginner = level.equals("A1", ignoreCase = true)

        return when (stage?.orderIndex) {
            1 -> {
                when {
                    lastAssistantMessage.contains("drink") || lastAssistantMessage.contains("tea") || lastAssistantMessage.contains("juice") -> {
                        if (isBeginner) {
                            HintResponse(
                                suggestionEn = "I want water and tea, please.",
                                explanationFa = "می‌توانید برای سفارش نوشیدنی خیلی ساده بگویید: «I want water and tea, please.»"
                            )
                        } else {
                            HintResponse(
                                suggestionEn = "Could I please have a hot tea and a glass of water?",
                                explanationFa = "می‌توانید برای سفارش نوشیدنی از عبارت مؤدبانه «Could I please have...» استفاده کنید."
                            )
                        }
                    }
                    lastAssistantMessage.contains("holiday") || lastAssistantMessage.contains("study") || lastAssistantMessage.contains("traveling") -> {
                        if (isBeginner) {
                            HintResponse(
                                suggestionEn = "I travel to London to study.",
                                explanationFa = "می‌توانید هدف سفر خود را با جمله‌ای ساده بیان کنید: «I travel to London to study.»"
                            )
                        } else {
                            HintResponse(
                                suggestionEn = "I am traveling to London for my university studies.",
                                explanationFa = "می‌توانید هدف سفر خود (تحصیل در دانشگاه یا تعطیلات) را با جمله‌ای ساده و واضح بیان کنید."
                            )
                        }
                    }
                    else -> {
                        if (isBeginner) {
                            HintResponse(
                                suggestionEn = "I want chicken with rice, please.",
                                explanationFa = "می‌توانید غذای مورد نظرتان را خیلی ساده درخواست کنید: «I want chicken with rice, please.»"
                            )
                        } else {
                            HintResponse(
                                suggestionEn = "I would like the grilled chicken with rice, please.",
                                explanationFa = "می‌توانید غذای مورد نظرتان را به شکل مؤدبانه با عبارت «I would like...» درخواست کنید."
                            )
                        }
                    }
                }
            }
            2 -> {
                when {
                    lastAssistantMessage.contains("staying") || lastAssistantMessage.contains("long") -> {
                        if (isBeginner) {
                            HintResponse(
                                suggestionEn = "I will stay in London for one year.",
                                explanationFa = "می‌توانید خیلی ساده بگویید: «I will stay in London for one year.»"
                            )
                        } else {
                            HintResponse(
                                suggestionEn = "I will be staying in central London and my course lasts for one year.",
                                explanationFa = "محل اقامت در لندن و طول دوره تحصیلی‌تان را توضیح دهید."
                            )
                        }
                    }
                    else -> {
                        if (isBeginner) {
                            HintResponse(
                                suggestionEn = "I want to study at university.",
                                explanationFa = "می‌توانید هدف سفرتان را ساده بگویید: «I want to study at university.»"
                            )
                        } else {
                            HintResponse(
                                suggestionEn = "I am here to study computer science at the university.",
                                explanationFa = "هدف از سفر و رشته تحصیلی‌تان را به افسر مرزی اعلام کنید."
                            )
                        }
                    }
                }
            }
            else -> {
                if (isBeginner) {
                    HintResponse(
                        suggestionEn = "Can you please help me?",
                        explanationFa = "می‌توانید خیلی ساده بپرسید: «Can you please help me?»"
                    )
                } else {
                    HintResponse(
                        suggestionEn = "Could you please explain what I should do next?",
                        explanationFa = "می‌توانید مؤدبانه از هم‌صحبت خود بخواهید مرحله بعدی را برایتان توضیح دهد."
                    )
                }
            }
        }
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

