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
import kotlinx.serialization.json.Json
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
            val (initialGreeting, initialTranslation) = getInitialGreeting(stage?.orderIndex, stage?.characterName)
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

        // Evaluate user's message grammar
        val grammarFeedback = evaluateGrammar(userMessage)

        // Generate character's next reply
        val (nextReply, replyTranslation, isObjectiveDone) = generateNextTurn(
            stageOrderIndex = stage?.orderIndex ?: 1,
            characterName = stage?.characterName ?: "Character",
            characterBehavior = stage?.characterBehavior ?: "",
            userMessage = userMessage,
            history = history
        )

        val audioUrl = trySynthesize(nextReply, voiceSid)

        return StageChatResponse(
            message = nextReply,
            translatedMessage = replyTranslation,
            audioUrl = audioUrl,
            grammarFeedbackFa = grammarFeedback,
            objectiveCompleted = isObjectiveDone,
            finishTaskIndexes = if (isObjectiveDone) listOf(0, 1) else emptyList()
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
}
