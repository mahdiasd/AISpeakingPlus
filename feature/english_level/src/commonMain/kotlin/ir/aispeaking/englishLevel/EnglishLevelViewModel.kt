package ir.aispeaking.englishLevel

import ir.aispeaking.englishLevel.utils.QuestionUtils
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.english_level_error_answer_all_questions
import ir.aispeaking.sharedui.ui.model.ui_message.MessageStatus
import ir.aispeaking.sharedui.ui.model.ui_message.MessageType
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import kotlinx.collections.immutable.toImmutableMap
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class EnglishLevelViewModel(
    private val mobile: String,
) : BaseViewModel<EnglishLevelUiState, EnglishLevelUiEvent>() {

    init {
        // The mobile number is passed from the Auth screen via the route argument.
        // If it is missing the user can still take the exam; the register screen
        // will re-read it from its own route argument.
    }

    override fun createInitialState() = EnglishLevelUiState()

    override fun onTriggerEvent(event: EnglishLevelUiEvent) {
        when (event) {
            is EnglishLevelUiEvent.OnAnswer -> {
                val newList = currentState.userAnswers.toMutableMap()
                    .apply { put(event.question.id, event.optionIndex) }
                    .toImmutableMap()
                setState { copy(userAnswers = newList) }
            }

            is EnglishLevelUiEvent.OnBackBtnClick -> {
                setUiNavigation(EnglishLevelUiNavigation.ToBack)
            }

            is EnglishLevelUiEvent.OnCalculateLevelBtnClick -> {
                if (currentState.userAnswers.size != currentState.questions.size) {
                    setUiMessage(
                        UiMessage(
                            intValue = Res.string.english_level_error_answer_all_questions,
                            messageType = MessageType.Device, status = MessageStatus.Failure
                        )
                    )
                } else {
                    calculateLevel()
                }
            }

            EnglishLevelUiEvent.OnContinueRegisterBtnClick -> {
                setState { copy(isShowResultDialog = false) }
                setUiNavigation(
                    EnglishLevelUiNavigation.NavigateToRegister(
                        languageLevel = currentState.languageLevel!!,
                        mobile = mobile
                    )
                )
            }

            EnglishLevelUiEvent.OnShowAnswersBtnClick -> {
                setState { copy(isShowResultDialog = false) }
            }
        }
    }

    private fun calculateLevel() {
        val languageLevel = QuestionUtils.calculateEnglishLevel(currentState.questions, currentState.userAnswers)
        setState { copy(isShowResultDialog = true, languageLevel = languageLevel) }
    }

}
