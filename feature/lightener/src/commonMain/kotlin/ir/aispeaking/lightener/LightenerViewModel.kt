package ir.aispeaking.lightener

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.paging.addMore
import ir.aispeaking.domain.model.paging.errorHappened
import ir.aispeaking.domain.model.paging.firstPage
import ir.aispeaking.domain.model.paging.nextPage
import ir.aispeaking.domain.usecase.translation.CreateTranslateUseCase
import ir.aispeaking.domain.usecase.translation.DeleteTranslateUseCase
import ir.aispeaking.domain.usecase.translation.GetTranslationsUseCase
import ir.aispeaking.domain.usecase.translation.UpdateTranslateUseCase
import ir.aispeaking.domain.usecase.user.GetSharedPrefUserUseCase
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.utils.constant.AppConstant
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LightenerViewModel(
    savedStateHandle: SavedStateHandle?,
    private val getTranslationsUseCase: GetTranslationsUseCase,
    private val deleteTranslateUseCase: DeleteTranslateUseCase,
    private val getSharedPrefUserUseCase: GetSharedPrefUserUseCase,
    private val createTranslateUseCase: CreateTranslateUseCase,
    private val updateTranslateUseCase: UpdateTranslateUseCase,
) : BaseViewModel<LightenerUiState, LightenerUiEvent>() {

    init {
        getSharedPrefUser()
    }

    private fun getSharedPrefUser() {
        viewModelScope.launch {
            getSharedPrefUserUseCase().collect {
                it.onSuccess { localUser ->
                    setState { copy(user = localUser) }
                    onTriggerEvent(LightenerUiEvent.OnRefresh)
                }.onFailure {

                }
            }
        }
    }

    override fun createInitialState() = LightenerUiState()

    override fun onTriggerEvent(event: LightenerUiEvent) {
        when (event) {
            is LightenerUiEvent.OnBtnClick -> {

            }

            is LightenerUiEvent.OnLoadMore -> {
                if (!currentState.translationPaging.isLast) {
                    setState { copy(translationPaging = translationPaging.nextPage()) }
                    getTranslations()
                }
            }

            is LightenerUiEvent.OnRefresh -> {
                setState { copy(translationPaging = translationPaging.firstPage()) }
                getTranslations()
            }

            is LightenerUiEvent.OnSearchIconClick -> {
                setState { copy(isExpandedSearch = !currentState.isExpandedSearch) }
            }

            is LightenerUiEvent.OnSearchTextChange -> {
                setState { copy(searchText = event.text) }
            }

            LightenerUiEvent.OnShowAllTranslations -> {
                val allShow = currentState.translationPaging.content.all { it.showTranslate }

                setState {
                    copy(translationPaging = translationPaging.copy(content = translationPaging.content.map {
                        it.copy(showTranslate = !allShow)
                    }.toImmutableList()))
                }
            }

            is LightenerUiEvent.DeleteTranslation -> {
                deleteTranslation(uid = event.uid)
            }

            is LightenerUiEvent.OnChangeTranslation -> {
                setState {
                    copy(translationPaging = translationPaging.copy(content = translationPaging.content.map {
                        if (it.uid == event.translation.uid) event.translation
                        else it
                    }.toImmutableList()))
                }
            }

            is LightenerUiEvent.OnShowDeleteDialog -> {
                setState { copy(deleteDialogUid = event.uid) }
            }

            LightenerUiEvent.CheckForFetching -> {
                if (AppConstant.needToFetchTranslations)
                    onTriggerEvent(LightenerUiEvent.OnRefresh)
            }
        }
    }

    private fun getTranslations() {
        viewModelScope.launch {
            getTranslationsUseCase(
                searchText = currentState.searchText.ifEmpty { null },
                page = currentState.translationPaging.page,
            ).collect {
                it.onSuccess {
                    setState { copy(translationPaging = translationPaging.addMore(it)) }
                }.onFailure { apiError ->
                    setState { copy(translationPaging = translationPaging.errorHappened()) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

    private fun deleteTranslation(uid: String) {
        setState { copy(deletingId = uid) }

        viewModelScope.launch {
            deleteTranslateUseCase(uid).collect { result ->
                result.onSuccess {
                    setState {
                        copy(
                            deletingId = "",
                            translationPaging = translationPaging.copy(
                                content = translationPaging.content
                                    .filterNot { translation -> translation.uid == uid }
                                    .toImmutableList()
                            )
                        )
                    }
                }.onFailure { apiError ->
                    setState { copy(deletingId = "") }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }
}

