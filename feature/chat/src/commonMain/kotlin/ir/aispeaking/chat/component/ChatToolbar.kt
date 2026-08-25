package ir.aispeaking.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.DialogType
import ir.aispeaking.chat.OnAction
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_more
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.image.AppAsyncImage
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.animatedBorder
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay

@Composable
fun ChatToolbar(
    aiAvatar: String,
    title: String,
    voiceSetting: VoiceSetting,
    moreIconCoordinates: (LayoutCoordinates) -> Unit = {},
    tasksCoordinates: (LayoutCoordinates) -> Unit = {},
    onAction: OnAction,
    scenario: Scenario,
    onTaskClick: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .background(AppTheme.colors.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        AppIcon(
            size = 20.dp,
            icon = Res.drawable.ic_back,
            tint = AppTheme.colors.onSurface,
            onClick = { onAction(ChatUiEvent.OnDialogType(DialogType.ExitChat)) }
        )

        AppAsyncImage(
            modifier = Modifier
                .size(50.dp)
                .background(AppTheme.colors.surfaceContainer, shape = CircleShape)
                .border(0.5.dp, color = AppTheme.colors.onSurface, shape = CircleShape),
            data = aiAvatar,
            shape = CircleShape
        )

        BodyMediumText(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            text = title,
            overflow = TextOverflow.MiddleEllipsis,
            maxLines = 1
        )

        var showAnimateBorder by remember(scenario.tasks) { mutableStateOf(true) }
        LaunchedEffect(showAnimateBorder) {
            if (showAnimateBorder) {
                delay(2500)
                showAnimateBorder = false
            }
        }

        LabelSmallBoldText(
            Modifier
                .wrapContentWidth()
                .animateClickable(onTaskClick)
                .background(
                    color = AppTheme.colors.surfaceContainerLow,
                    shape = AppTheme.shapes.roundSmall
                )
                .then(
                    when (showAnimateBorder || scenario.tasks.all { it.finished }) {
                        true -> {
                            Modifier.animatedBorder(
                                borderColors = if (scenario.tasks.all { it.finished })
                                    immutableListOf(AppTheme.colors.success, AppTheme.colors.success.copy(alpha = 0.9f))
                                else immutableListOf(AppTheme.colors.primary, AppTheme.colors.secondary),
                                backgroundColor = AppTheme.colors.surfaceContainerLow,
                                shape = AppTheme.shapes.roundSmall,
                                borderWidth = 1.dp
                            )
                        }

                        false -> {
                            Modifier.border(
                                width = 1.dp,
                                color = AppTheme.colors.outline,
                                shape = AppTheme.shapes.roundSmall
                            )
                        }
                    }
                )
                .onGloballyPositioned {
                    tasksCoordinates(it)
                }
                .padding(8.dp),
            text = "Tasks ${scenario.tasks.count { it1 -> it1.finished }} / ${scenario.tasks.size}"
        )


        Box(modifier = Modifier, contentAlignment = Alignment.TopEnd)
        {
            DropDownContent(
                onAction = onAction,
                expandedMenu = expandedMenu,
                voiceSetting = voiceSetting,
                onDismiss = {
                    expandedMenu = false
                }
            )

            AppIcon(
                modifier = Modifier
                    .onGloballyPositioned {
                        moreIconCoordinates(it)
                    },
                icon = Res.drawable.ic_more,
                tint = AppTheme.colors.onSurface,
                onClick = { expandedMenu = !expandedMenu }
            )
        }
    }

}


@LightDarkPreview
@Composable
private fun ChatToolbarPreview() {
    AppTheme {
//        ChatToolbar(
//            aiAvatar = "",
//            title = "Take a taxi",
//            voiceSetting = VoiceSetting(),
//            onAction = {}
////        )
    }
}