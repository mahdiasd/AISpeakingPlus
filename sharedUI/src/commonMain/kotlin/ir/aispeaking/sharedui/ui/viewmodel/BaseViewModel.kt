package ir.aispeaking.sharedui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : UiState, Event : UiEvent> : ViewModel() {

    private val initialState: State by lazy { createInitialState() }

    val currentState: State get() = uiState.value

    abstract fun createInitialState(): State

    abstract fun onTriggerEvent(event: Event)

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState

    private val _uiMessage: MutableSharedFlow<UiMessage?> = MutableSharedFlow()
    val uiMessage: SharedFlow<UiMessage?> = _uiMessage

    private var messageJob: Job? = null

    protected fun setState(reduce: State.() -> State) {
        val newState = currentState.reduce()
        _uiState.value = newState
    }

    protected fun setUiMessage(uiMessage: UiMessage, delay: Long = 3000L) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            _uiMessage.emit(uiMessage)
            delay(delay)
            _uiMessage.emit(null)
        }
    }

    private val _uiNavigation: MutableSharedFlow<UiNavigation> = MutableSharedFlow()
    val uiNavigation: SharedFlow<UiNavigation> = _uiNavigation

    protected fun setUiNavigation(uiNavigation: UiNavigation) {
        viewModelScope.launch {
            _uiNavigation.emit(uiNavigation)
        }
    }
}