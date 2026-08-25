package ir.aispeaking.englishLevel

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.englishLevel.model.Question
import ir.aispeaking.englishLevel.utils.QuestionUtils
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.uuid.Uuid

@Stable
data class EnglishLevelUiState(
    val isBtnLoading: Boolean = false,
    val questions: ImmutableList<Question> = QuestionUtils.getBalancedQuestionsPerWeight().toImmutableList(),
    val userAnswers: Map<Uuid, Int> = emptyMap(),
    val languageLevel: LanguageLevel? = null,
    val isShowResultDialog: Boolean = true,
) : UiState


sealed class EnglishLevelUiEvent : UiEvent {
    data object OnBackBtnClick : EnglishLevelUiEvent()
    data object OnCalculateLevelBtnClick : EnglishLevelUiEvent()
    data object OnShowAnswersBtnClick : EnglishLevelUiEvent()
    data object OnContinueRegisterBtnClick : EnglishLevelUiEvent()
    data class OnAnswer(val question: Question, val optionIndex: Int) : EnglishLevelUiEvent()
}

sealed class EnglishLevelUiNavigation : UiNavigation {
    data object ToBack : EnglishLevelUiNavigation()
    data class NavigateToRegister(val languageLevel: LanguageLevel, val mobile: String) :
        EnglishLevelUiNavigation()
}

typealias OnAction = (EnglishLevelUiEvent) -> Unit
