package ir.aispeaking.editprofile

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.ui.model.avatar.ProfileAvatar
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.random.Random

@Stable
data class EditProfileUiState(
    var user: User? = null,
    var showAvatarsDialog: Boolean = false,
    val showLevelDialog: Boolean = false,
    val isBtnLoading: Boolean = false,

    val levels: ImmutableList<LanguageLevel> = LanguageLevel.entries.toImmutableList(),

    val avatars: ImmutableList<ProfileAvatar> = AvatarUtils.provideProfileAvatars(),
    val selectedAvatar: ProfileAvatar = avatars[Random.nextInt(0, avatars.size)],
) : UiState

sealed class EditProfileUiEvent : UiEvent {
    data object OnBackClick : EditProfileUiEvent()
    data object OnBtnClick : EditProfileUiEvent()

    data class OnShowAvatarsDialog(val show: Boolean) : EditProfileUiEvent()
    data class OnShowLevelsDialog(val show: Boolean) : EditProfileUiEvent()
    data class OnSelectAvatar(val avatarProfile: ProfileAvatar) : EditProfileUiEvent()
    data class OnChangeUser(val user: User) : EditProfileUiEvent()
}

sealed class EditProfileUiNavigation : UiNavigation {
    data object ToBack : EditProfileUiNavigation()
}

typealias OnAction = (EditProfileUiEvent) -> Unit


