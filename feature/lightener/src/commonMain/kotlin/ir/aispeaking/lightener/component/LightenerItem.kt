package ir.aispeaking.lightener.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import ir.aispeaking.domain.model.translate.AlternativeTranslation
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.lightener.LightenerUiEvent
import ir.aispeaking.lightener.OnAction
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.example
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.ic_arrow_right
import ir.aispeaking.sharedui.ic_delete
import ir.aispeaking.sharedui.ic_eye
import ir.aispeaking.sharedui.ic_eye_close
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.utils.immutableListOf
import org.jetbrains.compose.resources.stringResource

@Composable
fun LightenerItem(
    modifier: Modifier,
    translation: Translation,
    isRemoveLoading: Boolean,
    onAction: OnAction
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (translation.expanded) {
            true -> AppTheme.colors.surfaceContainerLow
            false -> Color.Transparent
        }
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            DualContentRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                leftContent = {
                    LightenerItemDeleteContent(
                        uid = translation.uid,
                        removeLoading = isRemoveLoading,
                        onAction = onAction,
                    )
                },
                rightContent = {
                    BodyMediumText(
                        text = translation.sourceText,
                        maxLines = if (translation.expanded) 6 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )

            if (!translation.expanded) {
                AppIcon(
                    modifier = Modifier.padding(4.dp),
                    icon = when (translation.showTranslate) {
                        true -> Res.drawable.ic_eye
                        false -> Res.drawable.ic_eye_close
                    },
                    tint = when (translation.showTranslate) {
                        true -> AppTheme.colors.onSurface
                        false -> AppTheme.colors.primary
                    },
                    onClick = {
                        onAction(LightenerUiEvent.OnChangeTranslation(translation.copy(showTranslate = !translation.showTranslate)))
                    }
                )
            }

            DualContentRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                leftContent = {
                    BodyMediumText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (translation.showTranslate) Modifier
                                else Modifier.padding(top = 6.dp)
                            )
                            .weight(1f),
                        text = if (translation.showTranslate || translation.expanded) translation.translatedText else "*****",
                        maxLines = if (translation.expanded) 6 else 1,
                        textAlign = TextAlign.Start,
                        textDirection = TextDirection.Rtl,
                        textStyle = AppTheme.typography.bodyMediumBold.copy(fontFamily = AppTheme.typography.persianRegular),
                        overflow = TextOverflow.Ellipsis
                    )
                },
                rightContent = {
                    val rotateAnim by animateFloatAsState(targetValue = if (translation.expanded) 125f else 45f)
                    AppIcon(
                        modifier = Modifier.rotate(rotateAnim),
                        icon = Res.drawable.ic_arrow_left,
                        onClick = {
                            onAction(LightenerUiEvent.OnChangeTranslation(translation.copy(expanded = !translation.expanded)))
                        }
                    )
                }
            )
        }

        AnimatedVisibility(visible = translation.expanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
            ) {
                if (translation.alternatives.isNotEmpty()) {
                    translation.alternatives.forEach {
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
                                color = AppTheme.colors.outline,
                                text = it.translations.filter { s -> s.isNotEmpty() }
                                    .fastJoinToString(", ")
                            )

                            AppIcon(
                                size = 12.dp,
                                modifier = Modifier.rotate(180f),
                                icon = Res.drawable.ic_arrow_right
                            )

                            BodyMediumBoldText(
                                text = it.type,
                                persianFont = true,
                            )
                        }
                    }
                }

                if (translation.example.isNotEmpty()) {
                    DualContentRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        leftContent = {
                            LabelMediumBoldText(
                                modifier = Modifier,
                                color = AppTheme.colors.outline,
                                text = "${stringResource(Res.string.example)}: "
                            )
                        },
                        rightContent = {
                            LabelMediumText(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                text = translation.example,
                                color = AppTheme.colors.outline,
                            )
                        }
                    )
                }

                VerticalSpace(8.dp)
            }
        }
    }
}

@Composable
fun LightenerItemDeleteContent(
    uid: String,
    removeLoading: Boolean,
    onAction: (LightenerUiEvent) -> Unit
) {
    AnimatedContent(targetState = removeLoading) {
        when (it) {
            true -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 1.dp,
                    color = AppTheme.colors.primary
                )
            }

            false -> {
                AppIcon(
                    modifier = Modifier.padding(4.dp),
                    icon = Res.drawable.ic_delete,
                    onClick = { onAction(LightenerUiEvent.OnShowDeleteDialog(uid)) },
                    tint = AppTheme.colors.outline
                )
            }
        }
    }
}


@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        LightenerItem(
            modifier = Modifier,
            translation = Translation(
                sourceText = "Hello, Im Mahdi, your teacher.",
                translatedText = "سلام، من مهدی هستم، معلم شما",
                alternatives = immutableListOf(),
                example = "This is example",
                expanded = false
            ),
            isRemoveLoading = false
        ) { }
    }
}

@LightDarkPreview
@Composable
private fun ExpandedPreview() {
    AppTheme {
        LightenerItem(
            modifier = Modifier,
            translation = Translation(
                sourceText = "Hello, Im Mahdi, your teacher.",
                translatedText = "سلام، من مهدی هستم، معلم شما",
                alternatives = immutableListOf(AlternativeTranslation("ضمیر", immutableListOf("تست اول", "تست دوم"))),
                example = "This is example",
                expanded = true
            ),
            isRemoveLoading = false
        ) { }
    }
}