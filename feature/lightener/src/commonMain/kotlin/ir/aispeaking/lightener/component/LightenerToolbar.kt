package ir.aispeaking.lightener.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.lightener.LightenerUiEvent
import ir.aispeaking.lightener.OnAction
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_eye
import ir.aispeaking.sharedui.ic_eye_close
import ir.aispeaking.sharedui.is_search
import ir.aispeaking.sharedui.lightener_toolbar_title
import ir.aispeaking.sharedui.search_hint
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.input.AppCompactTextField
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LightenerToolbar(
    modifier: Modifier,
    searchText: String,
    isSearchExpanded: Boolean,
    isShowAllTranslations: Boolean = false,
    onAction: OnAction,
    lightenerSize: Int
) {
    AnimatedContent(
        modifier = modifier,
        targetState = isSearchExpanded
    ) { it ->
        when (it) {
            true -> {
                AppCompactTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = searchText,
                    hint = stringResource(Res.string.search_hint),
                    leadingIcon = {
                        AppIcon(
                            icon = Res.drawable.is_search,
                            tint = AppTheme.colors.onSurface
                        )
                    },
                    trailingIcon = {
                        AppIcon(
                            icon = Res.drawable.ic_close,
                            tint = AppTheme.colors.onSurface,
                            onClick = { onAction(LightenerUiEvent.OnSearchIconClick) }
                        )
                    },
                    onFinishTyping = {
                        onAction(LightenerUiEvent.OnRefresh)
                    },
                    onValueChange = {
                        onAction(LightenerUiEvent.OnSearchTextChange(it))
                    }
                )
            }

            false -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (lightenerSize > 0) {
                        AppIcon(
                            icon = when (isShowAllTranslations) {
                                true -> Res.drawable.ic_eye
                                false -> Res.drawable.ic_eye_close
                            },
                            tint = AppTheme.colors.onSurface,
                            onClick = {
                                onAction(LightenerUiEvent.OnShowAllTranslations)
                            }
                        )
                    }

                    BodyLargeBoldText(
                        text = stringResource(Res.string.lightener_toolbar_title),
                        textAlign = TextAlign.Center
                    )

                    AppIcon(
                        icon = Res.drawable.is_search,
                        tint = AppTheme.colors.onSurface,
                        onClick = { onAction(LightenerUiEvent.OnSearchIconClick) }
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        LightenerToolbar(
            modifier = Modifier.fillMaxWidth(),
            searchText = "searchText",
            isSearchExpanded = false,
            onAction = {},
            lightenerSize = 1,
        )
    }
}

@LightDarkPreview
@Composable
private fun ExpandedPreview() {
    AppTheme {
        LightenerToolbar(
            modifier = Modifier.fillMaxWidth(),
            searchText = "searchText",
            isSearchExpanded = true,
            onAction = {},
            lightenerSize = 1,
        )
    }
}