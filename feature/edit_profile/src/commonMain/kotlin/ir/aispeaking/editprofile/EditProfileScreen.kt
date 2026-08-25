package ir.aispeaking.editprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.edit_profile_toolbar_title
import ir.aispeaking.sharedui.ic_add
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.input_first_name
import ir.aispeaking.sharedui.input_language_level
import ir.aispeaking.sharedui.input_last_name
import ir.aispeaking.sharedui.input_nick_name
import ir.aispeaking.sharedui.language_level
import ir.aispeaking.sharedui.ui.core.avatar.AvatarItem
import ir.aispeaking.sharedui.ui.core.avatar.ShowAvatarsDialog
import ir.aispeaking.sharedui.ui.core.button.AppButton
import ir.aispeaking.sharedui.ui.core.dialog.language_level.LevelDialog
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.input.AppTextField
import ir.aispeaking.sharedui.ui.core.input.textFieldColors
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.core.toolbar.AppToolbar
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.core.unauthorized.UnauthorizedContent
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.model.avatar.ProfileAvatar
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import ir.aispeaking.sharedui.update_btn
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditProfileScreen(
    vm: EditProfileViewModel = koinViewModel(),
    navigateToLogin: () -> Unit,
    navigateBack: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    if (uiState.user != null) {
        EditProfileScreenContent(
            user = uiState.user!!,
            selectedAvatar = uiState.selectedAvatar,
            avatars = uiState.avatars,
            isBtnLoading = uiState.isBtnLoading,
            onAction = { vm.onTriggerEvent(it) }
        )
    } else {
        UnauthorizedContent(
            modifier = Modifier.baseModifier(),
            navigateToLogin = navigateToLogin
        )
    }


    if (uiState.showAvatarsDialog) {
        ShowAvatarsDialog(
            avatars = uiState.avatars,
            onClick = {
                vm.onTriggerEvent(EditProfileUiEvent.OnSelectAvatar(it))
                vm.onTriggerEvent(EditProfileUiEvent.OnShowAvatarsDialog(false))
            },
            onDismiss = { vm.onTriggerEvent(EditProfileUiEvent.OnShowAvatarsDialog(false)) }
        )
    }

    if (uiState.showLevelDialog) {
        LevelDialog(
            onNewLevel = {
                vm.onTriggerEvent(EditProfileUiEvent.OnChangeUser(uiState.user!!.copy(languageLevel = it)))
            },
            onDismiss = {
                vm.onTriggerEvent(EditProfileUiEvent.OnShowLevelsDialog(false))
            },

            currentLevel = uiState.user!!.languageLevel
        )
    }
    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is EditProfileUiNavigation.ToBack -> navigateBack()
        }
    }
}


@Composable
fun EditProfileScreenContent(
    user: User,
    selectedAvatar: ProfileAvatar,
    avatars: ImmutableList<ProfileAvatar>,
    onAction: OnAction,
    isBtnLoading: Boolean = false,
) {
    Scaffold(
        containerColor = AppTheme.colors.surface,
        topBar = {
            AppToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                title = stringResource(Res.string.edit_profile_toolbar_title),
                onLeftIconClick = { onAction(EditProfileUiEvent.OnBackClick) }
            )
        },
        bottomBar = {
            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                disabled = user.nickName.isEmpty(),
                isLoading = isBtnLoading,
                text = Res.string.update_btn,
                onClick = { onAction(EditProfileUiEvent.OnBtnClick) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
        ) {
            Image(
                modifier = Modifier
                    .background(color = AppTheme.colors.surfaceContainer, CircleShape)
                    .clip(CircleShape)
                    .size(120.dp),
                painter = painterResource(selectedAvatar.drawable),
                contentScale = ContentScale.FillBounds,
                contentDescription = "Avatar of your profile"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stickyHeader {
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .background(color = Color.White, CircleShape)
                            .clip(CircleShape)
                            .padding(8.dp)
                            .animateClickable { onAction(EditProfileUiEvent.OnShowAvatarsDialog(true)) },
                        painter = painterResource(Res.drawable.ic_add),
                        tint = AppTheme.colors.primary,
                        contentDescription = "show all avatar",

                        )
                }
                items(count = avatars.take(4).size, key = { avatars[it].name }) { index ->
                    AvatarItem(
                        profileAvatar = avatars[index],
                        onClick = {
                            onAction(EditProfileUiEvent.OnSelectAvatar(avatars[index]))
                        })
                }
            }


            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(1.dp),
                color = AppTheme.colors.outlineVariant
            )

            LabelSmallBoldText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.language_level)
            )

            AppTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateClickable { onAction(EditProfileUiEvent.OnShowLevelsDialog(true)) },
                value = user.languageLevel.name,
                maxLines = 1,
                hint = stringResource(Res.string.input_language_level),
                enabled = false,
                showClearIcon = false,
                colors = textFieldColors(disabledTextColor = AppTheme.colors.onSurface),
                trailingIcon = {
                    AppIcon(
                        size = 16.dp,
                        modifier = Modifier.rotate(180f),
                        icon = Res.drawable.ic_arrow_left,
                        tint = AppTheme.colors.onSurface
                    )
                },
                onValueChange = {}
            )


            LabelSmallBoldText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = stringResource(Res.string.input_nick_name)
            )
            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = user.nickName,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                showClearIcon = false,
                hint = stringResource(Res.string.input_nick_name),
                onValueChange = { onAction(EditProfileUiEvent.OnChangeUser(user.copy(nickName = it))) }
            )

            LabelSmallBoldText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = stringResource(Res.string.input_first_name)
            )

            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                showClearIcon = false,
                hint = stringResource(Res.string.input_first_name),
                value = user.firstName,
                onValueChange = { onAction(EditProfileUiEvent.OnChangeUser(user.copy(firstName = it))) }
            )

            LabelSmallBoldText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = stringResource(Res.string.input_last_name)
            )

            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                showClearIcon = false,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                hint = stringResource(Res.string.input_last_name),
                value = user.lastName,
                onValueChange = { onAction(EditProfileUiEvent.OnChangeUser(user.copy(lastName = it))) }
            )

        }
    }
}


@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        EditProfileScreenContent(
            user = FakeData.provideUsers().first(),
            selectedAvatar = AvatarUtils.provideProfileAvatars().first(),
            avatars = AvatarUtils.provideProfileAvatars(),
            onAction = {},
            isBtnLoading = false
        )
    }
}



