package ir.aispeaking.editprofile

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.usecase.user.GetSharedPrefUserUseCase
import ir.aispeaking.domain.usecase.user.UpdateUserUseCase
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_local_user_not_found
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.ui_message.MessageStatus
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.sharedui.your_information_was_updated_successfully
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class EditProfileViewModel(
    private val getSharedPrefUserUseCase: GetSharedPrefUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
) : BaseViewModel<EditProfileUiState, EditProfileUiEvent>() {
    init {
        getSharedPrefUser()
    }

    private fun getSharedPrefUser() {
        viewModelScope.launch {
            getSharedPrefUserUseCase().collect {
                it.onSuccess { localUser ->
                    setState { copy(user = localUser) }
                    detectAvatar(localUser)
                }.onFailure {
                    setUiMessage(UiMessage(intValue = Res.string.error_local_user_not_found))
                }
            }
        }
    }

    private fun detectAvatar(localUser: User) {
        AvatarUtils.findAvatarByName(localUser.avatar)?.let {
            setState { copy(selectedAvatar = it) }
        }
    }

    override fun createInitialState() = EditProfileUiState()

    override fun onTriggerEvent(event: EditProfileUiEvent) {
        when (event) {
            is EditProfileUiEvent.OnBackClick -> {
                setUiNavigation(EditProfileUiNavigation.ToBack)
            }

            is EditProfileUiEvent.OnBtnClick -> {
                updateUser()
            }

            is EditProfileUiEvent.OnChangeUser -> {
                setState { copy(user = event.user, showLevelDialog = false) }
            }

            is EditProfileUiEvent.OnSelectAvatar -> {
                setState {
                    copy(
                        user = currentState.user!!.copy(avatar = event.avatarProfile.name),
                        selectedAvatar = event.avatarProfile,
                        showAvatarsDialog = false
                    )
                }
            }

            is EditProfileUiEvent.OnShowAvatarsDialog -> {
                setState { copy(showAvatarsDialog = event.show) }
            }

            is EditProfileUiEvent.OnShowLevelsDialog -> {
                setState { copy(showLevelDialog = event.show) }
            }
        }
    }

    private fun updateUser() {
        setState { copy(isBtnLoading = true) }
        viewModelScope.launch {
            updateUserUseCase.invoke(currentState.user!!).collect {
                it.onSuccess {
                    setState { copy(isBtnLoading = false) }
                    setUiMessage(
                        UiMessage(
                            intValue = Res.string.your_information_was_updated_successfully,
                            status = MessageStatus.Success
                        )
                    )
                    delay(3000)
                    setUiNavigation(EditProfileUiNavigation.ToBack)
                }.onFailure { apiError ->
                    setState { copy(isBtnLoading = false) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }
}


