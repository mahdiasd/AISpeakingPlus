package ir.aispeaking.sharedui.ui.core.dialog.translate


import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobilebytelabs.kmptoolkit.clipboard.copyToClipboard
import ir.aispeaking.domain.model.translate.AlternativeTranslation
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.add_to_lightener
import ir.aispeaking.sharedui.ic_arrow_right
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_copy
import ir.aispeaking.sharedui.in_lightener
import ir.aispeaking.sharedui.translation_dialog_example
import ir.aispeaking.sharedui.translation_dialog_translate
import ir.aispeaking.sharedui.ui.core.button.AppCompactButton
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.loading.PageLoading
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateDialog(
    modifier: Modifier,
    vm: TranslateViewModel = koinViewModel(),
    text: String,
    onDismiss: () -> Unit,
) {
    val clipboardManager = LocalClipboard.current
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    LaunchedEffect(text) {
        vm.onTriggerEvent(TranslateUiEvent.Translate(text))
    }


    val sheetState = rememberModalBottomSheetState(true)
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceContainerLow,
        content = {
            TranslateDialogContent(
                modifier = Modifier,
                text = text,
                isSaved = uiState.isSaved,
                translation = uiState.translation,
                onCopy = {
                    copyToClipboard(text)
                },
                onDismiss = onDismiss,
                addToLightener = {
                    vm.onTriggerEvent(TranslateUiEvent.AddToLightener)
                },
            )
        }
    )

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is TranslateUiNavigation.ToDismiss -> onDismiss()
        }
    }

}

@Composable
fun TranslateDialogContent(
    modifier: Modifier = Modifier,
    text: String,
    translation: Translation?,
    onCopy: (String) -> Unit,
    onDismiss: () -> Unit,
    isSaved: Boolean = false,
    addToLightener: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterVertically)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            AppIcon(
                icon = Res.drawable.ic_copy,
                onClick = {
                    onCopy(text)
                }
            )

            BodyMediumBoldText(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textAlign = TextAlign.Center,
                text = stringResource(Res.string.translation_dialog_translate),
            )

            AppIcon(
                icon = Res.drawable.ic_close,
                onClick = onDismiss
            )
        }

        AnimatedContent(targetState = translation == null) { translationIsNull ->
            if (translationIsNull) {
                PageLoading(modifier = Modifier.fillMaxWidth())
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterVertically)
                ) {
                    BodyLargeBoldText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Start,
                        text = text,
                    )

                    BodyMediumBoldText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        text = translation?.translatedText ?: "",
                        persianFont = true
                    )

                    translation?.alternatives?.takeIf { it.isNotEmpty() }?.let { alternativeTranslations ->
                        Separator()

                        alternativeTranslations.forEach {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp , alignment = Alignment.CenterHorizontally)
                            ) {
                                BodyMediumText(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    persianFont = true,
                                    color = AppTheme.colors.onSurface,
                                    text = it.translations.filter { s -> s.isNotEmpty() }
                                        .fastJoinToString(", ")
                                )

                                AppIcon(
                                    size = 12.dp,
                                    modifier = Modifier.rotate(180f),
                                    icon = Res.drawable.ic_arrow_right
                                )

                                BodyLargeBoldText(
                                    text = it.type,
                                    persianFont = true,
                                )
                            }
                        }
                    }

                    translation?.example?.takeIf { it.isNotEmpty() }?.let {
                        Separator(text = stringResource(Res.string.translation_dialog_example))

                        BodyMediumText(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = it
                        )
                    }

                    VerticalSpace(8.dp)

                    AnimatedContent(isSaved) {
                        when (it) {
                            true -> {
                                AppCompactButton(
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    containerColor = AppTheme.colors.success,
                                    textColor = AppTheme.colors.onSuccess,
                                    text = Res.string.in_lightener,
                                    onClick = {}
                                )
                            }

                            false -> {
                                AppCompactButton(
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    containerColor = AppTheme.colors.primaryContainer,
                                    textColor = AppTheme.colors.onPrimaryContainer,
                                    text = Res.string.add_to_lightener,
                                    onClick = addToLightener
                                )

                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier)
    }
}

@Composable
private fun Separator(
    modifier: Modifier = Modifier,
    text: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            thickness = 1.dp,
            color = AppTheme.colors.surfaceContainer
        )

        text?.let {
            BodyMediumBoldText(
                modifier = Modifier,
                text = it
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            thickness = 2.dp,
            color = AppTheme.colors.surfaceContainer
        )

    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLow),
        ) {
            TranslateDialogContent(
                modifier = Modifier,
                text = "Hello",
                onDismiss = {},
                translation = Translation(
                    uid = "uid",
                    sourceText = "sourceText",
                    translatedText = "معنی کلمه",
                    alternatives = immutableListOf(
                        AlternativeTranslation(
                            type = "صفت",
                            translations = listOf("تست").toImmutableList()
                        )
                    ).toImmutableList(),
                    example = "example",
                    expanded = true,
                    showTranslate = true
                ),
                onCopy = {},
                isSaved = false,
                addToLightener = {},
            )
        }
    }
}