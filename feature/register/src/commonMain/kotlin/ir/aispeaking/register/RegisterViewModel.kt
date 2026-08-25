package ir.aispeaking.register

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.user.RegisterParam
import ir.aispeaking.domain.usecase.guide.ReadGuideStatusUseCase
import ir.aispeaking.domain.usecase.guide.SetGuideForRegister
import ir.aispeaking.domain.usecase.user.auth.RegisterUserUseCase
import ir.aispeaking.register.RegisterUiNavigation.ToLevel
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.register_success_message
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.guide.GuideModel
import ir.aispeaking.sharedui.ui.model.ui_message.MessageStatus
import ir.aispeaking.sharedui.ui.model.ui_message.MessageType
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import ir.aispeaking.utils.dLog
import ir.aispeaking.utils.immutableListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class RegisterViewModel(
    private val mobile: String,
    private val languageLevel: String,
    private val registerUserUseCase: RegisterUserUseCase,
    private val readGuideStatusUseCase: ReadGuideStatusUseCase,
    private val setGuideForRegister: SetGuideForRegister,
) : BaseViewModel<RegisterUiState, RegisterUiEvent>() {

    init {
        readGuideStatus()
        // Restore the mobile number and detected language level passed in from
        // the previous screens (Auth/EnglishLevel) through the RegisterRoute.
        setState {
            copy(
                registerParam = registerParam.copy(
                    mobile = mobile,
                    languageLevel = if (languageLevel.isEmpty()) null
                    else LanguageLevel.valueOf(languageLevel)
                )
            )
        }
    }

    fun readGuideStatus() {
        dLog("readGuideStatus")
        viewModelScope.launch {
            readGuideStatusUseCase.invoke().register.dLog("register:")
            if (!readGuideStatusUseCase.invoke().register) {

                setState { copy(guideList = immutableListOf(GuideModel.RegisterGuideModel())) }
            }
        }
    }

    fun setReadGuide() {
        viewModelScope.launch {
            setGuideForRegister.invoke()
            setState { copy(guideList = immutableListOf()) }
        }
    }

    override fun createInitialState() = RegisterUiState()

    override fun onTriggerEvent(event: RegisterUiEvent) {
        when (event) {
            is RegisterUiEvent.OnBtnClick -> {
                when (currentState.screenStep) {
                    RegisterScreenStep.Step1 -> {
                        setState { copy(screenStep = RegisterScreenStep.Step2) }
                    }

                    RegisterScreenStep.Step2 -> {
                        if (validate())
                            registerUser()
                    }
                }
            }

            is RegisterUiEvent.OnChangeLevel -> {
                setState { copy(registerParam = registerParam.copy(languageLevel = event.level)) }
            }

            is RegisterUiEvent.OnChangeFirstName -> {
                setState { copy(firstName = firstName.copy(value = event.newText)) }
            }

            is RegisterUiEvent.OnChangeLastName -> {
                setState { copy(lastName = lastName.copy(value = event.newText)) }
            }

            is RegisterUiEvent.OnChangeNickName -> {
                setState { copy(nickName = nickName.copy(value = event.newText)) }
            }

            is RegisterUiEvent.OnSelectGender -> {
                setState { copy(registerParam = registerParam.copy(gender = event.gender)) }
            }

            is RegisterUiEvent.OnLevelExam -> {
                setUiNavigation(ToLevel(currentState.registerParam.mobile))
            }

            is RegisterUiEvent.OnSelectAvatar -> {
                setState { copy(selectedAvatar = event.avatar) }
            }

            is RegisterUiEvent.OnShowAvatarsDialog -> {
                setState { copy(showAvatarsDialog = event.show) }
            }

            is RegisterUiEvent.NavigateToMain -> {
                setUiNavigation(RegisterUiNavigation.ToMain)
            }

            is RegisterUiEvent.SetGuideRead -> setReadGuide()
        }
    }

    private fun validate(): Boolean {
        if (currentState.nickName.validate() is ValidationStatus.Invalid) {
            setState { copy(nickName = nickName.copy(shouldValidate = true)) }
            return false
        }
        return true
    }

    private fun updateUserParam(): RegisterParam {
        return currentState.registerParam.copy(
            nickName = currentState.nickName.value,
            firstName = currentState.firstName.value,
            lastName = currentState.lastName.value,
            avatar = currentState.selectedAvatar.name,
        )
    }

    private fun registerUser() {
        val registerParam = updateUserParam()
        setState { copy(isBtnLoading = true) }
        viewModelScope.launch {
            registerUserUseCase(registerParam).collect {
                it.onSuccess { userIntPair ->
                    setState { copy(isBtnLoading = false) }
                    if (userIntPair.second > 0) {
                        setState { copy(giftDays = userIntPair.second) }
                    } else {
                        setUiMessage(
                            UiMessage(
                                intValue = Res.string.register_success_message,
                                messageType = MessageType.Device,
                                status = MessageStatus.Success
                            )
                        )
                        delay(2 * 1000)
                        setUiNavigation(RegisterUiNavigation.ToMain)
                    }
                }.onFailure { apiError ->
                    setState { copy(isBtnLoading = false) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

}