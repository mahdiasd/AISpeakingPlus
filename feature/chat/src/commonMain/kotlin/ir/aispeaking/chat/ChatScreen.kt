package ir.aispeaking.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.ktor.http.encodeURLQueryComponent
import ir.aispeaking.chat.audio.rememberAudioPlayer
import ir.aispeaking.chat.component.ChatBottomBarContent
import ir.aispeaking.chat.component.ChatToolbar
import ir.aispeaking.chat.component.SuggestsContent
import ir.aispeaking.chat.component.TasksContent
import ir.aispeaking.chat.component.UserStarterContent
import ir.aispeaking.chat.component.bubble.BubbleVoiceText
import ir.aispeaking.chat.component.chat_item.AiChatItem
import ir.aispeaking.chat.component.chat_item.ChatLoading
import ir.aispeaking.chat.component.chat_item.UserChatItem
import ir.aispeaking.chat.component.dialog.HandleDialogs
import ir.aispeaking.chat.input.Message
import ir.aispeaking.chat.stt.SttUiState
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.scenario.Role
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.network.platformBaseUrl
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_first_chat_of_ai
import ir.aispeaking.sharedui.ui.core.error.ErrorContent
import ir.aispeaking.sharedui.ui.core.guide.GuideDialog
import ir.aispeaking.sharedui.ui.core.loading.PageLoading
import ir.aispeaking.sharedui.ui.core.permission.PerPermissionScreen
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.model.permission.PermissionState
import ir.aispeaking.sharedui.ui.remember.rememberPermissionGrant
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.utils.dLog
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ChatScreen(
    level: String,
    vm: ChatViewModel = koinViewModel { parametersOf(level) },
    navigateBack: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)
    val permissionGrant = rememberPermissionGrant(uiState.permissions)
    val audioPlayer = rememberAudioPlayer()

    val navState = rememberNavigationEventState(
        currentInfo = NavigationEventInfo.None,
    )

    NavigationBackHandler(
        state = navState,
        isBackEnabled = true,
        onBackCompleted = {
            audioPlayer.stop()
            vm.onTriggerEvent(ChatUiEvent.OnDialogType(DialogType.ExitChat))
        },
    )

    LaunchedEffect(permissionGrant) {
        vm.onTriggerEvent(ChatUiEvent.OnPermissionChange(permissionGrant))
    }

    val activeAiChat = remember(uiState.chats) {
        uiState.chats.filterIsInstance<Chat.Ai>().firstOrNull {
            it.voiceState is AiVoiceState.PendingToPlay || it.voiceState is AiVoiceState.Playing
        }
    }

    LaunchedEffect(activeAiChat?.uid, activeAiChat?.voiceState) {
        if (activeAiChat != null && activeAiChat.voiceState == AiVoiceState.PendingToPlay) {
            val chatId = activeAiChat.uid
            val audioUrl = activeAiChat.audioUrl
            val fullUrl = if (!audioUrl.isNullOrBlank()) {
                if (audioUrl.startsWith("http://") || audioUrl.startsWith("https://")) {
                    audioUrl
                } else {
                    val base = platformBaseUrl().trimEnd('/')
                    val path = audioUrl.trimStart('/')
                    "$base/$path"
                }
            } else {
                val base = platformBaseUrl().trimEnd('/')
                val encodedText = activeAiChat.message.encodeURLQueryComponent()
                "$base/api/v1/tts/speak?text=$encodedText&voiceId=${uiState.voiceSetting.voiceId}&speed=${uiState.voiceSetting.speed}"
            }

            vm.onTriggerEvent(ChatUiEvent.OnChangeAiVoiceState(chatId, AiVoiceState.Playing))
            audioPlayer.play(
                url = fullUrl,
                onComplete = {
                    vm.onTriggerEvent(ChatUiEvent.OnChangeAiVoiceState(chatId, AiVoiceState.Stopped))
                },
                onError = { e ->
                    "ChatScreen Audio error: ${e.message}".dLog(tag = "AudioPlayer")
                    vm.onTriggerEvent(ChatUiEvent.OnChangeAiVoiceState(chatId, AiVoiceState.Stopped))
                }
            )
        } else if (activeAiChat == null || (activeAiChat.voiceState == AiVoiceState.Stopped)) {
            // If stopped externally
        }
    }

    uiState.scenario?.let { scenario ->
        ChatScreenContent(
            modifier = Modifier.fillMaxSize(),
            screenInputType = uiState.screenInputType,
            message = uiState.message,
            onAction = { event ->
                if (event is ChatUiEvent.OnChangeAiVoiceState && event.voiceState == AiVoiceState.Stopped) {
                    audioPlayer.stop()
                }
                vm.onTriggerEvent(event)
            },
            chats = uiState.chats,
            scenario = scenario,
            isRecording = uiState.isRecording,
            voiceSetting = uiState.voiceSetting,
            showErrorContent = uiState.showErrorContent,
            stt = uiState.stt,
        )
    } ?: run {
        PageLoading(modifier = Modifier.fillMaxSize())
    }

    uiState.permissionState.takeIf { it is PermissionState.Requesting }?.let {
        PerPermissionScreen(
            permissions = uiState.permissions,
            onDismissDialog = {
                vm.onTriggerEvent(ChatUiEvent.OnPermissionChange(permissionGrant))
            }
        )
    }

    HandleDialogs(
        dialogType = uiState.dialogType,
        voiceSetting = uiState.voiceSetting,
        voices = uiState.voices,
        selectedVoice = uiState.selectedVoice,
        onAction = { vm.onTriggerEvent(it) }
    )

    if (uiState.guideList.isNotEmpty()) {
        GuideDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismiss = { vm.onTriggerEvent(ChatUiEvent.SetGuideRead) },
            list = uiState.guideList
        )
    }

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is ChatUiNavigation.ToBack -> {
                audioPlayer.stop()
                navigateBack()
            }
        }
    }
}

@Composable
fun ChatScreenContent(
    modifier: Modifier,
    scenario: Scenario,
    screenInputType: InputType,
    message: Message,
    isRecording: Boolean,
    voiceSetting: VoiceSetting,
    onAction: OnAction,
    chats: ImmutableList<Chat>,
    showErrorContent: Boolean,
    stt: SttUiState,
) {
    val listState = rememberLazyListState()
    val suggests by remember(chats) { derivedStateOf { (chats.lastOrNull()?.takeIf { chat -> chat is Chat.Ai } as? Chat.Ai)?.suggests } }
    var isShowTaskDialog by remember { mutableStateOf(false) }

    // Show the voice bubble when there is something to display: either the
    // user has typed text in the message field, OR the server is streaming
    // a live partial transcript.
    val hasDisplayableText = message.value.isNotEmpty() ||
        (stt.partial.isNotEmpty() && stt.connected)
    val isShowBubbleVoiceText by remember(message.value, stt.partial, stt.connected, screenInputType, voiceSetting.showTranscribe)
    {
        derivedStateOf {
            hasDisplayableText &&
                voiceSetting.showTranscribe &&
                screenInputType is InputType.Voice
        }
    }
    // Display text combines the committed message + the live partial so the
    // user sees one continuous line that grows as they speak.
    val bubbleText = remember(message.value, stt.partial, stt.connected) {
        if (stt.connected && stt.partial.isNotEmpty()) {
            if (message.value.isEmpty()) stt.partial
            else "${message.value} ${stt.partial}"
        } else {
            message.value
        }
    }

    LaunchedEffect(chats.lastOrNull()) {
        if (chats.isNotEmpty()) {
            // Wait for composition to complete
            delay(200.milliseconds)
            listState.scrollToItem(
                index = chats.lastIndex,
                scrollOffset = 15 * 1000
            )
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        topBar = {
            ChatToolbar(
                aiAvatar = scenario.aiAvatar ?: "",
                title = scenario.title,
                voiceSetting = voiceSetting,
                scenario = scenario,
                onAction = onAction,
                onTaskClick = {
                    isShowTaskDialog = !isShowTaskDialog
                }
            )
        },
        bottomBar = {
            ChatBottomBarContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                message = message,
                screenInputType = screenInputType,
                onAction = onAction,
                isRecording = isRecording,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clipScrollableContainer(Orientation.Vertical)
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                state = listState,
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 48.dp),
            ) {
                itemsIndexed(items = chats, key = { _, item -> item.uid }) { index, chat ->
                    Box(modifier = Modifier.fillMaxWidth())
                    {
                        when (chat) {
                            is Chat.User -> UserChatItem(
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterEnd),
                                chat = chat,
                                onAction = onAction
                            )

                            is Chat.Ai -> AiChatItem(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .align(alignment = Alignment.CenterStart),
                                chat = chat,
                                isLastChat = index == chats.lastIndex,
                                onAction = onAction
                            )

                            Chat.WaitingForAi -> {
                                ChatLoading(
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterStart)
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                visible = scenario.starter == Role.User && chats.isEmpty()
            ) {
                UserStarterContent(onAction)
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                visible = showErrorContent && scenario.starter != Role.User
            ) {
                ErrorContent(
                    onRetry = { onAction(ChatUiEvent.RetrySendChat) },
                    text = Res.string.error_first_chat_of_ai
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                visible = !suggests.isNullOrEmpty() && !isShowBubbleVoiceText
            ) {
                SuggestsContent(
                    modifier = Modifier.fillMaxWidth(),
                    suggests = suggests,
                    onAction = onAction,
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.BottomCenter),
                visible = isShowBubbleVoiceText,
            ) {
                BubbleVoiceText(
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    message = message.copy(value = bubbleText),
                    onAction = onAction
                )
            }

            AnimatedVisibility(
                modifier = Modifier,
                visible = isShowTaskDialog,
                enter = slideInVertically(
                    initialOffsetY = { -it + 260 }
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it + 260 }
                ) + fadeOut()
            ) {
                TasksContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    tasks = scenario.tasks,
                    onBackClick = {
                        isShowTaskDialog = !isShowTaskDialog
                    }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ChatPreview() {
    AppTheme {
        ChatScreenContent(
            modifier = Modifier.baseModifier(),
            scenario = FakeData.provideScenarios().first(),
            screenInputType = InputType.Voice,
            message = Message(),
            isRecording = false,
            voiceSetting = VoiceSetting(),
            onAction = {},
            chats = FakeData.provideAiChats(),
            showErrorContent = false,
            stt = SttUiState(),
        )
    }
}
