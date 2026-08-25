package ir.aispeaking.roadmap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.roadmap.component.LevelButton
import ir.aispeaking.roadmap.model.LevelData
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ic_roadmap_dashline_1
import ir.aispeaking.sharedui.ic_roadmap_dashline_2
import ir.aispeaking.sharedui.ic_roadmap_dashline_3
import ir.aispeaking.sharedui.ic_roadmap_dashline_4
import ir.aispeaking.sharedui.ic_roadmap_dashline_5
import ir.aispeaking.sharedui.ic_roadmap_dashline_6
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.core.unauthorized.UnauthorizedContent
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.PointsColor
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import ir.aispeaking.sharedui.utils.lifecycle.OnResume
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RoadmapScreen(
    vm: RoadmapViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigateToLogin: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    OnResume {
        vm.onTriggerEvent(RoadmapUiEvent.FetchUser)
    }


    if (uiState.user != null) {
        RoadmapScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.roadMapSurface),
            user = uiState.user,
            levels = uiState.levels,
            onAction = { vm.onTriggerEvent(it) }
        )
    } else {
        UnauthorizedContent(
            modifier = Modifier.baseModifier(),
            navigateToLogin = navigateToLogin
        )
    }

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is RoadmapUiNavigation.ToBack -> navigateBack()
        }
    }
}


@Composable
fun RoadmapScreenContent(
    modifier: Modifier = Modifier,
    onAction: OnAction,
    levels: ImmutableList<LevelData> = immutableListOf(),
    user: User?,
) {
    val cScope = rememberCoroutineScope()
    val state = rememberLazyListState()
    val userLevel by remember(levels, user?.score) {
        derivedStateOf { levels.findLast { it.isPassed }?.level ?: -1 }
    }

    LaunchedEffect(levels) {
        delay(200)
        cScope.launch { state.animateScrollToItem(if (userLevel - 2 < 0) 0 else userLevel - 2) }
    }

    val dashLineDrawables = remember {
        mutableStateListOf(
            Res.drawable.ic_roadmap_dashline_1,
            Res.drawable.ic_roadmap_dashline_2,
            Res.drawable.ic_roadmap_dashline_3,
            Res.drawable.ic_roadmap_dashline_4,
            Res.drawable.ic_roadmap_dashline_5,
            Res.drawable.ic_roadmap_dashline_6,
        )
    }

    Box(
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            reverseLayout = true,
            state = state
        ) {
            itemsIndexed(levels) { index, item ->
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    val (dashRef, levelRef) = createRefs()
                    val dashLineDrawable by remember(index) { derivedStateOf { dashLineDrawables[(index % dashLineDrawables.size)] } }

                    Image(
                        modifier = Modifier
                            .constrainAs(dashRef)
                            {
                                top.linkTo(parent.top)
                            }
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(80.dp),
                        painter = painterResource(dashLineDrawable),
                        contentDescription = "",
                        contentScale = ContentScale.FillBounds,
                        colorFilter = ColorFilter.tint(color = AppTheme.colors.onPrimary)
                    )

                    when (userLevel) {
                        item.level -> {
                            AvatarUtils.findAvatarByNameComposable(user?.avatar)?.let { profileAvatar ->
                                Column(
                                    modifier = Modifier
                                        .constrainAs(levelRef) {
                                            top.linkTo(dashRef.bottom)
                                            bottom.linkTo(dashRef.bottom)
                                            if (index % 2 == 0)
                                                start.linkTo(dashRef.start)
                                            else
                                                end.linkTo(dashRef.end)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy((-8).dp, alignment = Alignment.CenterVertically)
                                ) {
                                    Image(
                                        modifier = Modifier
                                            .size(75.dp)
                                            .clip(CircleShape)
                                            .shadow(1.dp)
                                            .background(AppTheme.colors.outline, shape = CircleShape)
                                            .border(8.dp, color = AppTheme.colors.secondary, shape = CircleShape)
                                            .padding(top = 8.dp),
                                        painter = painterResource(profileAvatar.drawable),
                                        contentScale = ContentScale.FillHeight,
                                        contentDescription = "User avatar"
                                    )
                                    DualContentRow(
                                        modifier = Modifier
                                            .background(color = AppTheme.colors.secondary, shape = AppTheme.shapes.roundMedium)
                                            .padding(horizontal = 24.dp, vertical = 4.dp),
                                        leftContent = {
                                            BodyMediumBoldText(
                                                text = user?.score.toString(), color = AppTheme.colors.onPrimary
                                            )
                                        },
                                        rightContent = {
                                            AppIcon(
                                                size = 12.dp,
                                                icon = Res.drawable.ic_points,
                                                tint = PointsColor
                                            )
                                        },
                                    )
                                }
                            }
                        }

                        else -> {
                            LevelButton(
                                modifier = Modifier
                                    .constrainAs(levelRef) {
                                        top.linkTo(dashRef.bottom)
                                        bottom.linkTo(dashRef.bottom)
                                        if (index % 2 == 0)
                                            start.linkTo(dashRef.start)
                                        else
                                            end.linkTo(dashRef.end)
                                    },
                                item = item
                            )
                        }
                    }

                    if (item.level == 1) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .constrainAs(createRef()) {
                                    top.linkTo(levelRef.bottom)
                                }
                                .height(50.dp),
                        )
                    }


                }
            }
        }
    }

}


@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        RoadmapScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.roadMapSurface),
            user = FakeData.provideUsers().first(),
            levels = List(10)
            { levelIndex ->
                val level = levelIndex + 1
                val min = (level * 50) - 49
                val max = (level * 50)

                LevelData(
                    level = level,
                    title = "Level $level",
                    rangeText = "$min - $max",
                    isPassed = 120 in min..max
                )
            }.toImmutableList(),
            onAction = {}
        )
    }
}




