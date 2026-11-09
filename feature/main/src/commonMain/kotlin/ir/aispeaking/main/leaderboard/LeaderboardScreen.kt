package ir.aispeaking.main.leaderboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ir.aispeaking.domain.model.stage.LeaderboardEntry
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.core.loading.AppLoadingIndicator
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Leaderboard Screen for AISpeakingPlus.
 *
 * Implements:
 * - Frosted Liquid Glass HUD Header with RTL Back button.
 * - Tiered 3D Podium for Top 3 learners (Gold #1 center, Silver #2 left, Bronze #3 right)
 *   with engraved 3D blocks, avatar medals, and star score capsules.
 * - Translucent glass list for Rank 4+ with rank numbers, avatars, and star indicators.
 * - Sticky bottom user standing bar showing personal rank, stars, and progress.
 */
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = AppTheme.isDark
    val listState = rememberLazyListState()

    val leaderboard = uiState.leaderboard
    val items = leaderboard?.items.orEmpty()
    val top1 = items.firstOrNull { it.rank == 1 } ?: items.getOrNull(0)
    val top2 = items.firstOrNull { it.rank == 2 } ?: items.getOrNull(1)
    val top3 = items.firstOrNull { it.rank == 3 } ?: items.getOrNull(2)
    val remainingItems = items.filter { it.rank > 3 }

    val currentUserRank = leaderboard?.currentUserRank

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF131522),
                            Color(0xFF0C0E18),
                            Color(0xFF06070D)
                        )
                    )
                )
        ) {
            // Ambient Top Spotlight Glow on the Podium
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x334F46E5),
                                Color(0x1A2563EB),
                                Color.Transparent
                            ),
                            radius = 450f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header Bar (RTL Start anchor Back Button, Centered Title)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppBackButton(
                        onClick = onNavigateBack,
                        backgroundColor = if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF),
                        borderColor = Color(0x38FFFFFF)
                    )

                    GameText(
                        text = "جدول برترین‌ها",
                        size = 18.sp,
                        bold = true,
                        color = Color.White,
                        align = TextAlign.Center
                    )

                    // Symmetrical balance spacer
                    Box(modifier = Modifier.size(38.dp))
                }

                when {
                    uiState.isLoading && leaderboard == null -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                AppLoadingIndicator(
                                    size = 40.dp,
                                    color = Color(0xFF6366F1)
                                )
                                GameText(
                                    text = "در حال بارگذاری رده‌بندی مسافران…",
                                    size = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    uiState.errorMessage != null && leaderboard == null -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                GameText(
                                    text = uiState.errorMessage.orEmpty(),
                                    size = 14.sp,
                                    color = Color(0xFFFF6B6B),
                                    align = TextAlign.Center
                                )
                                GameButton(
                                    text = "تلاش مجدد",
                                    onClick = { viewModel.loadLeaderboard(isRefresh = true) },
                                    icon = Res.drawable.ic_refresh,
                                    height = 42.dp,
                                    textSize = 13.sp
                                )
                            }
                        }
                    }

                    else -> {
                        // Main Content: Top 3 Podium + Rank 4+ List
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Top 3 Podium
                            TopThreePodium(
                                first = top1,
                                second = top2,
                                third = top3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bottom Glass Card for Ranks 4+
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                                    .background(Color(0xD9141724))
                                    .border(
                                        1.dp,
                                        Brush.verticalGradient(
                                            listOf(Color(0x38FFFFFF), Color(0x0DFFFFFF))
                                        ),
                                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                                    )
                            ) {
                                if (remainingItems.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            GameText(
                                                text = "جایگاه‌های بعدی در انتظار شماست!",
                                                size = 14.sp,
                                                bold = true,
                                                color = Color.White,
                                                align = TextAlign.Center
                                            )
                                            GameText(
                                                text = "با پیشروی در مراحل داستانی، اولین نفری باش که این رتبه‌ها را تصاحب می‌کند.",
                                                size = 12.sp,
                                                lineHeight = 20.sp,
                                                color = Color(0xFF94A3B8),
                                                align = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(
                                            top = 12.dp,
                                            bottom = if (currentUserRank != null || onNavigateToLogin != null) 90.dp else 24.dp
                                        )
                                    ) {
                                        items(
                                            items = remainingItems,
                                            key = { it.userId }
                                        ) { entry ->
                                            val isCurrentUser = entry.userId == currentUserRank?.userId
                                            LeaderboardRowItem(
                                                entry = entry,
                                                isCurrentUser = isCurrentUser
                                            )
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 20.dp),
                                                color = Color(0x14FFFFFF),
                                                thickness = 0.75.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pinned Floating Current User Standing Bar (anchored at bottom)
            if (currentUserRank != null) {
                CurrentUserStickyBar(
                    userRank = currentUserRank,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            } else if (onNavigateToLogin != null && leaderboard != null) {
                GuestLoginPromptBar(
                    onLoginClick = onNavigateToLogin,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

/**
 * 3D Tiered Podium displaying the Top 3 Learners.
 * Layout: Rank 2 (Silver / Left), Rank 1 (Gold / Center - Tallest), Rank 3 (Bronze / Right).
 */
@Composable
private fun TopThreePodium(
    first: LeaderboardEntry?,
    second: LeaderboardEntry?,
    third: LeaderboardEntry?,
    modifier: Modifier = Modifier
) {
    // We compose the 3 podium columns LTR so 2 is on the Left, 1 in the Center, 3 on the Right (Olympic format)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            // Rank 2 (Silver)
            PodiumColumn(
                rank = 2,
                entry = second,
                pedestalHeight = 90.dp,
                pedestalWidth = 98.dp,
                avatarSize = 56.dp,
                ringColor = Color(0xFFC0C0C0),
                badgeColor = Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Rank 1 (Gold) - Champion
            PodiumColumn(
                rank = 1,
                entry = first,
                pedestalHeight = 130.dp,
                pedestalWidth = 112.dp,
                avatarSize = 68.dp,
                ringColor = Color(0xFFFFD700),
                badgeColor = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFF59E0B))),
                modifier = Modifier.weight(1.15f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Rank 3 (Bronze)
            PodiumColumn(
                rank = 3,
                entry = third,
                pedestalHeight = 72.dp,
                pedestalWidth = 94.dp,
                avatarSize = 52.dp,
                ringColor = Color(0xFFCD7F32),
                badgeColor = Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFF92400E))),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Single 3D podium pedestal with avatar, rank medal badge, name, score pill, and engraved pedestal.
 */
@Composable
private fun PodiumColumn(
    rank: Int,
    entry: LeaderboardEntry?,
    pedestalHeight: Dp,
    pedestalWidth: Dp,
    avatarSize: Dp,
    ringColor: Color,
    badgeColor: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Avatar + Medal Badge
        Box(
            modifier = Modifier.padding(bottom = 6.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            if (entry != null) {
                LeaderboardAvatar(
                    name = entry.displayName,
                    avatarUrl = entry.avatarUrl,
                    size = avatarSize,
                    borderColor = ringColor,
                    borderWidth = if (rank == 1) 2.5.dp else 2.dp
                )
            } else {
                // Empty placeholder ring
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(Color(0x26FFFFFF))
                        .border(1.5.dp, ringColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GameText(
                        text = "—",
                        size = 18.sp,
                        bold = true,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Medal Rank Badge
            Box(
                modifier = Modifier
                    .offset(x = 6.dp, y = (-4).dp)
                    .size(if (rank == 1) 24.dp else 20.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GameText(
                    text = "$rank",
                    size = if (rank == 1) 12.sp else 10.sp,
                    bold = true,
                    color = Color(0xFF0F111A)
                )
            }
        }

        // Learner Display Name
        GameText(
            text = entry?.displayName ?: "هنوز خالی",
            size = if (rank == 1) 13.sp else 11.sp,
            bold = true,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (entry != null) Color.White else Color(0xFF94A3B8),
            align = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Star Score Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x334F46E5))
                .border(0.75.dp, Color(0x666366F1), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_star),
                    contentDescription = null,
                    tint = if (rank == 1) Color(0xFFFFD700) else Color(0xFFFCD34D),
                    modifier = Modifier.size(13.dp)
                )
                GameText(
                    text = "${entry?.totalStars?.fa() ?: 0.fa()}",
                    size = 11.sp,
                    bold = true,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3D Engraved Pedestal Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF333748),
                            Color(0xFF232634),
                            Color(0xFF161822)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            ringColor.copy(alpha = if (rank == 1) 0.6f else 0.4f),
                            Color(0x1AFFFFFF)
                        )
                    ),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Isometric Top highlight rim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x40FFFFFF), Color.Transparent)
                        )
                    )
            )

            // Large Engraved / Embossed Rank Number
            GameText(
                text = "$rank",
                size = if (rank == 1) 56.sp else 44.sp,
                bold = true,
                color = Color.White.copy(alpha = if (rank == 1) 0.22f else 0.16f),
                align = TextAlign.Center
            )
        }
    }
}

/**
 * Single Row Item for Ranks 4 to N.
 */
@Composable
private fun LeaderboardRowItem(
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isCurrentUser) Color(0x334F46E5) else Color.Transparent
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Rank Number
            GameText(
                text = entry.rank.fa(),
                size = 14.sp,
                bold = true,
                color = if (isCurrentUser) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                modifier = Modifier.width(28.dp),
                align = TextAlign.Center
            )

            // Avatar
            LeaderboardAvatar(
                name = entry.displayName,
                avatarUrl = entry.avatarUrl,
                size = 42.dp,
                borderColor = if (isCurrentUser) Color(0xFF38BDF8) else Color(0x28FFFFFF)
            )

            // Name and Completed Stages
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GameText(
                        text = entry.displayName,
                        size = 14.sp,
                        bold = true,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isCurrentUser) Color(0xFF38BDF8) else Color.White
                    )
                    if (isCurrentUser) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3338BDF8))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            GameText(
                                text = "شما",
                                size = 9.sp,
                                bold = true,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
                GameText(
                    text = "${entry.completedStages.fa()} مرحله تکمیل‌شده",
                    size = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Score Star Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x296366F1))
                .border(0.75.dp, Color(0x406366F1), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_star),
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(13.dp)
                )
                GameText(
                    text = entry.totalStars.fa(),
                    size = 12.sp,
                    bold = true,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Pinned Glass HUD Bar at the bottom showing the current user's personal rank.
 */
@Composable
private fun CurrentUserStickyBar(
    userRank: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            .background(Color(0xF2181B2A))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.7f),
                        Color(0xFF6366F1).copy(alpha = 0.7f)
                    )
                ),
                barShape
            )
            .shadow(16.dp, barShape, ambientColor = Color(0x66000000), spotColor = Color(0x8038BDF8))
            .padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF4F46E5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    GameText(
                        text = "#${userRank.rank.fa()}",
                        size = 12.sp,
                        bold = true,
                        color = Color.White
                    )
                }

                LeaderboardAvatar(
                    name = userRank.displayName,
                    avatarUrl = userRank.avatarUrl,
                    size = 38.dp,
                    borderColor = Color(0xFF38BDF8)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GameText(
                            text = "جایگاه شما",
                            size = 11.sp,
                            color = Color(0xFF38BDF8),
                            bold = true
                        )
                        GameText(
                            text = "(${userRank.displayName})",
                            size = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    GameText(
                        text = "رتبه ${userRank.rank.fa()} مسافران",
                        size = 13.sp,
                        bold = true,
                        color = Color.White
                    )
                }
            }

            // User Stars
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x330284C7))
                    .border(0.75.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_star),
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    GameText(
                        text = "${userRank.totalStars.fa()} ستاره",
                        size = 12.sp,
                        bold = true,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Guest Login Prompt Bar encouraging guest learners to register to get ranked.
 */
@Composable
private fun GuestLoginPromptBar(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xF2181B2A))
            .border(1.dp, Color(0x336366F1), shape)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GameText(
                    text = "ثبت نام در رده‌بندی",
                    size = 13.sp,
                    bold = true,
                    color = Color.White
                )
                GameText(
                    text = "برای حضور در جدول رقابت و ثبت ستاره‌ها، وارد شوید.",
                    size = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            GameButton(
                text = "ورود / ثبت‌نام",
                onClick = onLoginClick,
                height = 36.dp,
                textSize = 12.sp
            )
        }
    }
}

/**
 * Universal Avatar helper that checks local preset avatars (B-1..12, G-1..12), URLs, and initial letters.
 */
@Composable
private fun LeaderboardAvatar(
    name: String,
    avatarUrl: String?,
    size: Dp,
    borderColor: Color,
    borderWidth: Dp = 2.dp,
    modifier: Modifier = Modifier
) {
    val localAvatar = remember(avatarUrl) { AvatarUtils.findAvatarByName(avatarUrl) }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(Color(0xFF282B3E), Color(0xFF161824)))
            )
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (localAvatar != null) {
            Image(
                painter = painterResource(localAvatar.drawable),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            GameText(
                text = name.trim().split(" ").lastOrNull { it.isNotBlank() }?.firstOrNull()?.uppercase() ?: "A",
                size = (size.value * 0.42f).sp,
                bold = true,
                latin = true,
                color = Color.White
            )
        }
    }
}
