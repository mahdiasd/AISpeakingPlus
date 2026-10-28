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
        override fun logEvent(name: String, params: Map<String, Any?>) {}
        override fun setUserId(userId: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private lateinit var sttRepo: FakeSttRepo
    private lateinit var audioController: DefaultStageAudioController
    private lateinit var viewModel: ChatViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sttRepo = FakeSttRepo()
        audioController = DefaultStageAudioController()
        val stageRepo = FakeStageRepo()
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
            analyticsTracker = FakeAnalyticsTracker(),
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
}
