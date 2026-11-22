package ir.aispeaking.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import ir.aispeaking.domain.usecase.leitner.*
import ir.aispeaking.sharedui.audio.DefaultStageAudioController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LeitnerViewModelAudioTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var audioController: DefaultStageAudioController
    private lateinit var fakeRepository: FakeLeitnerRepository
    private lateinit var viewModel: LeitnerViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        audioController = DefaultStageAudioController()
        fakeRepository = FakeLeitnerRepository()

        viewModel = LeitnerViewModel(
            getDueCardsUseCase = GetDueCardsUseCase(fakeRepository),
            getCardsByBoxUseCase = GetCardsByBoxUseCase(fakeRepository),
            getBoxCountsUseCase = GetBoxCountsUseCase(fakeRepository),
            submitCardReviewUseCase = SubmitCardReviewUseCase(fakeRepository),
            getReminderTimeUseCase = GetReminderTimeUseCase(fakeRepository),
            setReminderTimeUseCase = SetReminderTimeUseCase(fakeRepository),
            audioController = audioController
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPlayAudioWithExplicitAudioUrlInvokesAudioController() = runTest(testDispatcher) {
        var playedUrl: String? = null
        var onEndedCallback: (() -> Unit)? = null
        audioController.onPlatformPlayVoice = { url, onEnded ->
            playedUrl = url
            onEndedCallback = onEnded
        }

        val card = Flashcard(
            id = "c1",
            headword = "breakthrough",
            meaningFa = "پیشرفت چشمگیر",
            audioUrl = "/audio_cache/words/breakthrough.mp3"
        )

        viewModel.playAudio(card)

        assertTrue(viewModel.uiState.value.isAudioPlaying)
        assertEquals("c1", viewModel.uiState.value.audioPlayingCardId)
        assertEquals("/audio_cache/words/breakthrough.mp3", playedUrl)

        // Complete playback
        onEndedCallback?.invoke()

        assertFalse(viewModel.uiState.value.isAudioPlaying)
        assertNull(viewModel.uiState.value.audioPlayingCardId)
    }

    @Test
    fun testPlayAudioWithFallbackSynthesizesTtsUrl() = runTest(testDispatcher) {
        var playedUrl: String? = null
        audioController.onPlatformPlayVoice = { url, onEnded ->
            playedUrl = url
            onEnded()
        }

        val card = Flashcard(
            id = "c2",
            headword = "apple",
            meaningFa = "سیب",
            audioUrl = null,
            customAudioUrl = null
        )

        viewModel.playAudio(card)

        assertEquals("/api/v2/tts/speak?text=apple", playedUrl)
        assertFalse(viewModel.uiState.value.isAudioPlaying)
        assertNull(viewModel.uiState.value.audioPlayingCardId)
    }

    @Test
    fun testStopAudioClearsPlayingStateAndStopsVoice() = runTest(testDispatcher) {
        var stopVoiceCalled = false
        audioController.onPlatformPlayVoice = { _, _ -> }
        audioController.onPlatformStopVoice = {
            stopVoiceCalled = true
        }

        val card = Flashcard(
            id = "c3",
            headword = "persistent",
            meaningFa = "مداوم",
            audioUrl = "/audio_cache/words/persistent.mp3"
        )

        viewModel.playAudio(card)
        assertTrue(viewModel.uiState.value.isAudioPlaying)

        viewModel.stopAudio()

        assertTrue(stopVoiceCalled)
        assertFalse(viewModel.uiState.value.isAudioPlaying)
        assertNull(viewModel.uiState.value.audioPlayingCardId)
    }

    @Test
    fun testPlayDictionaryAudioStreamsCorrectUrl() = runTest(testDispatcher) {
        var playedUrl: String? = null
        var onEndedCallback: (() -> Unit)? = null
        audioController.onPlatformPlayVoice = { url, onEnded ->
            playedUrl = url
            onEndedCallback = onEnded
        }

        viewModel.playDictionaryAudio("/audio_cache/words/hello.mp3")

        assertTrue(viewModel.uiState.value.isDictionaryAudioPlaying)
        assertEquals("/audio_cache/words/hello.mp3", playedUrl)

        onEndedCallback?.invoke()

        assertFalse(viewModel.uiState.value.isDictionaryAudioPlaying)
    }
}

private class FakeLeitnerRepository : LeitnerRepository {
    override suspend fun getDueCards(limit: Int): DataResult<List<Flashcard>> = DataResult.Success(emptyList())
    override suspend fun getCardsByBox(boxNumber: Int?): DataResult<List<Flashcard>> = DataResult.Success(emptyList())
    override suspend fun getBoxCounts(): DataResult<Map<String, Long>> = DataResult.Success(emptyMap())
    override suspend fun addCard(
        lexiconId: String?,
        customWord: String?,
        customMeaning: String?,
        customExample: String?,
        customAudioUrl: String?
    ): DataResult<Flashcard> = DataResult.Success(
        Flashcard(
            id = "gen-1",
            headword = customWord.orEmpty(),
            meaningFa = customMeaning.orEmpty(),
            audioUrl = customAudioUrl
        )
    )
    override suspend fun reviewCard(cardId: String, remembered: Boolean): DataResult<Flashcard> =
        DataResult.Success(Flashcard(id = cardId))
    override suspend fun deleteCard(cardId: String): DataResult<Boolean> = DataResult.Success(true)
    override suspend fun getReminderTime(): DataResult<String?> = DataResult.Success("20:00")
    override suspend fun setReminderTime(time: String): DataResult<Boolean> = DataResult.Success(true)
}
