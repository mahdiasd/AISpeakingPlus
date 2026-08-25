package ir.aispeaking.register

// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.register.component.LevelItem
import ir.aispeaking.register.input.FirstName
import ir.aispeaking.register.input.LastName
import ir.aispeaking.register.input.NickName
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.congratulations
import ir.aispeaking.sharedui.dialog_message_vector
import ir.aispeaking.sharedui.gift_message
import ir.aispeaking.sharedui.ic_edit_2
import ir.aispeaking.sharedui.lets_start
import ir.aispeaking.sharedui.register_screen_do_not_know_level
import ir.aispeaking.sharedui.register_screen_go_to_language_level
import ir.aispeaking.sharedui.register_screen_step1_btn
import ir.aispeaking.sharedui.register_screen_step1_title
import ir.aispeaking.sharedui.register_screen_step2_btn
import ir.aispeaking.sharedui.register_screen_step2_title
import ir.aispeaking.sharedui.register_screen_step2_title2
import ir.aispeaking.sharedui.ui.core.avatar.AvatarItem
import ir.aispeaking.sharedui.ui.core.avatar.ShowAvatarsDialog
import ir.aispeaking.sharedui.ui.core.button.AppButton
import ir.aispeaking.sharedui.ui.core.dialog.MessageDialog
import ir.aispeaking.sharedui.ui.core.guide.GuideDialog
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.input.AppTextField
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallText
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.model.avatar.ProfileAvatar
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RegisterScreen(
    mobile: String,
    languageLevel: String,
    vm: RegisterViewModel = koinViewModel { parametersOf(mobile, languageLevel) },
    navigateToLevel: (String) -> Unit,
    navigateToMain: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    when (uiState.screenStep) {
        RegisterScreenStep.Step1 -> {
            RegisterStep1Content(
                modifier = Modifier.baseModifier(24.dp),
                levels = uiState.levels,
                isBtnLoading = uiState.isBtnLoading,
                selectedLevel = uiState.registerParam.languageLevel,
                onAction = { vm.onTriggerEvent(it) }
            )
        }

        RegisterScreenStep.Step2 -> {
            RegisterStep2Content(
                modifier = Modifier
                    .imePadding()
                    .baseModifier(24.dp)
                    .verticalScroll(rememberScrollState()),
                firstName = uiState.firstName,
                nickName = uiState.nickName,
                lastName = uiState.lastName,
                avatars = uiState.avatars,
                selectedAvatar = uiState.selectedAvatar,
                isBtnLoading = uiState.isBtnLoading,
                onAction = { vm.onTriggerEvent(it) }
            )
        }
    }

    if (uiState.showAvatarsDialog) {
        ShowAvatarsDialog(
            avatars = uiState.avatars,
            onClick = {
                vm.onTriggerEvent(RegisterUiEvent.OnSelectAvatar(it))
                vm.onTriggerEvent(RegisterUiEvent.OnShowAvatarsDialog(false))
            },
            onDismiss = { vm.onTriggerEvent(RegisterUiEvent.OnShowAvatarsDialog(false)) }
        )
    }

    if (uiState.giftDays > 0) {
        MessageDialog(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
                .padding(16.dp),
            title = stringResource(Res.string.congratulations),
            message = stringResource(Res.string.gift_message, uiState.giftDays),
            positiveText = Res.string.lets_start,
            onPositive = {
                vm.onTriggerEvent(RegisterUiEvent.NavigateToMain)
            },
            onNegative = {
                vm.onTriggerEvent(RegisterUiEvent.NavigateToMain)
            },
            onDismiss = {
                vm.onTriggerEvent(RegisterUiEvent.NavigateToMain)
            },
            negativeText = null,
            image = Res.drawable.dialog_message_vector
        )
//
//        GiftDialog(
//            giftDays = uiState.giftDays,
//            moveToMain = { }
//        )
    }

    if (uiState.guideList.isNotEmpty()) {
        GuideDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismiss = {vm.onTriggerEvent(RegisterUiEvent.SetGuideRead)},
            list = uiState.guideList
        )
    }

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is RegisterUiNavigation.ToLevel -> navigateToLevel((uiNavigation as RegisterUiNavigation.ToLevel).mobile)
            is RegisterUiNavigation.ToMain -> navigateToMain()
        }
    }
}


@Composable
fun RegisterStep1Content(
    modifier: Modifier = Modifier.baseModifier(),
    levels: ImmutableList<LanguageLevel>,
    selectedLevel: LanguageLevel?,
    isBtnLoading: Boolean = false,
    onAction: OnAction,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BodyLargeBoldText(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(Res.string.register_screen_step1_title),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                12.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            items(
                count = levels.size,
                key = { levels[it].name }
            ) { index ->
                LevelItem(
                    languageLevel = levels[index],
                    isSelected = selectedLevel == levels[index],
                    onClick = { onAction(RegisterUiEvent.OnChangeLevel(levels[index])) }
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = Alignment.CenterHorizontally
                )
            ) {
                LabelSmallText(text = stringResource(Res.string.register_screen_do_not_know_level))
                LabelMediumBoldText(
                    modifier = Modifier.animateClickable { onAction(RegisterUiEvent.OnLevelExam) },
                    text = stringResource(Res.string.register_screen_go_to_language_level),
                    color = AppTheme.colors.primary
                )
            }

            AppButton(
                modifier = Modifier.fillMaxWidth(),
                disabled = selectedLevel == null,
                text = Res.string.register_screen_step1_btn,
                isLoading = isBtnLoading,
                onClick = { onAction(RegisterUiEvent.OnBtnClick) }
            )
        }
    }
}

@Composable
fun RegisterStep2Content(
    modifier: Modifier = Modifier.baseModifier(),
    firstName: FirstName,
    nickName: NickName,
    lastName: LastName,
    isBtnLoading: Boolean = false,
    avatars: ImmutableList<ProfileAvatar>,
    selectedAvatar: ProfileAvatar,
    onAction: OnAction,
) {
    val mainAvatars by remember(avatars) {
        mutableStateOf(
            (avatars.shuffled().filter { it.name.startsWith("G-") }.take(2)
                    + avatars.shuffled().filter { it.name.startsWith("B-") }.take(2)
                    ).toImmutableList()
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                24.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            BodyLargeBoldText(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(Res.string.register_screen_step2_title),
            )

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
//                    Icon(
//                        modifier = Modifier
//                            .size(48.dp)
//                            .background(color = Color.White, CircleShape)
//                            .clip(CircleShape)
//                            .padding(8.dp)
//                            .animateClickable { onAction(RegisterUiEvent.OnShowAvatarsDialog(true)) },
//                        imageVector = Icons.Default.Add,
//                        tint = AppTheme.colors.primary,
//                        contentDescription = "show all avatar",
//
//                        )
                    AppIcon(
                        modifier = Modifier
                            .size(48.dp)
                            .background(color = Color.White, CircleShape)
                            .clip(CircleShape)
                            .padding(8.dp)
                            .animateClickable { onAction(RegisterUiEvent.OnShowAvatarsDialog(true)) },
                        icon = Res.drawable.ic_edit_2, // Using ic_edit_2 as a placeholder until a proper add icon is available
                        tint = AppTheme.colors.primary,
                        contentDescription = "show all avatar"
                    )
                }

                items(
                    count = mainAvatars.size,
                    key = { mainAvatars[it].name }
                ) { index ->
                    AvatarItem(
                        profileAvatar = mainAvatars[index],
                        onClick = {
                            onAction(
                                RegisterUiEvent.OnSelectAvatar(
                                    mainAvatars[index]
                                )
                            )
                        })
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            BodyLargeBoldText(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(Res.string.register_screen_step2_title2),
            )

            VerticalSpace(8.dp)

            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = nickName.value,
                maxLines = 1,
                hint = stringResource(nickName.hint),
                onValueChange = { onAction(RegisterUiEvent.OnChangeNickName(it)) }
            )

            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                hint = stringResource(firstName.hint),
                value = firstName.value,
                onValueChange = { onAction(RegisterUiEvent.OnChangeFirstName(it)) }
            )

            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                hint = stringResource(lastName.hint),
                value = lastName.value,
                onValueChange = { onAction(RegisterUiEvent.OnChangeLastName(it)) }
            )
        }

        AppButton(
            modifier = Modifier.fillMaxWidth(),
            disabled = nickName.copy(shouldValidate = true).validate() is ValidationStatus.Invalid,
            text = Res.string.register_screen_step2_btn,
            isLoading = isBtnLoading,
            onClick = { onAction(RegisterUiEvent.OnBtnClick) }
        )
    }
}


@PreviewLightDark
@Composable
private fun RegisterPreview() {
    AppTheme {
        RegisterStep1Content(
            levels = LanguageLevel.entries.toImmutableList(),
            selectedLevel = LanguageLevel.B1,
            onAction = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun Register2Preview() {
    AppTheme {
        RegisterStep2Content(
            firstName = FirstName(),
            nickName = NickName(),
            lastName = LastName(),
            avatars = AvatarUtils.provideProfileAvatars(),
            selectedAvatar = AvatarUtils.provideProfileAvatars().first(),
            onAction = {}
        )
    }
}
