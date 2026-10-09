package ir.aispeaking.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.profile.component.AchievementStatsCard
import ir.aispeaking.profile.component.AvatarPickerDialog
import ir.aispeaking.profile.component.EditNickNameDialog
import ir.aispeaking.profile.component.GuestBannerCard
import ir.aispeaking.profile.component.LevelPickerBottomSheet
import ir.aispeaking.profile.component.ProfileHeaderCard
import ir.aispeaking.profile.component.SignOutConfirmDialog
import ir.aispeaking.profile.component.SubscriptionCard
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_exit
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.utils.lifecycle.OnResume

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    OnResume {
        viewModel.refresh()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Game.FallbackBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameIconButton(
                        icon = Res.drawable.ic_back,
                        onClick = onNavigateBack,
                        contentDescription = "بازگشت"
                    )
                    GameText(
                        text = "حساب کاربری",
                        size = 18.sp,
                        bold = true,
                        align = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(44.dp))
                }

                if (uiState.isLoading && uiState.user == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(color = Game.Mint, modifier = Modifier.size(40.dp))
                            GameText(text = "در حال دریافت مشخصات…", size = 13.sp, color = Game.TextSecondary)
                        }
                    }
                } else if (uiState.user != null) {
                    val user = uiState.user!!

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ProfileHeaderCard(
                            user = user,
                            onAvatarClick = { viewModel.onAvatarPickerClicked() },
                            onEditNameClick = { viewModel.onEditNameClicked() }
                        )

                        if (user.isGuest) {
                            GuestBannerCard(onLoginClick = onNavigateToLogin)
                        }

                        SubscriptionCard(
                            subscription = user.subscription,
                            onUpgradeClick = onNavigateToSubscription
                        )

                        AchievementStatsCard(
                            user = user,
                            onLevelClick = { viewModel.onLevelPickerClicked() }
                        )

                        if (!user.isGuest) {
                            Spacer(modifier = Modifier.height(4.dp))
                            GameButton(
                                text = "خروج از حساب کاربری",
                                onClick = { viewModel.onSignOutClicked() },
                                modifier = Modifier.fillMaxWidth(),
                                style = GameButtonStyle.Glass,
                                icon = Res.drawable.ic_exit,
                                height = 48.dp,
                                textSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            GameText(
                                text = uiState.errorMessage ?: "خطایی رخ داد",
                                size = 14.sp,
                                lineHeight = 22.sp,
                                color = Game.TextSecondary,
                                align = TextAlign.Center
                            )
                            GameButton(
                                text = "تلاش دوباره",
                                onClick = { viewModel.refresh() },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

            // Overlays render inside the screen so they stay within the phone frame on web.
            AvatarPickerDialog(
                visible = uiState.showAvatarPicker,
                currentAvatar = uiState.user?.avatar ?: "avatar_g1",
                onAvatarSelected = { viewModel.onAvatarSelected(it) },
                onDismiss = { viewModel.onDismissDialogs() }
            )

            EditNickNameDialog(
                visible = uiState.showEditNameDialog,
                currentNickName = uiState.user?.nickName ?: "",
                onSave = { viewModel.onSaveNickName(it) },
                onDismiss = { viewModel.onDismissDialogs() }
            )

            SignOutConfirmDialog(
                visible = uiState.showSignOutDialog,
                onConfirm = {
                    viewModel.onConfirmSignOut(onComplete = onNavigateToLogin)
                },
                onDismiss = { viewModel.onDismissDialogs() }
            )

            LevelPickerBottomSheet(
                visible = uiState.showLevelPicker,
                currentLevel = uiState.user?.languageLevel,
                onLevelSelected = { level -> viewModel.onSelectLanguageLevel(level) },
                onDismiss = { viewModel.onDismissDialogs() }
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ProfileScreenPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Game.Ink),
            contentAlignment = Alignment.Center
        ) {
            GameText(text = "پیش‌نمایش صفحه پروفایل کاربری", size = 16.sp)
        }
    }
}
