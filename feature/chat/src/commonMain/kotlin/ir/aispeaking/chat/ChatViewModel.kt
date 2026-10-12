@file:OptIn(kotlin.time.ExperimentalTime::class)

package ir.aispeaking.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.getErrorMessage
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.model.stage.GrammarErrorDetail
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stt.SttStreamEvent
import ir.aispeaking.domain.repository.stage.StageRepository
import ir.aispeaking.domain.usecase.auth.GetCurrentAccessTierUseCase
import ir.aispeaking.domain.usecase.stage.GetStageDetailUseCase
import ir.aispeaking.domain.usecase.stage.RequestStageHintUseCase
import ir.aispeaking.domain.usecase.stage.SendStageChatMessageUseCase
import ir.aispeaking.domain.usecase.stage.SubmitStageEvaluationUseCase
import ir.aispeaking.domain.usecase.stt.CloseSttSessionUseCase
import ir.aispeaking.domain.usecase.stt.StreamSpeechToTextUseCase
import ir.aispeaking.domain.usecase.user.GetUserProfileUseCase
import ir.aispeaking.chat.audio.StageAudioController
import ir.aispeaking.chat.component.ChatInputMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.koin.core.annotation.Factory

data class ChatUiState(
    val stage: Stage? = null,
    val userLevel: String = "A1",
    val stageLoadError: String? = null,
    val chats: ImmutableList<Chat> = persistentListOf(),
    val inputMode: ChatInputMode = ChatInputMode.VOICE,
    val messageText: String = "",
    val isRecording: Boolean = false,
    val isModelSpeaking: Boolean = false,
    val isRequestingHint: Boolean = false,
    val hintsUsedCount: Int = 0,
    val turnsCount: Int = 0,
    val currentHintSuggestion: String? = null,
    val currentHintExplanation: String? = null,
    val hintError: String? = null,
    val showEvaluationDialog: Boolean = false,
    val isObjectiveCompleted: Boolean = false,
    val showFinishConfirmDialog: Boolean = false,
    val isSubmittingEvaluation: Boolean = false,
    val evaluationSession: EvaluationSession? = null,
    val earnedStars: Int = 0,
    val earnedScore: Int = 0,
    val currentlyPlayingUid: String? = null
)

@Factory
class ChatViewModel(
    private val getStageDetailUseCase: GetStageDetailUseCase,
    private val getCurrentAccessTierUseCase: GetCurrentAccessTierUseCase,
    private val sendStageChatMessageUseCase: SendStageChatMessageUseCase,
    private val requestStageHintUseCase: RequestStageHintUseCase,
    private val submitStageEvaluationUseCase: SubmitStageEvaluationUseCase,
    private val stageRepository: StageRepository,
    private val audioController: StageAudioController,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val streamSpeechToTextUseCase: StreamSpeechToTextUseCase,
    private val closeSttSessionUseCase: CloseSttSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentStageId: String = ""
    private var currentTier: AccessTier = AccessTier.GUEST
    private var sttStreamJob: Job? = null

    fun initStage(stageId: String, tier: AccessTier? = null) {
        if (currentStageId == stageId && _uiState.value.stage != null) return
        currentStageId = stageId
        _uiState.update { it.copy(stageLoadError = null) }

        viewModelScope.launch {
            val resolvedTier = tier ?: getCurrentAccessTierUseCase()
            currentTier = resolvedTier
            val profileResult = getUserProfileUseCase()
            val userLevel = when (profileResult) {
                is DataResult.Success -> profileResult.data.languageLevel.takeIf { it.isNotBlank() } ?: "A1"
                else -> "A1"
            }

            when (val result = getStageDetailUseCase(stageId, resolvedTier)) {
                is DataResult.Success -> {
                    val stage = result.data
                    _uiState.update {
                        it.copy(
                            stage = stage,
                            userLevel = userLevel,
                            stageLoadError = null,
                            chats = persistentListOf(),
                            turnsCount = 0,
                            hintsUsedCount = 0,
                            showEvaluationDialog = false,
                            isObjectiveCompleted = false,
                            showFinishConfirmDialog = false,
                            isSubmittingEvaluation = false,
                            evaluationSession = null,
                            currentHintSuggestion = null,
                            currentHintExplanation = null
                        )
                    }

                    // Requirement 1: If conversation is started by AI, request initial message from server
                    if (stage.initialSpeaker == "Model") {
                        requestInitialAiGreeting(stageId)
                    }
                }
                is DataResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            stage = null,
                            stageLoadError = "امکان بارگذاری اطلاعات این مرحله وجود ندارد. لطفاً دسترسی یا اتصال اینترنت خود را بررسی کنید."
                        )
                    }
                }
            }
        }
    }

    fun retryLoadStage() {
        val id = currentStageId
        if (id.isNotBlank()) {
            currentStageId = ""
            initStage(id)
        }
    }

    private fun requestInitialAiGreeting(stageId: String) {
        viewModelScope.launch {
            val level = _uiState.value.userLevel
            _uiState.update {
                it.copy(
                    chats = kotlinx.collections.immutable.persistentListOf(Chat.WaitingForAi),
                    isModelSpeaking = true
                )
            }

            when (val result = sendStageChatMessageUseCase(stageId, userMessage = null, history = emptyList(), userLevel = level)) {
                is DataResult.Success -> {
                    val d = result.data
                    val aiUid = "ai_init_${Clock.System.now().toEpochMilliseconds()}"
                    streamAiResponse(
                        aiUid = aiUid,
                        fullMessage = d.message,
                        translatedMessage = d.translatedMessage,
                        audioUrl = d.audioUrl
                    )
                }
                is DataResult.Failure -> {
                    val currentStage = _uiState.value.stage
                    val (fallbackMessage, fallbackTranslation) = resolveStageFallbackInitialGreeting(currentStage, level)
                    val aiUid = "ai_init_fallback"
                    streamAiResponse(
                        aiUid = aiUid,
                        fullMessage = fallbackMessage,
                        translatedMessage = fallbackTranslation,
                        audioUrl = null
                    )
                }
            }
        }
    }

    private fun resolveStageFallbackInitialGreeting(stage: Stage?, level: String): Pair<String, String> {
        val isBeginner = level.equals("A1", ignoreCase = true)
        return when (stage?.orderIndex) {
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
                if (isBeginner) {
                    Pair(
                        "Hello! Welcome. How can I help you?",
                        "سلام! خوش آمدید. چطور می‌توانم کمکتان کنم؟"
                    )
                } else {
                    Pair(
                        "Hello! Welcome. How can I help you today?",
                        "سلام! خوش آمدید. چطور می‌توانم امروز کمکتان کنم؟"
                    )
                }
            }
        }
    }

    fun playAiVoice(uid: String) {
        val targetChat = _uiState.value.chats.find { it.uid == uid } as? Chat.Ai ?: return

        if (targetChat.audioUrl.isNullOrBlank()) {
            return
        }

        // Requirement 3: Toggle play / stop
        if (targetChat.voiceState == AiVoiceState.Playing) {
            stopAiVoice(uid)
            return
        }

        // Set target chat to Playing, all others to Stopped
        _uiState.update { state ->
            val updated = state.chats.map {
                if (it is Chat.Ai) {
                    if (it.uid == uid) it.copy(voiceState = AiVoiceState.Playing)
                    else it.copy(voiceState = AiVoiceState.Stopped)
                } else it
            }
            state.copy(chats = updated.toImmutableList(), currentlyPlayingUid = uid)
        }

        audioController.playVoice(targetChat.audioUrl) {
            onVoicePlaybackEnded(uid)
        }
    }

    fun stopAiVoice(uid: String) {
        audioController.stopVoice()
        _uiState.update { state ->
            val updated = state.chats.map {
                if (it is Chat.Ai && it.uid == uid) it.copy(voiceState = AiVoiceState.Stopped)
                else it
            }
            state.copy(chats = updated.toImmutableList(), currentlyPlayingUid = null)
        }
    }

    private fun onVoicePlaybackEnded(uid: String) {
        _uiState.update { state ->
            val updated = state.chats.map {
                if (it is Chat.Ai && it.uid == uid) it.copy(voiceState = AiVoiceState.Stopped)
                else it
            }
            state.copy(
                chats = updated.toImmutableList(),
                currentlyPlayingUid = if (state.currentlyPlayingUid == uid) null else state.currentlyPlayingUid
            )
        }
    }

    // Requirements 4 & 5: Voice recording, realtime transcription, and 5-second silence auto-pause
    fun toggleRecording(start: Boolean) {
        if (start) {
            audioController.stopVoice()
            _uiState.update { it.copy(isRecording = true) }

            sttStreamJob?.cancel()
            sttStreamJob = viewModelScope.launch {
                try {
                    streamSpeechToTextUseCase().collect { event ->
                        when (event) {
                            is SttStreamEvent.PartialTranscript -> {
                                if (event.text.isNotBlank()) {
                                    _uiState.update { it.copy(messageText = event.text) }
                                }
                            }
                            is SttStreamEvent.FinalTranscript -> {
                                if (event.text.isNotBlank()) {
                                    _uiState.update { it.copy(messageText = event.text) }
                                }
                            }
                            else -> Unit
                        }
                    }
                } catch (_: Exception) {}
            }

            audioController.startRecording(
                onSpeechRecognized = { text ->
                    _uiState.update { it.copy(messageText = text) }
                },
                onSilenceDetected = {
                    // 5-second silence detected -> auto pause & stop recording
                    audioController.stopRecording()
                    sttStreamJob?.cancel()
                    sttStreamJob = null
                    viewModelScope.launch {
                        try {
                            closeSttSessionUseCase()
                        } catch (_: Exception) {}
                    }
                    _uiState.update { it.copy(isRecording = false) }
                }
            )
        } else {
            audioController.stopRecording()
            sttStreamJob?.cancel()
            sttStreamJob = null
            viewModelScope.launch {
                try {
                    closeSttSessionUseCase()
                } catch (_: Exception) {}
            }
            _uiState.update { it.copy(isRecording = false) }
        }
    }

    fun onMessageTextChanged(newText: String) {
        _uiState.update { it.copy(messageText = newText) }
    }

    fun setInputMode(mode: ChatInputMode) {
        if (mode == ChatInputMode.TEXT && _uiState.value.isRecording) {
            audioController.stopRecording()
            sttStreamJob?.cancel()
            sttStreamJob = null
            viewModelScope.launch {
                try {
                    closeSttSessionUseCase()
                } catch (_: Exception) {}
            }
            _uiState.update { it.copy(isRecording = false, inputMode = mode) }
        } else {
            _uiState.update { it.copy(inputMode = mode) }
        }
    }

    fun clearMessageText() {
        _uiState.update { it.copy(messageText = "") }
    }

    // Requirement 4 & 7: Send message, grammar evaluation & beautiful correction box
    fun sendMessage() {
        val stateSnapshot = _uiState.value
        if (stateSnapshot.isSubmittingEvaluation || stateSnapshot.isModelSpeaking) return
        val maxTurnsLimit = stateSnapshot.stage?.maxTurns ?: 12
        if (stateSnapshot.turnsCount >= maxTurnsLimit) {
            submitEvaluation()
            return
        }

        val content = stateSnapshot.messageText.trim()
        if (content.isBlank()) return

        if (stateSnapshot.isRecording) {
            audioController.stopRecording()
            sttStreamJob?.cancel()
            sttStreamJob = null
            viewModelScope.launch {
                try {
                    closeSttSessionUseCase()
                } catch (_: Exception) {}
            }
            _uiState.update { it.copy(isRecording = false) }
        }

        val userUid = "user_${Clock.System.now().toEpochMilliseconds()}"
        val userChat = Chat.User(
            uid = userUid,
            message = content,
            status = ChatStatus.Sending
        )

        val updatedTurns = _uiState.value.turnsCount + 1

        _uiState.update {
            it.copy(
                chats = (it.chats + userChat).toImmutableList(),
                messageText = "",
                turnsCount = updatedTurns,
                isModelSpeaking = false
            )
        }

        viewModelScope.launch {
            val history = _uiState.value.chats
                .filter { it.uid != userUid && it !is Chat.WaitingForAi }
                .mapNotNull {
                    when (it) {
                        is Chat.User -> "User" to it.message
                        is Chat.Ai -> "Model" to it.message
                        else -> null
                    }
                }

            when (val result = sendStageChatMessageUseCase(currentStageId, content, history, _uiState.value.userLevel)) {
                is DataResult.Success -> {
                    val turn = result.data

                    // Update user chat with AI grammar check result, and now show AI waiting/typing indicator
                    val answeredUserChat = userChat.copy(
                        status = ChatStatus.Answered(
                            grammar = turn.grammarFeedbackFa,
                            correctedSentence = turn.correctedSentence,
                            hasGrammarError = turn.hasGrammarError || turn.grammarFeedbackFa.isNotBlank()
                        )
                    )

                    val isTurnGoalCompleted = turn.objectiveCompleted
                    val isGoalCompleted = isTurnGoalCompleted || _uiState.value.isObjectiveCompleted

                    _uiState.update { state ->
                        val updated = state.chats.map {
                            if (it.uid == userUid) answeredUserChat else it
                        }
                        state.copy(
                            chats = (updated + Chat.WaitingForAi).toImmutableList(),
                            isModelSpeaking = true,
                            isObjectiveCompleted = isGoalCompleted
                        )
                    }

                    delay(700)

                    val aiUid = "ai_${Clock.System.now().toEpochMilliseconds()}"
                    streamAiResponse(
                        aiUid = aiUid,
                        fullMessage = turn.message,
                        translatedMessage = turn.translatedMessage,
                        audioUrl = turn.audioUrl,
                        objectiveCompleted = isTurnGoalCompleted,
                        finishTaskIndexes = emptyList()
                    )

                    val maxTurns = _uiState.value.stage?.maxTurns ?: 12
                    if (isGoalCompleted || _uiState.value.turnsCount >= maxTurns) {
                        delay(900)
                        submitEvaluation()
                    }
                }
                is DataResult.Failure -> {
                    // Mark as failed
                    val failedUserChat = userChat.copy(status = ChatStatus.Failed)
                    _uiState.update { state ->
                        val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi }
                        val updated = withoutWaiting.map {
                            if (it.uid == userUid) failedUserChat else it
                        }
                        state.copy(
                            chats = updated.toImmutableList(),
                            isModelSpeaking = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun streamAiResponse(
        aiUid: String,
        fullMessage: String,
        translatedMessage: String?,
        audioUrl: String?,
        objectiveCompleted: Boolean = false,
        finishTaskIndexes: List<Int> = emptyList()
    ) {
        val hasAudio = !audioUrl.isNullOrBlank()
        val initialAiChat = Chat.Ai(
            uid = aiUid,
            message = "",
            translatedMessage = translatedMessage,
            voiceState = if (hasAudio) AiVoiceState.Playing else AiVoiceState.Stopped,
            audioUrl = audioUrl,
            objectiveCompleted = objectiveCompleted,
            finishTaskIndexes = finishTaskIndexes.toImmutableList()
        )

        _uiState.update { state ->
            val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi }
            state.copy(
                chats = (withoutWaiting + initialAiChat).toImmutableList(),
                isModelSpeaking = false,
                currentlyPlayingUid = if (hasAudio) aiUid else null
            )
        }

        if (hasAudio) {
            audioController.playVoice(audioUrl) {
                onVoicePlaybackEnded(aiUid)
            }
        }

        // Progressive word-by-word streaming effect
        val words = fullMessage.split(" ")
        var accumulated = ""
        for (i in words.indices) {
            accumulated += (if (i == 0) "" else " ") + words[i]
            _uiState.update { state ->
                state.copy(
                    chats = state.chats.map { chat ->
                        if (chat.uid == aiUid && chat is Chat.Ai) {
                            chat.copy(message = accumulated)
                        } else chat
                    }.toImmutableList()
                )
            }
            delay(40)
        }
    }

    fun retrySendMessage() {
        val lastUserChat = _uiState.value.chats.lastOrNull { it is Chat.User } as? Chat.User ?: return
        _uiState.update { state ->
            state.copy(
                chats = state.chats.filter { it.uid != lastUserChat.uid }.toImmutableList(),
                messageText = lastUserChat.message
            )
        }
        sendMessage()
    }

    fun requestHint() {
        val stageId = _uiState.value.stage?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingHint = true, hintError = null) }
            val dialogue = _uiState.value.chats.mapNotNull {
                when (it) {
                    is Chat.User -> "User" to it.message
                    is Chat.Ai -> "Model" to it.message
                    else -> null
                }
            }
            when (val result = requestStageHintUseCase(stageId, dialogue, _uiState.value.userLevel)) {
                is DataResult.Success -> {
                    val newHintsCount = _uiState.value.hintsUsedCount + 1
                    _uiState.update {
                        it.copy(
                            isRequestingHint = false,
                            hintsUsedCount = newHintsCount,
                            currentHintSuggestion = result.data.suggestionEn,
                            currentHintExplanation = result.data.explanationFa,
                            hintError = null
                        )
                    }
                }
                is DataResult.Failure -> {
                    // Do not increment hintsUsedCount on network failure
                    val errorMsg = result.appError.getErrorMessage().ifBlank {
                        "خطا در دریافت راهنمایی. لطفاً اتصال اینترنت خود را بررسی کنید."
                    }
                    _uiState.update {
                        it.copy(
                            isRequestingHint = false,
                            currentHintSuggestion = null,
                            currentHintExplanation = null,
                            hintError = errorMsg
                        )
                    }
                }
            }
        }
    }

    fun dismissHint() {
        _uiState.update {
            it.copy(
                currentHintSuggestion = null,
                currentHintExplanation = null,
                hintError = null
            )
        }
    }

    fun submitEvaluation() {
        val stageId = _uiState.value.stage?.id ?: return
        if (_uiState.value.isSubmittingEvaluation) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingEvaluation = true) }
            val hasAiCompletionInChatList = _uiState.value.chats
                .filterIsInstance<Chat.Ai>()
                .any { it.objectiveCompleted }
            val isGoalDone = _uiState.value.isObjectiveCompleted || hasAiCompletionInChatList

            val dialogue = _uiState.value.chats.mapNotNull {
                when (it) {
                    is Chat.User -> "User" to it.message
                    is Chat.Ai -> "Model" to it.message
                    else -> null
                }
            }
            val hintsCount = _uiState.value.hintsUsedCount
            val turnsCount = _uiState.value.turnsCount

            val localGrammarErrors = _uiState.value.chats
                .filterIsInstance<Chat.User>()
                .mapNotNull { userChat ->
                    val status = userChat.status as? ChatStatus.Answered
                    if (status != null && (status.hasGrammarError || status.grammar.isNotBlank())) {
                        GrammarErrorDetail(
                            original = userChat.message,
                            correction = status.correctedSentence?.takeIf { it.isNotBlank() } ?: userChat.message,
                            explanationFa = status.grammar
                        )
                    } else null
                }
            val localGrammarErrorsCount = localGrammarErrors.size

            val session = when (val result = submitStageEvaluationUseCase(
                stageId = stageId,
                hintsUsedCount = hintsCount,
                turnsCount = turnsCount,
                transcript = dialogue,
                grammarErrorsCount = localGrammarErrorsCount,
                grammarErrors = localGrammarErrors,
                objectiveCompleted = isGoalDone
            )) {
                is DataResult.Success -> {
                    val serverSession = result.data
                    val finalGrammarErrorsCount = maxOf(serverSession.grammarErrorsCount, localGrammarErrorsCount)
                    val finalGrammarErrors = if (serverSession.grammarErrors.size >= localGrammarErrors.size && serverSession.grammarErrors.isNotEmpty()) {
                        serverSession.grammarErrors
                    } else {
                        localGrammarErrors
                    }

                    if (!isGoalDone || !serverSession.objectiveCompleted) {
                        serverSession.copy(
                            objectiveCompleted = false,
                            grammarErrorsCount = finalGrammarErrorsCount,
                            grammarErrors = finalGrammarErrors,
                            calculatedStars = 0,
                            score = 0
                        )
                    } else {
                        val totalPenalties = hintsCount + finalGrammarErrorsCount
                        val stars = when {
                            totalPenalties == 0 -> 3
                            totalPenalties == 1 -> 2
                            totalPenalties == 2 -> 1
                            else -> 0
                        }
                        val score = when (stars) {
                            3 -> 100
                            2 -> 85
                            1 -> 70
                            else -> 50
                        }
                        serverSession.copy(
                            objectiveCompleted = true,
                            grammarErrorsCount = finalGrammarErrorsCount,
                            grammarErrors = finalGrammarErrors,
                            calculatedStars = stars,
                            score = score
                        )
                    }
                }
                is DataResult.Failure -> {
                    if (!isGoalDone) {
                        EvaluationSession(
                            stageId = stageId,
                            hintsUsedCount = hintsCount,
                            grammarErrorsCount = localGrammarErrorsCount,
                            objectiveCompleted = false,
                            grammarErrors = localGrammarErrors,
                            calculatedStars = 0,
                            score = 0,
                            feedbackFa = "مکالمه پیش از رسیدن به هدف مرحله پایان یافت. برای دریافت ستاره و تکمیل مرحله، مکالمه را ادامه دهید."
                        )
                    } else {
                        val penalties = hintsCount + localGrammarErrorsCount
                        val stars = when (penalties) {
                            0 -> 3
                            1 -> 2
                            2 -> 1
                            else -> 0
                        }
                        EvaluationSession(
                            stageId = stageId,
                            hintsUsedCount = hintsCount,
                            grammarErrorsCount = localGrammarErrorsCount,
                            objectiveCompleted = true,
                            grammarErrors = localGrammarErrors,
                            calculatedStars = stars,
                            score = when (stars) {
                                3 -> 100
                                2 -> 85
                                1 -> 70
                                else -> 50
                            },
                            feedbackFa = "مکالمه به پایان رسید و پیشرفت شما ثبت شد."
                        )
                    }
                }
            }

            if (currentTier == AccessTier.GUEST && session.objectiveCompleted) {
                saveGuestProgress(stageId, session.calculatedStars, session.score)
            }

            _uiState.update {
                it.copy(
                    isSubmittingEvaluation = false,
                    evaluationSession = session,
                    showFinishConfirmDialog = true,
                    earnedStars = session.calculatedStars,
                    earnedScore = session.score
                )
            }
        }
    }

    private fun saveGuestProgress(stageId: String, stars: Int, score: Int) {
        viewModelScope.launch {
            val progress = LocalGuestProgress(
                stageId = stageId,
                stars = stars,
                score = score,
                completedAt = Clock.System.now().toString()
            )
            stageRepository.saveLocalGuestProgress(progress)
            _uiState.update {
                it.copy(
                    earnedStars = stars,
                    earnedScore = score
                )
            }
        }
    }

    fun dismissFinishConfirmDialog() {
        _uiState.update { it.copy(showFinishConfirmDialog = false) }
    }

    fun replayStage() {
        val stageId = _uiState.value.stage?.id ?: return
        _uiState.update {
            it.copy(
                showFinishConfirmDialog = false,
                evaluationSession = null,
                chats = persistentListOf(),
                isObjectiveCompleted = false,
                turnsCount = 0,
                hintsUsedCount = 0,
                earnedStars = 0,
                earnedScore = 0
            )
        }
        currentStageId = ""
        initStage(stageId)
    }

    override fun onCleared() {
        super.onCleared()
        sttStreamJob?.cancel()
        sttStreamJob = null
        viewModelScope.launch {
            try {
                closeSttSessionUseCase()
            } catch (_: Exception) {}
        }
        audioController.stopVoice()
        audioController.stopRecording()
    }
}
