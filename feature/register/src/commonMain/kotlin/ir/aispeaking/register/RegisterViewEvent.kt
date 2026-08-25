package ir.aispeaking.register

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.user.Gender
import ir.aispeaking.domain.model.user.RegisterParam
import ir.aispeaking.register.input.FirstName
import ir.aispeaking.register.input.LastName
import ir.aispeaking.register.input.NickName
import ir.aispeaking.sharedui.ui.model.avatar.ProfileAvatar
import ir.aispeaking.sharedui.ui.model.guide.GuideModel
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import ir.aispeaking.utils.immutableListOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.random.Random

@Stable
data class RegisterUiState(
    val isBtnLoading: Boolean = false,
    val showAvatarsDialog: Boolean = false,
    val screenStep: RegisterScreenStep = RegisterScreenStep.Step1,
    val registerParam: RegisterParam = RegisterParam(),
    val levels: ImmutableList<LanguageLevel> = LanguageLevel.entries.toImmutableList(),
    val firstName: FirstName = FirstName(),
    val lastName: LastName = LastName(),
    val nickName: NickName = NickName(),
    val avatars: ImmutableList<ProfileAvatar> = AvatarUtils.provideProfileAvatars(),
    val selectedAvatar: ProfileAvatar = avatars[Random.nextInt(0, avatars.size)],
    val giftDays: Int = 0,
    val guideList: ImmutableList<GuideModel> = immutableListOf(),
) : UiState

enum class RegisterScreenStep {
    Step1,
    Step2
}

sealed class RegisterUiEvent : UiEvent {
    data class OnChangeFirstName(val newText: String) : RegisterUiEvent()
    data class OnChangeLastName(val newText: String) : RegisterUiEvent()
    data class OnChangeNickName(val newText: String) : RegisterUiEvent()
    data class OnSelectGender(val gender: Gender) : RegisterUiEvent()
    data class OnSelectAvatar(val avatar: ProfileAvatar) : RegisterUiEvent()

    data class OnChangeLevel(val level: LanguageLevel) : RegisterUiEvent()

    data object OnBtnClick : RegisterUiEvent()

    data object SetGuideRead : RegisterUiEvent()
    data object NavigateToMain : RegisterUiEvent()
    data class OnShowAvatarsDialog(val show: Boolean) : RegisterUiEvent()
    data object OnLevelExam : RegisterUiEvent()
}

sealed class RegisterUiNavigation : UiNavigation {
    data object ToMain : RegisterUiNavigation()
    data object ToBack : RegisterUiNavigation()
    data class ToLevel(val mobile: String) : RegisterUiNavigation()
}

typealias OnAction = (RegisterUiEvent) -> Unit