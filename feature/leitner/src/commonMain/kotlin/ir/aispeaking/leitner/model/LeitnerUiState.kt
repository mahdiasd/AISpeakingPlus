package ir.aispeaking.leitner.model

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
    val isReviewSessionFinished: Boolean = false
) {
    /**
     * Cards displayed in the current tab.
     */
    val displayCards: List<Flashcard>
        get() = when (val tab = selectedTab) {
            is LeitnerTab.DueToday -> dueCards
            is LeitnerTab.All -> allCards
            is LeitnerTab.BoxTab -> allCards.filter { it.boxNumber == tab.box.boxNumber }
        }

    val totalDueCount: Int
        get() = dueCards.size
}
