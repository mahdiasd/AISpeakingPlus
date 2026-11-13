package ir.aispeaking.stageslist.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_private
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameAvatar
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameStars
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * Redesigned Apple Frosted Glass Stage Item Card:
 * - Direct visual connection to cartoon scenery & characters
 * - Apple Liquid Glass surface with specular reflection
 * - Apple spring physics on press (damping 0.82, stiffness 450)
 * - Journey timeline with glowing active pulse & star nodes
 */
@Composable
fun StageListItemCard(
    stage: Stage,
    isActiveCurrentStage: Boolean,
    onStageClick: (Stage) -> Unit,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val isLocked = stage.lockStatus != StageLockStatus.UNLOCKED
    val starsEarned = stage.userProgress?.stars ?: 0
    val hasPlayed = starsEarned > 0

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Apple fluid spring press feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f),
        label = "stageAppleSpringScale"
    )

    val isDark = AppTheme.isDark

    // Infinite transition for active node pulse
    val infiniteTransition = rememberInfiniteTransition(label = "activeStagePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Timeline Track + Node
        Box(
            modifier = Modifier
                .width(46.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Connecting vertical road track
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(
                        top = if (isFirst) 30.dp else 0.dp,
                        bottom = if (isLast) 20.dp else 0.dp
                    )
                    .width(3.dp)
                    .background(
                        when {
                            isLast -> Brush.verticalGradient(
                                listOf(
                                    if (hasPlayed) Game.Gold.copy(alpha = 0.6f) else Game.Stroke,
                                    Color.Transparent
                                )
                            )
                            hasPlayed -> Brush.verticalGradient(
                                listOf(Game.Gold.copy(alpha = 0.6f), Game.Gold.copy(alpha = 0.6f))
                            )
                            isActiveCurrentStage -> Brush.verticalGradient(
                                listOf(Game.Blue.copy(alpha = 0.7f), Game.Stroke)
                            )
                            else -> Brush.verticalGradient(
                                listOf(Game.Stroke, Game.Stroke)
                            )
                        }
                    )
            )

            // Timeline Node Circle
            Box(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isActiveCurrentStage) {
                    // Pulsing Apple Blue halo ring
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Game.Blue.copy(alpha = pulseAlpha))
                    )
                }

                // Core Node Disc
                Box(
                    modifier = Modifier
                        .size(if (isActiveCurrentStage) 38.dp else 34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActiveCurrentStage -> Brush.linearGradient(
                                    listOf(Game.Blue, Color(0xFF0056B3))
                                )
                                hasPlayed -> Brush.linearGradient(
                                    listOf(Game.Gold, Game.GoldDeep)
                                )
                                stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> Brush.linearGradient(
                                    listOf(Color(0xFF3A3010), Color(0xFF221A06))
                                )
                                isLocked -> if (isDark) {
                                    Brush.linearGradient(listOf(Color(0xFF2A2D3A), Color(0xFF191B24)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB)))
                                }
                                else -> Brush.linearGradient(
                                    listOf(Game.Sky, Color(0xFF006699))
                                )
                            }
                        )
                        .border(
                            width = if (isActiveCurrentStage) 2.dp else 1.2.dp,
                            color = when {
                                isActiveCurrentStage -> Color.White
                                hasPlayed -> Color.White.copy(alpha = 0.6f)
                                stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> Game.Gold.copy(alpha = 0.6f)
                                else -> if (isDark) Color(0x3DFFFFFF) else Color(0x26000000)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isActiveCurrentStage -> Icon(
                            painter = painterResource(Res.drawable.ic_play),
                            contentDescription = "مرحله فعال",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        hasPlayed -> Icon(
                            painter = painterResource(Res.drawable.ic_star),
                            contentDescription = "انجام شده",
                            tint = Color(0xFF1F1600),
                            modifier = Modifier.size(16.dp)
                        )
                        stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> Icon(
                            painter = painterResource(Res.drawable.ic_crown),
                            contentDescription = "اشتراک ویژه",
                            tint = Game.Gold,
                            modifier = Modifier.size(15.dp)
                        )
                        isLocked -> Icon(
                            painter = painterResource(Res.drawable.ic_private),
                            contentDescription = "قفل",
                            tint = Game.TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        else -> GameText(
                            text = stage.orderIndex.fa(),
                            size = 14.sp,
                            bold = true,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Apple Frosted Glass Card
        val cardShape = RoundedCornerShape(22.dp)
        val specularBorder = Brush.verticalGradient(
            when {
                isActiveCurrentStage -> listOf(Game.Blue, Game.Blue.copy(alpha = 0.45f))
                hasPlayed -> if (isDark) listOf(Game.Gold.copy(alpha = 0.6f), Color(0x18FFFFFF)) else listOf(Game.Gold.copy(alpha = 0.5f), Color(0x1F000000))
                else -> if (isDark) listOf(Color(0x59FFFFFF), Color(0x14FFFFFF)) else listOf(Color(0x99FFFFFF), Color(0x1F000000))
            }
        )

        val cardBg = when {
            isActiveCurrentStage -> if (isDark) Color(0xEB1D2235) else Color(0xF2F0F6FF)
            hasPlayed -> if (isDark) Color(0xD9181B26) else Color(0xEBFFFFFF)
            isLocked -> if (isDark) Color(0xA613151F) else Color(0xC2F3F4F6)
            else -> if (isDark) Color(0xD9181A25) else Color(0xEBFFFFFF)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .scale(scale)
                .clip(cardShape)
                .background(cardBg)
                .border(
                    width = if (isActiveCurrentStage) 1.6.dp else 1.dp,
                    brush = specularBorder,
                    shape = cardShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onStageClick(stage) }
                )
                .padding(14.dp)
                .alpha(if (isLocked && !isActiveCurrentStage) 0.8f else 1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Character Avatar & Name + Status Badge (Active / Stars)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Character identity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameAvatar(
                        name = stage.characterName,
                        imageUrl = stage.characterAvatarUrl,
                        size = 28.dp,
                        ring = when {
                            isActiveCurrentStage -> Game.Blue
                            hasPlayed -> Game.Gold.copy(alpha = 0.85f)
                            else -> Game.Stroke
                        }
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        GameText(
                            text = stage.characterName,
                            size = 12.sp,
                            latin = true,
                            bold = true,
                            color = if (hasPlayed) Game.Gold else Game.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Active or Stars badge
                when {
                    isActiveCurrentStage -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Game.Blue.copy(alpha = if (isDark) 0.22f else 0.12f))
                                .border(1.dp, Game.Blue.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            GameText(
                                text = "مرحله فعال شما",
                                size = 11.sp,
                                bold = true,
                                color = Game.Blue
                            )
                        }
                    }
                    hasPlayed -> {
                        GameStars(stars = starsEarned, size = 17.dp, spacing = 2.dp)
                    }
                    stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_crown),
                                contentDescription = null,
                                tint = Game.Gold,
                                modifier = Modifier.size(13.dp)
                            )
                            GameText(
                                text = "اشتراک ویژه",
                                size = 11.sp,
                                bold = true,
                                color = Game.Gold
                            )
                        }
                    }
                }
            }

            // Middle Section: Stage Cartoon Thumbnail + Persian Title & Briefing
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cartoon Scene Artwork Preview Thumbnail
                val thumbShape = RoundedCornerShape(14.dp)
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(thumbShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E2235), Color(0xFF141624))
                            )
                        )
                        .border(
                            1.dp,
                            if (isActiveCurrentStage) Game.Blue.copy(alpha = 0.7f) else Game.StrokeStrong,
                            thumbShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (stage.backgroundUrl.isNotBlank()) {
                        AsyncImage(
                            model = stage.backgroundUrl,
                            contentDescription = stage.titleFa,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Scrim overlay with lock icon if stage is locked
                    if (isLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION) Res.drawable.ic_crown
                                    else Res.drawable.ic_private
                                ),
                                contentDescription = null,
                                tint = if (stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION) Game.Gold else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Title and Briefing
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    GameText(
                        text = stage.titleFa,
                        size = 15.sp,
                        lineHeight = 22.sp,
                        bold = true,
                        color = if (isLocked) Game.TextSecondary else Game.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val briefingText = stage.briefingFa.ifBlank { stage.title }
                    GameText(
                        text = briefingText,
                        size = 12.sp,
                        lineHeight = 18.sp,
                        color = Game.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Row: Target objective info & Action CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Secondary hint/stats on start side
                val targetText = when {
                    hasPlayed -> "بهترین امتیاز: ${(stage.userProgress?.bestScore ?: 0).fa()}"
                    isActiveCurrentStage -> "آماده مکالمه صوتی و متنی"
                    stage.lockStatus == StageLockStatus.LOCKED_PREVIOUS_STAGE -> "نیاز به اتمام مرحله قبل"
                    stage.lockStatus == StageLockStatus.LOCKED_REGISTRATION -> "ورود به حساب لازم است"
                    stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> "دسترسی با اشتراک ویژه"
                    else -> "مرحله آزاد"
                }

                GameText(
                    text = targetText,
                    size = 11.sp,
                    color = if (isActiveCurrentStage) Game.Sky else Game.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Prominent Apple CTA Button / Chip
                when {
                    isActiveCurrentStage -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Game.Blue, Color(0xFF0056B3))
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_play),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                GameText(
                                    text = "ادامه مکالمه",
                                    size = 12.sp,
                                    bold = true,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> GameChip(
                        text = "ویژه اشتراک",
                        icon = Res.drawable.ic_crown,
                        accent = Game.Gold,
                        active = true
                    )
                    stage.lockStatus == StageLockStatus.LOCKED_REGISTRATION -> GameChip(
                        text = "ورود به حساب",
                        accent = Game.Violet,
                        active = true
                    )
                    stage.lockStatus == StageLockStatus.LOCKED_PREVIOUS_STAGE -> GameChip(
                        text = "قفل",
                        icon = Res.drawable.ic_private,
                        accent = Game.TextSecondary
                    )
                    hasPlayed -> GameChip(
                        text = "تکرار مرحله",
                        icon = Res.drawable.ic_refresh,
                        accent = Game.Sky
                    )
                    else -> GameChip(
                        text = "شروع",
                        icon = Res.drawable.ic_play,
                        accent = Game.Sky
                    )
                }
            }
        }
    }
}
