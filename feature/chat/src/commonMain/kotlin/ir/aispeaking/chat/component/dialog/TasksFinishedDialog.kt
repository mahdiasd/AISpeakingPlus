package ir.aispeaking.chat.component.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.DialogType
import ir.aispeaking.chat.FinishedTasksState
import ir.aispeaking.chat.OnAction
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.dialog_message_vector
import ir.aispeaking.sharedui.finished_task_exit_btn
import ir.aispeaking.sharedui.finished_task_failed_btn
import ir.aispeaking.sharedui.finished_task_failed_state
import ir.aispeaking.sharedui.finished_task_loading_state
import ir.aispeaking.sharedui.finished_task_not_exit_btn
import ir.aispeaking.sharedui.finished_task_sent_state
import ir.aispeaking.sharedui.finished_task_title
import ir.aispeaking.sharedui.ui.core.button.AppCompactButton
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TasksFinishedDialog(finishedTasksState: FinishedTasksState, onAction: OnAction = {}) {
    Box(
        modifier = Modifier
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, alignment = Alignment.CenterVertically)
        ) {

            BodyMediumBoldText(text = stringResource(Res.string.finished_task_title))

            if(finishedTasksState !is FinishedTasksState.Failed)
            {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    painter = painterResource(Res.drawable.dialog_message_vector),
                    contentDescription = ""
                )
            }

            BodyMediumText(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(
                    when (finishedTasksState) {
                        FinishedTasksState.Failed -> Res.string.finished_task_failed_state
                        FinishedTasksState.Loading -> Res.string.finished_task_loading_state
                        FinishedTasksState.Sent -> Res.string.finished_task_sent_state
                    }
                ),
                color = when (finishedTasksState) {
                    FinishedTasksState.Failed -> AppTheme.colors.error
                    else -> AppTheme.colors.onSurface
                }
            )

            AnimatedContent(
                modifier = Modifier
                    .align(Alignment.End),
                targetState = finishedTasksState
            ) {
                when (it) {
                    FinishedTasksState.Failed -> {
                        AppCompactButton(
                            text = Res.string.finished_task_failed_btn,
                            onClick = { onAction(ChatUiEvent.OnSentTasksFinished) }
                        )
                    }

                    FinishedTasksState.Loading -> {
                        AppCompactButton(
                            modifier = Modifier.fillMaxWidth(0.3f),
                            text = Res.string.finished_task_failed_btn,
                            isLoading = true,
                            loadingDotSize = 16.dp,
                            onClick = { onAction(ChatUiEvent.OnSentTasksFinished) }
                        )
                    }

                    FinishedTasksState.Sent -> {
                        DualContentRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                            leftContent = {
                                AppCompactButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    text = Res.string.finished_task_not_exit_btn,
                                    onClick = { onAction(ChatUiEvent.OnDialogType(DialogType.None)) }
                                )
                            },
                            rightContent = {
                                AppCompactButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    text = Res.string.finished_task_exit_btn,
                                    containerColor = AppTheme.colors.primary,
                                    textColor = AppTheme.colors.onPrimary,
                                    onClick = { onAction(ChatUiEvent.ExitChat) }
                                )
                            }
                        )
                    }
                }
            }
        }

        // TODO LottieLoader
//        LottieLoader(
//            modifier = Modifier.matchParentSize(),
//            anim = R.raw.lt_success,
//            color = null,
//            repeat = 5
//        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(AppTheme.colors.surfaceContainerLow),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            TasksFinishedDialog(FinishedTasksState.Failed)
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            TasksFinishedDialog(FinishedTasksState.Loading)
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            TasksFinishedDialog(FinishedTasksState.Sent)
        }
    }
}