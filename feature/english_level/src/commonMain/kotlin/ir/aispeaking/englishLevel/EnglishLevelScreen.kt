package ir.aispeaking.englishLevel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.englishLevel.component.IndicatorSection
import ir.aispeaking.englishLevel.component.QuestionItem
import ir.aispeaking.englishLevel.component.ResultDialog
import ir.aispeaking.englishLevel.model.Question
import ir.aispeaking.englishLevel.utils.QuestionUtils
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.english_level_continue_to_register
import ir.aispeaking.sharedui.english_level_screen_title
import ir.aispeaking.sharedui.english_level_second_title
import ir.aispeaking.sharedui.english_level_second_title_fa
import ir.aispeaking.sharedui.ic_translate
import ir.aispeaking.sharedui.ui.core.button.AppButton
import ir.aispeaking.sharedui.ui.core.text.BodyLargeText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.toolbar.AppToolbar
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.extension.coloredShadow
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

@Composable
fun EnglishLevelScreen(
    mobile: String,
    vm: EnglishLevelViewModel = koinViewModel { parametersOf(mobile) },
    navigateToBack: () -> Unit,
    navigateToRegister: (LanguageLevel, String) -> Unit,
) {

    val uiState = vm.uiState.collectAsState().value

    EnglishLevelScreenContent(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        onAction = { vm.onTriggerEvent(it) },
        questions = uiState.questions,
        languageLevel = uiState.languageLevel,
        userAnswers = uiState.userAnswers
    )

    if (uiState.isShowResultDialog && uiState.languageLevel != null) {
        ResultDialog(
            languageLevel = uiState.languageLevel,
            onAction = { vm.onTriggerEvent(it) }
        )
    }

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(Unit) {
        vm.uiNavigation.collectLatest {
            when (it) {
                EnglishLevelUiNavigation.ToBack -> navigateToBack()
                is EnglishLevelUiNavigation.NavigateToRegister -> navigateToRegister(it.languageLevel, it.mobile)
            }
        }
    }

}

@Composable
fun EnglishLevelScreenContent(
    modifier: Modifier = Modifier,
    questions: ImmutableList<Question>,
    onAction: OnAction,
    userAnswers: Map<Uuid, Int>,
    languageLevel: LanguageLevel?,
) {
    val pagerState = rememberPagerState(pageCount = { questions.size })
    val cScope = rememberCoroutineScope()
    var title by remember { mutableStateOf(Res.string.english_level_second_title) }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        topBar = {
            AppToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .statusBarsPadding(),
                title = stringResource(Res.string.english_level_screen_title),
                onLeftIconClick = { onAction(EnglishLevelUiEvent.OnBackBtnClick) }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = Alignment.CenterVertically
                )
            ) {
                if (languageLevel != null) {
                    AppButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = Res.string.english_level_continue_to_register,
                        onClick = { onAction(EnglishLevelUiEvent.OnContinueRegisterBtnClick) }
                    )
                }
                IndicatorSection(
                    pagerState = pagerState,
                    userAnswers = userAnswers,
                    questions = questions,
                    onAction = onAction
                )

            }
        })
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            DualContentRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
                leftContent = {
                    BodyLargeText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textAlign = TextAlign.Center,
                        text = stringResource(title)
                    )
                },
                rightContent = {
                    Image(
                        modifier = Modifier
                            .size(24.dp)
                            .coloredShadow(AppTheme.colors.primary, alpha = 0.2f, shadowRadius = 20.dp)
                            .shadow(3.dp, shape = CircleShape)
                            .animateClickable {
                                title = if (title == Res.string.english_level_second_title_fa)
                                    Res.string.english_level_second_title
                                else
                                    Res.string.english_level_second_title_fa
                            }
                            .background(AppTheme.colors.primary, CircleShape)
                            .padding(5.dp),
                        painter = painterResource(Res.drawable.ic_translate),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(AppTheme.colors.onPrimary),
                        contentDescription = "Translate",
                    )

                }
            )

            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth(),
                userScrollEnabled = languageLevel != null,
                state = pagerState
            ) { page ->
                QuestionItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = AppTheme.colors.surfaceContainerLow,
                            shape = AppTheme.shapes.roundSmall
                        )
                        .padding(16.dp),

                    item = questions[page],

                    // Replace getOrDefault with map index access and Elvis operator
                    // This approach is fully supported in Kotlin Multiplatform (Common module)
                    selectedAnswerIndex = userAnswers[questions[page].id] ?: -1,

                    showAnswers = languageLevel != null,
                    onAnswer = { answerIndex ->
                        onAction(
                            EnglishLevelUiEvent.OnAnswer(
                                questions[page],
                                answerIndex
                            )
                        )

                        // Check if there is a next page
                        if (pagerState.currentPage < questions.size - 1) {
                            cScope.launch {
                                // Add a small delay for better UX before scrolling
                                delay(100.milliseconds)
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                )
            }

            Spacer(Modifier)


        }
    }
}


@PreviewLightDark
@Composable
private fun EnglishLevelPreview() {
    AppTheme {
        EnglishLevelScreenContent(
            modifier = Modifier.baseModifier(0.dp),
            questions = QuestionUtils.getAllQuestions(),
            onAction = {},
            userAnswers = emptyMap(),
            languageLevel = null
        )
    }
}

@LightDarkPreview
@Composable
private fun EnglishLevel2Preview() {
    AppTheme {
        EnglishLevelScreenContent(
            modifier = Modifier.baseModifier(0.dp),
            questions = QuestionUtils.getAllQuestions(),
            onAction = {},
            userAnswers = emptyMap(),
            languageLevel = LanguageLevel.A1
        )
    }
}
