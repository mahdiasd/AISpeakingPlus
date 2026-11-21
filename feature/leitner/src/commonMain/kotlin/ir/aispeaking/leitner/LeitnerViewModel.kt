package ir.aispeaking.leitner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.model.leitner.LeitnerBox
import ir.aispeaking.domain.usecase.dictionary.LookupWordUseCase
import ir.aispeaking.domain.usecase.leitner.AddCardToLeitnerUseCase
import ir.aispeaking.domain.usecase.leitner.GetBoxCountsUseCase
import ir.aispeaking.domain.usecase.leitner.GetCardsByBoxUseCase
import ir.aispeaking.domain.usecase.leitner.GetDueCardsUseCase
import ir.aispeaking.domain.usecase.leitner.GetReminderTimeUseCase
import ir.aispeaking.domain.usecase.leitner.SetReminderTimeUseCase
import ir.aispeaking.domain.usecase.leitner.SubmitCardReviewUseCase
import ir.aispeaking.leitner.model.LeitnerTab
import ir.aispeaking.leitner.model.LeitnerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class LeitnerViewModel(
    private val getDueCardsUseCase: GetDueCardsUseCase,
    private val getCardsByBoxUseCase: GetCardsByBoxUseCase,
    private val getBoxCountsUseCase: GetBoxCountsUseCase,
    private val submitCardReviewUseCase: SubmitCardReviewUseCase,
    private val getReminderTimeUseCase: GetReminderTimeUseCase,
    private val setReminderTimeUseCase: SetReminderTimeUseCase,
    private val lookupWordUseCase: LookupWordUseCase? = null,
    private val addCardToLeitnerUseCase: AddCardToLeitnerUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeitnerUiState(isLoading = true))
    val uiState: StateFlow<LeitnerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Fetch due cards
            val dueResult = getDueCardsUseCase(limit = 50)
            val dueList = when (dueResult) {
                is DataResult.Success -> dueResult.data
                is DataResult.Failure -> emptyList()
            }

            // 2. Fetch all cards
            val allResult = getCardsByBoxUseCase(boxNumber = null)
            val allList = when (allResult) {
                is DataResult.Success -> allResult.data
                is DataResult.Failure -> emptyList()
            }

            // 3. Fetch box counts
            val boxCountsResult = getBoxCountsUseCase()
            val boxCounts = when (boxCountsResult) {
                is DataResult.Success -> boxCountsResult.data
                is DataResult.Failure -> emptyMap()
            }

            // 4. Fetch reminder time
            val reminderResult = getReminderTimeUseCase()
            var rHour = 20
            var rMinute = 0
            val reminderTimeStr = when (reminderResult) {
                is DataResult.Success -> {
                    val raw = reminderResult.data
                    if (!raw.isNullOrBlank()) {
                        val parts = raw.split(":")
                        if (parts.size >= 2) {
                            rHour = parts[0].toIntOrNull() ?: 20
                            rMinute = parts[1].toIntOrNull() ?: 0
                        }
                    }
                    raw
                }
                is DataResult.Failure -> null
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    dueCards = dueList,
                    allCards = allList,
                    boxCounts = boxCounts,
                    reminderTime = reminderTimeStr,
                    reminderHour = rHour,
                    reminderMinute = rMinute,
                    errorMessage = if (dueResult is DataResult.Failure && allResult is DataResult.Failure) {
                        "خطا در دریافت کارت‌های لایتنر"
                    } else null
                )
            }
        }
    }

    fun selectTab(tab: LeitnerTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openFocusedCard(card: Flashcard) {
        _uiState.update { it.copy(focusedCard = card) }
    }

    fun closeFocusedCard() {
        _uiState.update { it.copy(focusedCard = null) }
    }

    fun startReviewSession() {
        val currentDue = _uiState.value.dueCards
        if (currentDue.isEmpty()) return

        _uiState.update {
            it.copy(
                isReviewSessionOpen = true,
                reviewQueue = currentDue,
                currentReviewIndex = 0,
                promotedCardsCount = 0,
                demotedCardsCount = 0,
                isReviewSessionFinished = false
            )
        }
    }

    fun dismissReviewSession() {
        _uiState.update {
            it.copy(
                isReviewSessionOpen = false,
                reviewQueue = emptyList(),
                currentReviewIndex = 0,
                isReviewSessionFinished = false
            )
        }
        // Refresh cards after session completes or closes
        loadData()
    }

    fun submitCardReview(card: Flashcard, remembered: Boolean) {
        viewModelScope.launch {
            // Optimistically update counts and advance
            val newPromoted = if (remembered) _uiState.value.promotedCardsCount + 1 else _uiState.value.promotedCardsCount
            val newDemoted = if (!remembered) _uiState.value.demotedCardsCount + 1 else _uiState.value.demotedCardsCount
            val nextIndex = _uiState.value.currentReviewIndex + 1
            val isFinished = nextIndex >= _uiState.value.reviewQueue.size

            _uiState.update {
                it.copy(
                    promotedCardsCount = newPromoted,
                    demotedCardsCount = newDemoted,
                    currentReviewIndex = nextIndex,
                    isReviewSessionFinished = isFinished
                )
            }

            // Call backend
            submitCardReviewUseCase(cardId = card.id, remembered = remembered)
        }
    }

    fun openReminderSheet() {
        _uiState.update { it.copy(isReminderSheetOpen = true) }
    }

    fun dismissReminderSheet() {
        _uiState.update { it.copy(isReminderSheetOpen = false) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        val formatted = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    reminderHour = hour,
                    reminderMinute = minute,
                    reminderTime = formatted,
                    isReminderSheetOpen = false
                )
            }
            setReminderTimeUseCase(time = formatted)
        }
    }

    fun playAudio(card: Flashcard) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAudioPlaying = true,
                    audioPlayingCardId = card.id
                )
            }
            // Reset state after simulated or actual playback duration
            kotlinx.coroutines.delay(1200)
            _uiState.update {
                it.copy(
                    isAudioPlaying = false,
                    audioPlayingCardId = null
                )
            }
        }
    }

    /* -------------------------------- Add Word & Dictionary Integration -------------------------------- */

    fun openAddWordDialog() {
        _uiState.update { it.copy(isAddWordDialogOpen = true) }
    }

    fun closeAddWordDialog() {
        _uiState.update { it.copy(isAddWordDialogOpen = false) }
    }

    fun searchAndOpenDictionary(word: String) {
        val trimmed = word.trim()
        if (trimmed.isBlank()) return

        _uiState.update {
            it.copy(
                isAddWordDialogOpen = false,
                isDictionarySheetOpen = true,
                isDictionaryLoading = true,
                dictionaryTargetWord = trimmed,
                dictionaryEntry = null,
                dictionaryError = null,
                isCardAddedToLeitner = false,
                isAddingCardToLeitner = false
            )
        }

        viewModelScope.launch {
            if (lookupWordUseCase == null) {
                _uiState.update {
                    it.copy(
                        isDictionaryLoading = false,
                        dictionaryError = "سرویس دیکشنری در دسترس نیست"
                    )
                }
                return@launch
            }

            when (val result = lookupWordUseCase(trimmed)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isDictionaryLoading = false,
                            dictionaryEntry = result.data,
                            dictionaryError = null
                        )
                    }
                }
                is DataResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isDictionaryLoading = false,
                            dictionaryError = "معنای کلمه «$trimmed» در دیکشنری یافت نشد."
                        )
                    }
                }
            }
        }
    }

    fun retryDictionaryLookup() {
        searchAndOpenDictionary(_uiState.value.dictionaryTargetWord)
    }

    fun closeDictionarySheet() {
        _uiState.update {
            it.copy(
                isDictionarySheetOpen = false,
                isDictionaryAudioPlaying = false
            )
        }
    }

    fun addCardFromDictionary(entry: DictionaryEntry) {
        if (_uiState.value.isCardAddedToLeitner || _uiState.value.isAddingCardToLeitner) return

        _uiState.update { it.copy(isAddingCardToLeitner = true) }

        viewModelScope.launch {
            if (addCardToLeitnerUseCase == null) {
                _uiState.update { it.copy(isAddingCardToLeitner = false) }
                return@launch
            }

            when (val result = addCardToLeitnerUseCase(entry)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAddingCardToLeitner = false,
                            isCardAddedToLeitner = true
                        )
                    }
                    // Immediately refresh the Leitner deck cards
                    loadData()
                }
                is DataResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isAddingCardToLeitner = false
                        )
                    }
                }
            }
        }
    }

    fun playDictionaryAudio(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDictionaryAudioPlaying = true) }
            kotlinx.coroutines.delay(1200)
            _uiState.update { it.copy(isDictionaryAudioPlaying = false) }
        }
    }

    fun stopDictionaryAudio() {
        _uiState.update { it.copy(isDictionaryAudioPlaying = false) }
    }
}
