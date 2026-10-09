package ir.aispeaking.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.domain.repository.auth.AuthRepository
import ir.aispeaking.domain.usecase.user.GetUserProfileUseCase
import ir.aispeaking.domain.usecase.user.UpdateUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserProfile? = null,
    val errorMessage: String? = null,
    val showAvatarPicker: Boolean = false,
    val showEditNameDialog: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val showLevelPicker: Boolean = false,
    val isSaving: Boolean = false
) {
    val isGuest: Boolean
        get() = user?.isGuest ?: true
}

@Factory
class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getUserProfileUseCase()) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = result.data, errorMessage = null) }
                }
                is DataResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "خطا در دریافت اطلاعات کاربر") }
                }
            }
        }
    }

    fun refresh() {
        loadProfile()
    }

    fun onAvatarPickerClicked() {
        _uiState.update { it.copy(showAvatarPicker = true) }
    }

    fun onAvatarSelected(avatarKey: String) {
        _uiState.update { it.copy(showAvatarPicker = false) }
        updateProfile(UpdateProfileInput(avatar = avatarKey))
    }

    fun onEditNameClicked() {
        _uiState.update { it.copy(showEditNameDialog = true) }
    }

    fun onSaveNickName(newNickName: String) {
        val trimmed = newNickName.trim()
        if (trimmed.length < 2 || trimmed.length > 50) {
            _uiState.update { it.copy(errorMessage = "نام مستعار باید بین ۲ تا ۵۰ نویسه باشد") }
            return
        }
        _uiState.update { it.copy(showEditNameDialog = false) }
        updateProfile(UpdateProfileInput(nickName = trimmed))
    }

    private fun updateProfile(input: UpdateProfileInput) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = updateUserProfileUseCase(input)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, user = result.data, errorMessage = null) }
                }
                is DataResult.Failure -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "خطا در ذخیره تغییرات") }
                }
            }
        }
    }

    fun onSignOutClicked() {
        _uiState.update { it.copy(showSignOutDialog = true) }
    }

    fun onConfirmSignOut(onComplete: () -> Unit) {
        _uiState.update { it.copy(showSignOutDialog = false) }
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }

    fun onLevelPickerClicked() {
        _uiState.update { it.copy(showLevelPicker = true) }
    }

    fun onSelectLanguageLevel(level: String) {
        _uiState.update { it.copy(showLevelPicker = false) }
        updateProfile(UpdateProfileInput(languageLevel = level))
    }

    fun onDismissDialogs() {
        _uiState.update {
            it.copy(
                showAvatarPicker = false,
                showEditNameDialog = false,
                showSignOutDialog = false,
                showLevelPicker = false
            )
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
