package ir.aispeaking.leitner.model

import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.model.leitner.LeitnerBox

sealed interface LeitnerTab {
    val title: String

    data object DueToday : LeitnerTab {
        override val title: String = "امروز برای مرور"
    }

    data object All : LeitnerTab {
        override val title: String = "همه"
    }

    data class BoxTab(val box: LeitnerBox) : LeitnerTab {
        override val title: String = box.titleFa
    }

    companion object {
        val allTabs: List<LeitnerTab> = listOf(
            DueToday,
            All,
            BoxTab(LeitnerBox.BOX_1),
            BoxTab(LeitnerBox.BOX_2),
            BoxTab(LeitnerBox.BOX_3),
            BoxTab(LeitnerBox.BOX_4),
            BoxTab(LeitnerBox.BOX_5)
        )
    }
}

data class LeitnerUiState(
    val isLoading: Boolean = false,
    val isGuest: Boolean = false,
    val selectedTab: LeitnerTab = LeitnerTab.DueToday,
    val allCards: List<Flashcard> = emptyList(),
    val dueCards: List<Flashcard> = emptyList(),
    val boxCounts: Map<Int, Long> = emptyMap(),
    val focusedCard: Flashcard? = null,
    val isReviewSessionOpen: Boolean = false,
    val isReminderSheetOpen: Boolean = false,
    val reminderTime: String? = null,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val isAudioPlaying: Boolean = false,
    val audioPlayingCardId: String? = null,
    val errorMessage: String? = null,
    // Review session progress
    val reviewQueue: List<Flashcard> = emptyList(),
    val currentReviewIndex: Int = 0,
    val promotedCardsCount: Int = 0,
    val demotedCardsCount: Int = 0,
    val isReviewSessionFinished: Boolean = false,
    // Add Word & Dictionary Lookup state
    val isAddWordDialogOpen: Boolean = false,
    val isDictionarySheetOpen: Boolean = false,
    val isDictionaryLoading: Boolean = false,
    val dictionaryTargetWord: String = "",
    val dictionaryEntry: DictionaryEntry? = null,
    val dictionaryError: String? = null,
    val isDictionaryAudioPlaying: Boolean = false,
    val isAddingCardToLeitner: Boolean = false,
    val isCardAddedToLeitner: Boolean = false
) {
    /**
     * Cards displayed in the current tab (sanitized against corrupt empty cards).
     */
    val displayCards: List<Flashcard>
        get() {
            val list = when (val tab = selectedTab) {
                is LeitnerTab.DueToday -> dueCards
                is LeitnerTab.All -> allCards
                is LeitnerTab.BoxTab -> allCards.filter { it.boxNumber == tab.box.boxNumber }
            }
            return list.filter { it.headword.isNotBlank() || !it.customWord.isNullOrBlank() }
        }

    val totalDueCount: Int
        get() = dueCards.count { it.headword.isNotBlank() || !it.customWord.isNullOrBlank() }
}
