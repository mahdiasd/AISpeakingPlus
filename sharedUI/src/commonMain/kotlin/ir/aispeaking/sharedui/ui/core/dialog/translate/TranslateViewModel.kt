package ir.aispeaking.sharedui.ui.core.dialog.translate

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.usecase.translation.CreateTranslateUseCase
import ir.aispeaking.domain.usecase.translation.GetTranslationsUseCase
import ir.aispeaking.domain.usecase.translation.TranslateUseCase
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage

import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.utils.dLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class TranslateViewModel(
    private val translateUseCase: TranslateUseCase,
    private val createTranslateUseCase: CreateTranslateUseCase,
    private val getTranslationsUseCase: GetTranslationsUseCase,
) : BaseViewModel<TranslateUiState, TranslateUiEvent>() {

    init {
        this.dLog("init TranslateViewModel")
    }

    override fun onCleared() {
        this.dLog("onCleared TranslateViewModel")
        super.onCleared()
    }

    override fun createInitialState() = TranslateUiState()

    override fun onTriggerEvent(event: TranslateUiEvent) {
        when (event) {
            is TranslateUiEvent.AddToLightener -> createTranslation()
            is TranslateUiEvent.Translate -> translate(event.text)
        }
    }


    private fun translate(text: String) {
        setState { copy(translation = null, translateLoading = true, isSaved = false) }
        checkSavedOrNot(text)
        viewModelScope.launch {
            translateUseCase(text).collect {
                it.onSuccess { translation ->
                    setState { copy(translation = translation, translateLoading = false) }
                }.onFailure {
                    setState { copy(translation = null, translateLoading = true) }
                    setUiMessage(UiMessage(intValue = Res.string.error_translation))
                }
            }
        }
    }

    private fun checkSavedOrNot(text: String) {
        setState { copy(translateLoading = true) }
        viewModelScope.launch {
            getTranslationsUseCase(
                searchText = text,
                page = 1,
            ).collect {
                it.onSuccess { paging ->
                    setState { copy(isSaved = paging.content.isNotEmpty()) }
                }.onFailure { _ ->
                    setState { copy(isSaved = false) }
                }
            }
        }
    }

    private fun createTranslation() {
        viewModelScope.launch {
            setState { copy(addLightenerLoading = false) }
            createTranslateUseCase(translation = currentState.translation!!).collect {
                it.onSuccess {
                    setState { copy(addLightenerLoading = false, isSaved = true) }
                    delay(2500)
                    setUiNavigation(TranslateUiNavigation.ToDismiss)
                }.onFailure { apiError ->
                    setState { copy(addLightenerLoading = false, isSaved = false) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }
}
