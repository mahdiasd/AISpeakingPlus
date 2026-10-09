package ir.aispeaking.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_exit
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.core.button.AppTopBarIconButton
import ir.aispeaking.profile.component.AchievementStatsCard
import ir.aispeaking.profile.component.AvatarPickerDialog
import ir.aispeaking.profile.component.EditNickNameDialog
import ir.aispeaking.profile.component.GuestBannerCard
import ir.aispeaking.profile.component.LevelPickerBottomSheet
import ir.aispeaking.profile.component.ProfileHeaderCard
import ir.aispeaking.profile.component.SignOutConfirmDialog
import ir.aispeaking.profile.component.SubscriptionCard
import ir.aispeaking.sharedui.utils.lifecycle.OnResume
import org.jetbrains.compose.resources.painterResource

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
                .background(Color(0xFF070B19))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppBackButton(onClick = onNavigateBack)

                    Text(
                        text = "حساب کاربری",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Right action: Sign out or spacer
                    if (!uiState.isGuest) {
                        AppTopBarIconButton(
                            icon = Res.drawable.ic_exit,
                            onClick = { viewModel.onSignOutClicked() },
                            contentDescription = "Sign Out",
                            tint = Color(0xFFEF4444),
                            borderColor = Color(0x44EF4444)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(38.dp))
                    }
                }

                if (uiState.isLoading && uiState.user == null) {
                    // Loading State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Color(0xFF6366F1),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "در حال دریافت مشخصات...",
                                color = Color(0xFFA5B4FC),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else if (uiState.user != null) {
                    val user = uiState.user!!
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Profile Header
                        ProfileHeaderCard(
                            user = user,
                            onAvatarClick = { viewModel.onAvatarPickerClicked() },
                            onEditNameClick = { viewModel.onEditNameClicked() }
                        )

                        // 2. Guest Banner if Guest
                        if (user.isGuest) {
                            GuestBannerCard(
                                onLoginClick = onNavigateToLogin
                            )
                        }

                        // 3. Subscription Status
                        SubscriptionCard(
                            subscription = user.subscription,
                            onUpgradeClick = onNavigateToSubscription
                        )

                        // 4. Learning Achievements
                        AchievementStatsCard(
                            user = user,
                            onLevelClick = { viewModel.onLevelPickerClicked() }
                        )

                        // 5. Sign Out Button for Logged-In Users
                        if (!user.isGuest) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x11EF4444))
                                    .border(1.dp, Color(0x33EF4444), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_exit),
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "خروج از حساب کاربری",
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.onSignOutClicked() },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 14.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = "خروج",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else if (uiState.errorMessage != null) {
                    // Error State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "خطایی رخ داد",
                                color = Color(0xFFFCA5A5),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.refresh() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                            ) {
                                Text(text = "تلاش مجدد", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Dialogs
            if (uiState.showAvatarPicker) {
                AvatarPickerDialog(
                    currentAvatar = uiState.user?.avatar ?: "avatar_g1",
                    onAvatarSelected = { viewModel.onAvatarSelected(it) },
                    onDismiss = { viewModel.onDismissDialogs() }
                )
            }

            if (uiState.showEditNameDialog) {
                EditNickNameDialog(
                    currentNickName = uiState.user?.nickName ?: "",
                    onSave = { viewModel.onSaveNickName(it) },
                    onDismiss = { viewModel.onDismissDialogs() }
                )
            }

            if (uiState.showSignOutDialog) {
                SignOutConfirmDialog(
                    onConfirm = {
                        viewModel.onConfirmSignOut(onComplete = onNavigateToLogin)
                    },
                    onDismiss = { viewModel.onDismissDialogs() }
                )
            }

            if (uiState.showLevelPicker) {
                LevelPickerBottomSheet(
                    currentLevel = uiState.user?.languageLevel,
                    onLevelSelected = { level ->
                        viewModel.onSelectLanguageLevel(level)
                    },
                    onDismiss = { viewModel.onDismissDialogs() }
                )
            }
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
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "پیش‌نمایش صفحه پروفایل کاربری",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
