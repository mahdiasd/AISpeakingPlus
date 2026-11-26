package ir.aispeaking.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.repository.dictionary.DictionaryRepository
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import ir.aispeaking.domain.usecase.dictionary.LookupWordUseCase
import ir.aispeaking.domain.usecase.leitner.DeleteCardFromLeitnerUseCase
import ir.aispeaking.domain.usecase.leitner.GetBoxCountsUseCase
import ir.aispeaking.domain.usecase.leitner.GetCardsByBoxUseCase
import ir.aispeaking.domain.usecase.leitner.GetDueCardsUseCase
import ir.aispeaking.domain.usecase.leitner.GetReminderTimeUseCase
import ir.aispeaking.domain.usecase.leitner.SetReminderTimeUseCase
import ir.aispeaking.domain.usecase.leitner.SubmitCardReviewUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class LeitnerViewModelActionsTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun confirmDeleteCardRemovesCardUpdatesBoxCountsAndClosesFocusedCard() = runTest(testDispatcher) {
        val card1 = Flashcard(id = "c1", headword = "resilient", meaningFa = "تاب‌آور", boxNumber = 1)
        val card2 = Flashcard(id = "c2", headword = "ephemeral", meaningFa = "زودگذر", boxNumber = 2)
        val repo = InMemoryLeitnerRepository(mutableListOf(card1, card2))

        val viewModel = LeitnerViewModel(
            getDueCardsUseCase = GetDueCardsUseCase(repo),
            getCardsByBoxUseCase = GetCardsByBoxUseCase(repo),
            getBoxCountsUseCase = GetBoxCountsUseCase(repo),
            submitCardReviewUseCase = SubmitCardReviewUseCase(repo),
            getReminderTimeUseCase = GetReminderTimeUseCase(repo),
            setReminderTimeUseCase = SetReminderTimeUseCase(repo),
            deleteCardFromLeitnerUseCase = DeleteCardFromLeitnerUseCase(repo)
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.allCards.size)
        viewModel.openFocusedCard(card1)
        viewModel.requestDeleteCard(card1)
        assertEquals(card1, viewModel.uiState.value.cardPendingDelete)

        viewModel.confirmDeleteCard()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.cardPendingDelete)
        assertNull(viewModel.uiState.value.focusedCard)
        assertFalse(viewModel.uiState.value.isDeletingCard)
        assertEquals(listOf(card2), viewModel.uiState.value.allCards)
        assertEquals(listOf(card2), viewModel.uiState.value.dueCards)
        assertEquals(0L, viewModel.uiState.value.boxCounts[1])
    }

    @Test
    fun searchAndOpenDictionaryMarksBadRequestAsInvalidWordError() = runTest(testDispatcher) {
        val repo = InMemoryLeitnerRepository(mutableListOf())
        val dictRepo = object : DictionaryRepository {
            override suspend fun lookupWord(word: String, sentenceContext: String?): DataResult<DictionaryEntry> {
                return DataResult.Failure(
                    NetworkError.BadRequest(message = "عبارت «$word» یک کلمه معتبر انگلیسی نیست.")
                )
            }
        }

        val viewModel = LeitnerViewModel(
            getDueCardsUseCase = GetDueCardsUseCase(repo),
            getCardsByBoxUseCase = GetCardsByBoxUseCase(repo),
            getBoxCountsUseCase = GetBoxCountsUseCase(repo),
            submitCardReviewUseCase = SubmitCardReviewUseCase(repo),
            getReminderTimeUseCase = GetReminderTimeUseCase(repo),
            setReminderTimeUseCase = SetReminderTimeUseCase(repo),
            lookupWordUseCase = LookupWordUseCase(dictRepo)
        )
        advanceUntilIdle()

        viewModel.searchAndOpenDictionary("abbas bo azar")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDictionarySheetOpen)
        assertTrue(viewModel.uiState.value.isDictionaryInvalidWord)
        assertEquals("عبارت «abbas bo azar» یک کلمه معتبر انگلیسی نیست.", viewModel.uiState.value.dictionaryError)
    }

    private class InMemoryLeitnerRepository(
        private val cards: MutableList<Flashcard>
    ) : LeitnerRepository {
        override suspend fun getDueCards(limit: Int): DataResult<List<Flashcard>> = DataResult.Success(cards.toList())
        override suspend fun getCardsByBox(boxNumber: Int): DataResult<List<Flashcard>> =
            DataResult.Success(cards.filter { it.boxNumber == boxNumber })
        override suspend fun getAllCards(): DataResult<List<Flashcard>> = DataResult.Success(cards.toList())
        override suspend fun getBoxCounts(): DataResult<Map<Int, Long>> =
            DataResult.Success((1..5).associateWith { box -> cards.count { it.boxNumber == box }.toLong() })
        override suspend fun addCard(
            lexiconId: String?,
            customWord: String?,
            customMeaning: String?,
            customExample: String?,
            customAudioUrl: String?
        ): DataResult<Flashcard> = DataResult.Success(Flashcard(id = "new"))
        override suspend fun submitCardReview(cardId: String, remembered: Boolean): DataResult<Flashcard> =
            DataResult.Success(Flashcard(id = cardId))
        override suspend fun deleteCard(cardId: String): DataResult<Boolean> {
            val removed = cards.removeAll { it.id == cardId }
            return DataResult.Success(removed)
        }
        override suspend fun getReminderTime(): DataResult<String?> = DataResult.Success("20:00")
        override suspend fun setReminderTime(time: String): DataResult<Boolean> = DataResult.Success(true)
    }
}
