package ir.aispeaking.main.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.main.component.AtmosphericFantasyBackground
import ir.aispeaking.main.component.CenterStage
import ir.aispeaking.main.component.GamifiedArcBottomDock
import ir.aispeaking.main.component.LevelDetailDialog
import ir.aispeaking.main.component.StreakDetailDialog
import ir.aispeaking.main.component.TopGamingBar
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun CharacterHomeScreen(
    modifier: Modifier = Modifier,
    user: User? = null,
    level: Int = 3,
    currentXp: Int = 750,
    maxXp: Int = 1000,
    streakDays: Int = 5,
    gemsCount: Int = 120,
    activeLevelTitle: String = "مرحله ۴",
    speechText: String = "سلام علی! آماده‌ای برای ماجراجویی مرحله ۴؟",
    onNavigateToMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToLightener: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onDialogueAudioClick: () -> Unit = {},
) {
    var showStreakDialog by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }

    val characterName = remember(user) {
        user?.nickName?.ifBlank { null }
            ?: user?.firstName?.ifBlank { null }
            ?: "ایندی"
    }

    val characterSubtitle = remember(level) {
        "کاوشگر سطح $level"
    }

    AtmosphericFantasyBackground(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Main Content Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 1. Top Status Bar (Gamified Header)
                TopGamingBar(
                    modifier = Modifier.fillMaxWidth(),
                    level = level,
                    currentXp = currentXp,
                    maxXp = maxXp,
                    streakDays = streakDays,
                    gemsCount = gemsCount,
                    onLevelClick = { showLevelDialog = true },
                    onStreakClick = { showStreakDialog = true },
                    onGemsClick = onNavigateToStore,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Center Stage (Avatar, 3D Podium, Speech Bubble & Name)
                CenterStage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    characterName = characterName,
                    characterSubtitle = characterSubtitle,
                    userAvatarName = user?.avatar,
                    speechText = speechText,
                    onDialogueAudioClick = onDialogueAudioClick,
                    onAvatarClick = onDialogueAudioClick,
                )

                // Bottom spacer to ensure center stage content clears the floating arc dock
                Spacer(modifier = Modifier.height(120.dp))
            }

            // 3. Floating Bottom Gamified Arc Dock
            GamifiedArcBottomDock(
                modifier = Modifier.align(Alignment.BottomCenter),
                activeLevelTitle = activeLevelTitle,
                onProfileClick = onNavigateToProfile,
                onLeaderboardClick = onNavigateToLeaderboard,
                onMainPlayClick = onNavigateToMap,
                onLightenerClick = onNavigateToLightener,
                onStoreClick = onNavigateToStore,
            )
        }
    }

    // Dialogs
    if (showStreakDialog) {
        StreakDetailDialog(
            streakDays = streakDays,
            onDismiss = { showStreakDialog = false },
        )
    }

    if (showLevelDialog) {
        LevelDetailDialog(
            level = level,
            currentXp = currentXp,
            maxXp = maxXp,
            onDismiss = { showLevelDialog = false },
        )
    }
}

@LightDarkPreview
@Composable
private fun CharacterHomeScreenPreview() {
    AppTheme {
        CharacterHomeScreen(
            level = 3,
            currentXp = 750,
            maxXp = 1000,
            streakDays = 5,
            gemsCount = 120,
            activeLevelTitle = "مرحله ۴",
            speechText = "سلام علی! آماده‌ای برای ماجراجویی مرحله ۴؟",
        )
    }
}
