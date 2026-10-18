package ir.aispeaking.profile

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import ir.aispeaking.profile.component.ThemeSelectionCard
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_exit
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
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
    val isDark = AppTheme.isDark
    val screenBg = if (isDark) Color(0xFF0F1015) else Color(0xFFF2F4F7)

    OnResume {
        viewModel.refresh()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(screenBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Navigation Bar matching Apple sleek Liquid Glass style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right side in RTL: Circular Back Button (chevron right) + Page Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val backInteraction = remember { MutableInteractionSource() }
                        val backPressed by backInteraction.collectIsPressedAsState()
                        val backScale by animateFloatAsState(
                            targetValue = if (backPressed) 0.92f else 1f,
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                            label = "back_btn_press"
                        )

                        val backBg = if (isDark) Color(0x1AFFFFFF) else Color(0xFFFFFFFF)
                        val backBorder = if (isDark) Color(0x22FFFFFF) else Color(0x14000000)
                        val backTint = if (isDark) Game.TextPrimary else Color(0xFF111318)

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .graphicsLayer {
                                    scaleX = backScale
                                    scaleY = backScale
                                }
                                .clip(CircleShape)
                                .background(backBg)
                                .border(1.dp, backBorder, CircleShape)
                                .clickable(
                                    interactionSource = backInteraction,
                                    indication = null,
                                    onClick = onNavigateBack
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_back),
                                contentDescription = "بازگشت",
                                tint = backTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        GameText(
                            text = "پروفایل کاربری",
                            size = 18.sp,
                            bold = true,
                            color = if (isDark) Game.TextPrimary else Color(0xFF111318)
                        )
                    }
                }

                // Main Content
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
                            val accentColor = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
                            CircularProgressIndicator(color = accentColor, modifier = Modifier.size(38.dp))
                            GameText(
                                text = "در حال دریافت اطلاعات کاربر…",
                                size = 13.sp,
                                color = if (isDark) Game.TextSecondary else Color(0xFF6B7280)
                            )
                        }
                    }
                } else if (uiState.user != null) {
                    val user = uiState.user!!

                    // Scrollable Profile Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
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

                        // Apple Theme Appearance Selector (System / Dark / Light)
                        ThemeSelectionCard(
                            currentThemeMode = uiState.themeMode,
                            onThemeSelected = { viewModel.onThemeSelected(it) }
                        )

                        // Sign-out Button right below settings
                        if (!user.isGuest) {
                            AppleSignOutButton(
                                onClick = { viewModel.onSignOutClicked() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp).navigationBarsPadding())
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
                                color = if (isDark) Game.TextSecondary else Color(0xFF6B7280),
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

            // In-frame Overlays
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

/**
 * Apple-style destructive sign-out button:
 * Frosted translucent crimson container with hairline border and spring press physics.
 */
@Composable
private fun AppleSignOutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "apple_sign_out_press"
    )
    val shape = RoundedCornerShape(18.dp)

    val signOutBg = if (isDark) Color(0x1FFF453A) else Color(0x14FF3B30)
    val signOutBorder = if (isDark) Color(0x40FF453A) else Color(0x29FF3B30)
    val signOutTint = if (isDark) Game.Coral else Color(0xFFFF3B30)

    Box(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(signOutBg)
            .border(1.dp, signOutBorder, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_exit),
                contentDescription = null,
                tint = signOutTint,
                modifier = Modifier.size(18.dp)
            )
            GameText(
                text = "خروج از حساب کاربری",
                size = 14.sp,
                bold = true,
                color = signOutTint
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
                .background(Color(0xFF0F1015)),
            contentAlignment = Alignment.Center
        ) {
            GameText(text = "پیش‌نمایش صفحه پروفایل کاربری", size = 16.sp)
        }
    }
}
