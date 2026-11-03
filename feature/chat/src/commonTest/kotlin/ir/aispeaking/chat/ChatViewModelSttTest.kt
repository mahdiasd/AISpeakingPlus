package ir.aispeaking.chat

import ir.aispeaking.chat.audio.DefaultStageAudioController
import ir.aispeaking.domain.model.chat.StageChatTurnResult
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.model.stage.GrammarErrorDetail
import ir.aispeaking.domain.model.stage.HintSuggestion
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stt.SttStreamEvent
import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.domain.repository.auth.AuthRepository
import ir.aispeaking.domain.repository.stage.StageRepository
import ir.aispeaking.domain.repository.stt.SttRepository
import ir.aispeaking.domain.repository.user.UserProfileRepository
import ir.aispeaking.domain.usecase.auth.GetCurrentAccessTierUseCase
import ir.aispeaking.domain.usecase.stage.GetStageDetailUseCase
import ir.aispeaking.domain.usecase.stage.RequestStageHintUseCase
import ir.aispeaking.domain.usecase.stage.SubmitStageEvaluationUseCase
import ir.aispeaking.domain.usecase.stt.CloseSttSessionUseCase
import ir.aispeaking.domain.usecase.stt.SendAudioChunkUseCase
import ir.aispeaking.domain.usecase.stt.SendStopSttUseCase
import ir.aispeaking.domain.usecase.stt.StreamSpeechToTextUseCase
import ir.aispeaking.domain.usecase.user.GetUserProfileUseCase
import ir.aispeaking.utils.analytics.AnalyticsTracker
import ir.aispeaking.utils.crashlytics.DefaultCrashReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelSttTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSttRepo : SttRepository {
        val streamFlow = MutableSharedFlow<SttStreamEvent>(extraBufferCapacity = 10)
        val sentChunks = mutableListOf<ByteArray>()
        var closed = false

        override fun connectSttStream(): Flow<SttStreamEvent> = streamFlow

        override suspend fun sendAudioChunk(pcm16Bytes: ByteArray) {
            sentChunks.add(pcm16Bytes)
        }

        override suspend fun sendStop() {}

        override suspend fun closeSession() {
            closed = true
        }

        override fun isSessionActive(): Boolean = !closed
    }

    private class FakeStageRepo : StageRepository {
        var controllableStreamFlow: MutableSharedFlow<ir.aispeaking.domain.model.chat.StageChatStreamEvent>? = null

        override fun streamStageChatMessage(
            stageId: String,
            userMessage: String?,
            history: List<Pair<String, String>>,
            userLevel: String?
        ): Flow<ir.aispeaking.domain.model.chat.StageChatStreamEvent> {
            val custom = controllableStreamFlow
            if (custom != null) return custom
            return super.streamStageChatMessage(stageId, userMessage, history, userLevel)
        }

        override suspend fun getStages(tier: AccessTier): DataResult<List<Stage>> = DataResult.Success(emptyList())

        override suspend fun getStageDetail(stageId: String, tier: AccessTier): DataResult<Stage> =
            DataResult.Success(
                Stage(
                    id = stageId,
                    orderIndex = 1,
                    title = "Test",
                    titleFa = "تست",
                    briefing = "Briefing",
                    briefingFa = "شرح",
                    targetObjective = "Objective",
                    targetObjectiveFa = "هدف",
                    backgroundUrl = "/bg.webp",
                    characterName = "Sarah",
                    initialSpeaker = "User"
                )
            )

        override suspend fun sendStageChatMessage(
            stageId: String,
            userMessage: String?,
            history: List<Pair<String, String>>,
            userLevel: String?
        ): DataResult<StageChatTurnResult> =
            DataResult.Success(
                StageChatTurnResult(
                    message = "Model reply",
                    translatedMessage = "پاسخ مدل",
                    audioUrl = null,
                    hasGrammarError = false,
                    correctedSentence = null,
                    grammarFeedbackFa = "",
                    objectiveCompleted = false
                )
            )

        override suspend fun requestHint(
            stageId: String,
            messages: List<Pair<String, String>>,
            userLevel: String?
        ): DataResult<HintSuggestion> =
            DataResult.Success(HintSuggestion(suggestionEn = "suggestion", explanationFa = "توضیح"))

        override suspend fun submitEvaluation(
            stageId: String,
            hintsUsedCount: Int,
            turnsCount: Int,
            transcript: List<Pair<String, String>>,
            grammarErrorsCount: Int,
            grammarErrors: List<GrammarErrorDetail>,
            objectiveCompleted: Boolean
        ): DataResult<EvaluationSession> =
            DataResult.Success(
                EvaluationSession(
                    stageId = stageId,
                    hintsUsedCount = hintsUsedCount,
                    grammarErrorsCount = grammarErrorsCount,
                    objectiveCompleted = objectiveCompleted,
                    grammarErrors = grammarErrors,
                    calculatedStars = 3,
                    score = 100,
                    feedbackFa = "عالی"
                )
            )

        override suspend fun getLocalGuestProgress(): DataResult<List<LocalGuestProgress>> = DataResult.Success(emptyList())
        override suspend fun saveLocalGuestProgress(progress: LocalGuestProgress): DataResult<Unit> = DataResult.Success(Unit)
        override suspend fun clearLocalGuestProgress(): DataResult<Unit> = DataResult.Success(Unit)
    }

    private class FakeAuthRepo : AuthRepository {
        override suspend fun getToken(): String? = null
        override suspend fun saveToken(token: String) {}
        override suspend fun isLoggedIn(): Boolean = false
        override suspend fun getCurrentUser(): User? = null
        override suspend fun saveUser(user: User) {}
        override suspend fun getCurrentAccessTier(): AccessTier = AccessTier.GUEST
        override suspend fun logout(): DataResult<Unit> = DataResult.Success(Unit)
        override suspend fun sendOtp(mobile: String): DataResult<Int> = DataResult.Success(1234)
        override suspend fun verifyOtp(mobile: String, otpCode: String): DataResult<User> = DataResult.Success(User(uid = "1", nickName = "Learner", mobile = "09120000000"))
        override suspend fun fetchCurrentUserRemote(): DataResult<User> = DataResult.Success(User(uid = "1", nickName = "Learner", mobile = "09120000000"))
        override fun hasSeenIntroVideo(): Boolean = true
        override fun setHasSeenIntroVideo(seen: Boolean) {}
    }

    private class FakeUserProfileRepo : UserProfileRepository {
        override suspend fun getUserProfile(): DataResult<UserProfile> =
            DataResult.Success(UserProfile(id = "u1", phoneNumber = "09120000000", nickName = "Learner", languageLevel = "A1"))
        override suspend fun updateProfile(input: UpdateProfileInput): DataResult<UserProfile> = getUserProfile()
        override suspend fun getGuestProfile(): UserProfile =
            UserProfile(id = "guest", phoneNumber = "", nickName = "Guest", languageLevel = "A1", isGuest = true)
    }

    private class FakeAnalyticsTracker : AnalyticsTracker {
        val loggedEvents = mutableListOf<Pair<String, Map<String, Any?>>>()
        override fun logEvent(name: String, params: Map<String, Any?>) {
            loggedEvents.add(name to params)
        }
        override fun setUserId(userId: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private lateinit var sttRepo: FakeSttRepo
    private lateinit var stageRepo: FakeStageRepo
    private lateinit var audioController: DefaultStageAudioController
    private lateinit var analyticsTracker: FakeAnalyticsTracker
    private lateinit var viewModel: ChatViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sttRepo = FakeSttRepo()
        stageRepo = FakeStageRepo()
        audioController = DefaultStageAudioController()
        analyticsTracker = FakeAnalyticsTracker()
        val authRepo = FakeAuthRepo()
        val userProfileRepo = FakeUserProfileRepo()

        viewModel = ChatViewModel(
            getStageDetailUseCase = GetStageDetailUseCase(stageRepo),
            getCurrentAccessTierUseCase = GetCurrentAccessTierUseCase(authRepo),
            requestStageHintUseCase = RequestStageHintUseCase(stageRepo),
            submitStageEvaluationUseCase = SubmitStageEvaluationUseCase(stageRepo),
            stageRepository = stageRepo,
            audioController = audioController,
            getUserProfileUseCase = GetUserProfileUseCase(userProfileRepo),
            streamSpeechToTextUseCase = StreamSpeechToTextUseCase(sttRepo),
            sendAudioChunkUseCase = SendAudioChunkUseCase(sttRepo),
            closeSttSessionUseCase = CloseSttSessionUseCase(sttRepo),
            sendStopSttUseCase = SendStopSttUseCase(sttRepo),
            analyticsTracker = analyticsTracker,
            crashReporter = DefaultCrashReporter()
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testToggleRecordingStreamsAudioChunksViaUseCase() = runTest {
        viewModel.toggleRecording(true)
        runCurrent()

        assertTrue(viewModel.uiState.value.isRecording)
        assertNotNull(audioController.onAudioChunkCaptured)

        // Capture a 2048-byte PCM chunk
        val sampleChunk = ByteArray(2048) { it.toByte() }
        audioController.onAudioChunkCaptured?.invoke(sampleChunk)
        runCurrent()

        // Verify sendAudioChunkUseCase sent the chunk to the repository!
        assertEquals(1, sttRepo.sentChunks.size)
        assertEquals(2048, sttRepo.sentChunks[0].size)
    }

    @Test
    fun testRealtimeTranscriptionUpdatesMessageText() = runTest {
        viewModel.toggleRecording(true)
        runCurrent()

        sttRepo.streamFlow.emit(SttStreamEvent.PartialTranscript("Hello"))
        runCurrent()
        assertEquals("Hello", viewModel.uiState.value.messageText)

        sttRepo.streamFlow.emit(SttStreamEvent.PartialTranscript("Hello world"))
        runCurrent()
        assertEquals("Hello world", viewModel.uiState.value.messageText)

        sttRepo.streamFlow.emit(SttStreamEvent.FinalTranscript("Hello world."))
        runCurrent()
        assertEquals("Hello world.", viewModel.uiState.value.messageText)
    }

    @Test
    fun testSttErrorStopsRecordingAndSetsErrorState() = runTest {
        viewModel.toggleRecording(true)
        runCurrent()

        sttRepo.streamFlow.emit(SttStreamEvent.Error("server_busy"))
        runCurrent()

        assertFalse(viewModel.uiState.value.isRecording)
        assertEquals("server_busy", viewModel.uiState.value.sttError)
        assertNull(audioController.onAudioChunkCaptured)
    }

    @Test
    fun testStoppingRecordingCleansUpAndNullsCallback() = runTest {
        viewModel.toggleRecording(true)
        runCurrent()
        assertTrue(viewModel.uiState.value.isRecording)

        viewModel.toggleRecording(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRecording)
        assertNull(audioController.onAudioChunkCaptured)
        assertTrue(sttRepo.closed)
    }

    @Test
    fun testSilenceAutoPausesRecordingAfterFiveSeconds() = runTest {
        viewModel.toggleRecording(true)
        runCurrent()
        assertTrue(viewModel.uiState.value.isRecording)
        assertNotNull(audioController.onAudioChunkCaptured)

        // Advance beyond 5 seconds silence threshold
        advanceTimeBy(5100)
        runCurrent()

        assertFalse(viewModel.uiState.value.isRecording)
        assertNull(audioController.onAudioChunkCaptured)
    }

    @Test
    fun testManualSubmitEvaluationShowsDialogImmediatelyAndStopsVoice() = runTest {
        viewModel.initStage("stage-1")
        advanceUntilIdle()

        // Start playing voice
        audioController.playVoice("http://example.com/audio.wav") {}
        assertTrue(audioController.isVoicePlaying())

        // User manually triggers finish conversation
        viewModel.submitEvaluation()
        advanceUntilIdle()

        // Voice must be stopped immediately and finish dialog shown
        assertFalse(audioController.isVoicePlaying(), "Voice must stop when user manually finishes conversation")
        assertTrue(viewModel.uiState.value.showFinishConfirmDialog, "Finish dialog must be shown immediately on manual submit")
    }

    @Test
    fun testCheckMicrophonePermissionShowsDialogWhenNotGranted() = runTest {
        audioController.onPlatformCheckMicrophonePermission = { callback ->
            callback(false)
        }
        viewModel.checkMicrophonePermission()
        runCurrent()

        assertTrue(viewModel.uiState.value.showMicPermissionDialog)
        assertFalse(viewModel.uiState.value.isMicPermissionGranted)
    }

    @Test
    fun testCheckMicrophonePermissionHidesDialogWhenGranted() = runTest {
        audioController.onPlatformCheckMicrophonePermission = { callback ->
            callback(true)
        }
        viewModel.checkMicrophonePermission()
        runCurrent()

        assertFalse(viewModel.uiState.value.showMicPermissionDialog)
        assertTrue(viewModel.uiState.value.isMicPermissionGranted)
    }

    @Test
    fun testRequestMicrophonePermissionDismissesDialogWhenUserAllows() = runTest {
        audioController.onPlatformRequestMicrophonePermission = { callback ->
            callback(true)
        }
        viewModel.requestMicrophonePermission()
        runCurrent()

        assertFalse(viewModel.uiState.value.showMicPermissionDialog)
        assertTrue(viewModel.uiState.value.isMicPermissionGranted)
        assertFalse(viewModel.uiState.value.isMicPermissionPermanentlyDenied)
    }

    @Test
    fun testRequestMicrophonePermissionSetsPermanentlyDeniedWhenUserRejects() = runTest {
        audioController.onPlatformRequestMicrophonePermission = { callback ->
            callback(false)
        }
        viewModel.requestMicrophonePermission()
        runCurrent()

        assertTrue(viewModel.uiState.value.showMicPermissionDialog)
        assertFalse(viewModel.uiState.value.isMicPermissionGranted)
        assertTrue(viewModel.uiState.value.isMicPermissionPermanentlyDenied)
    }

    @Test
    fun testToggleRecordingBlocksWhenPermissionNotGrantedAndShowsDialog() = runTest {
        audioController.onPlatformCheckMicrophonePermission = { callback ->
            callback(false)
        }
        viewModel.toggleRecording(true)
        runCurrent()

        assertFalse(viewModel.uiState.value.isRecording, "Recording must not start without permission")
        assertTrue(viewModel.uiState.value.showMicPermissionDialog, "Permission dialog must be shown")
    }

    @Test
    fun testWhenNativeSpeechRecognitionAvailableDoesNotStreamToBackend() = runTest {
        audioController.onPlatformHasNativeSpeechRecognition = { true }
        var speechCallback: ((String) -> Unit)? = null
        audioController.onPlatformStartRecording = { onSpeech, _ ->
            speechCallback = onSpeech
        }

        viewModel.toggleRecording(true)
        runCurrent()

        assertTrue(viewModel.uiState.value.isRecording)
        // audioChunkCaptured callback must NOT be set when native STT is active
        assertNull(audioController.onAudioChunkCaptured)
        // No chunks sent to backend WebSocket
        assertTrue(sttRepo.sentChunks.isEmpty())

        // When native speech recognition delivers text
        speechCallback?.invoke("Hello from native speech")
        runCurrent()

        assertEquals("Hello from native speech", viewModel.uiState.value.messageText)
    }

    @Test
    fun testCannotRecordVoiceOrTypeWhileModelSpeaking() = runTest {
        viewModel.initStage("stage-1")
        advanceUntilIdle()

        val aiStream = MutableSharedFlow<ir.aispeaking.domain.model.chat.StageChatStreamEvent>(extraBufferCapacity = 10)
        stageRepo.controllableStreamFlow = aiStream

        // Set message text and send to put model in speaking state
        viewModel.onMessageTextChanged("Hello AI")
        viewModel.sendMessage()
        runCurrent()

        aiStream.emit(ir.aispeaking.domain.model.chat.StageChatStreamEvent.Chunk("Thinking..."))
        runCurrent()

        assertTrue(viewModel.uiState.value.isModelSpeaking, "Model must be in speaking/streaming state")

        // Attempting to record voice while model is speaking must be blocked
        viewModel.toggleRecording(true)
        runCurrent()
        assertFalse(viewModel.uiState.value.isRecording, "User must NOT be able to record voice while AI is speaking")

        // Attempting to type while model is speaking must be blocked
        viewModel.onMessageTextChanged("Trying to type")
        runCurrent()
        assertEquals("", viewModel.uiState.value.messageText, "Typing must be blocked while AI is speaking")
    }

    @Test
    fun testSttTranscriptDoesNotReappearAfterMessageSent() = runTest {
        viewModel.initStage("stage-1")
        advanceUntilIdle()

        val aiStream = MutableSharedFlow<ir.aispeaking.domain.model.chat.StageChatStreamEvent>(extraBufferCapacity = 10)
        stageRepo.controllableStreamFlow = aiStream

        // 1. User records first voice
        viewModel.toggleRecording(true)
        runCurrent()
        assertTrue(viewModel.uiState.value.isRecording)

        // 2. STT transcribes speech
        sttRepo.streamFlow.emit(SttStreamEvent.PartialTranscript("My first voice message"))
        runCurrent()
        assertEquals("My first voice message", viewModel.uiState.value.messageText)

        // 3. User sends the message
        viewModel.sendMessage()
        runCurrent()

        // Message text must be cleared and recording stopped
        assertEquals("", viewModel.uiState.value.messageText)
        assertFalse(viewModel.uiState.value.isRecording)
        assertTrue(viewModel.uiState.value.isModelSpeaking)

        // 4. Any late incoming STT event from backend MUST NOT repopulate messageText
        sttRepo.streamFlow.emit(SttStreamEvent.FinalTranscript("My first voice message"))
        runCurrent()

        assertEquals("", viewModel.uiState.value.messageText, "STT transcript must NOT reappear after sending message")

        // 5. Complete AI stream
        aiStream.emit(ir.aispeaking.domain.model.chat.StageChatStreamEvent.Done(
            StageChatTurnResult("Reply", "پاسخ", null, false, null, "", false)
        ))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isModelSpeaking)
        assertEquals("", viewModel.uiState.value.messageText)
    }

    @Test
    fun testSttRecordingStartedLogsWebsocketWhenNoNativeSpeechRecognition() = runTest {
        audioController.onPlatformHasNativeSpeechRecognition = { false }

        viewModel.toggleRecording(true)
        runCurrent()

        val event = analyticsTracker.loggedEvents.find { it.first == ir.aispeaking.utils.analytics.AnalyticsEvents.EVENT_STT_RECORDING_STARTED }
        assertNotNull(event, "EVENT_STT_RECORDING_STARTED should be logged")
        assertEquals(
            ir.aispeaking.utils.analytics.AnalyticsEvents.STT_ENGINE_WEBSOCKET,
            event.second[ir.aispeaking.utils.analytics.AnalyticsEvents.PARAM_STT_ENGINE]
        )
    }

    @Test
    fun testSttRecordingStartedLogsNativeWhenHasNativeSpeechRecognition() = runTest {
        audioController.onPlatformHasNativeSpeechRecognition = { true }

        viewModel.toggleRecording(true)
        runCurrent()

        val event = analyticsTracker.loggedEvents.find { it.first == ir.aispeaking.utils.analytics.AnalyticsEvents.EVENT_STT_RECORDING_STARTED }
        assertNotNull(event, "EVENT_STT_RECORDING_STARTED should be logged")
        assertEquals(
            ir.aispeaking.utils.analytics.AnalyticsEvents.STT_ENGINE_NATIVE,
            event.second[ir.aispeaking.utils.analytics.AnalyticsEvents.PARAM_STT_ENGINE]
        )
    }

    @Test
    fun testSendMessageIncludesSttEngineInAnalytics() = runTest {
        audioController.onPlatformHasNativeSpeechRecognition = { true }
        viewModel.onMessageTextChanged("Hello there")

        viewModel.sendMessage()
        runCurrent()

        val event = analyticsTracker.loggedEvents.find { it.first == ir.aispeaking.utils.analytics.AnalyticsEvents.EVENT_CHAT_MESSAGE_SENT }
        assertNotNull(event, "EVENT_CHAT_MESSAGE_SENT should be logged")
        assertEquals(
            ir.aispeaking.utils.analytics.AnalyticsEvents.STT_ENGINE_NATIVE,
            event.second[ir.aispeaking.utils.analytics.AnalyticsEvents.PARAM_STT_ENGINE]
        )
    }

    @Test
    fun testRecordingStartsInPreparingStateAndTransitionsOnReady() = runTest {
        viewModel.toggleRecording(true)
        runCurrent()

        assertTrue(viewModel.uiState.value.isRecording, "Recording should be active")
        assertTrue(viewModel.uiState.value.isRecordingPreparing, "Should start in preparing state")

        // Trigger recording ready
        audioController.notifyRecordingReady()
        runCurrent()

        assertTrue(viewModel.uiState.value.isRecording, "Recording should remain active")
        assertFalse(viewModel.uiState.value.isRecordingPreparing, "Should exit preparing state once ready")

        // Stop recording
        viewModel.toggleRecording(false)
        runCurrent()

        assertFalse(viewModel.uiState.value.isRecording)
        assertFalse(viewModel.uiState.value.isRecordingPreparing)
    }
}
