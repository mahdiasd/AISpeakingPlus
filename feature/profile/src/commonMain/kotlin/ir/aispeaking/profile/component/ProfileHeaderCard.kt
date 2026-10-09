package ir.aispeaking.profile.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.avatar_g1
import ir.aispeaking.sharedui.ic_camera
import ir.aispeaking.sharedui.ic_edit
import ir.aispeaking.sharedui.ic_phone
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.toFaDigits
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfileHeaderCard(
    user: UserProfile,
    onAvatarClick: () -> Unit,
    onEditNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarResource = AvatarUtils.provideProfileAvatars().find {
        it.name.equals(user.avatar, ignoreCase = true) ||
        user.avatar.contains(it.name.replace("-", "").lowercase()) ||
        user.avatar.contains(it.name.lowercase())
    }?.drawable ?: Res.drawable.avatar_g1

    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF24252B),
                        Game.PanelSolid
                    )
                )
            )
            .border(1.dp, Game.StrokeStrong, shape)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile Avatar with Apple spring press feedback
            val avatarInteraction = remember { MutableInteractionSource() }
            val avatarPressed by avatarInteraction.collectIsPressedAsState()
            val avatarScale by animateFloatAsState(
                targetValue = if (avatarPressed) 0.94f else 1f,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                label = "apple_avatar_press"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = avatarScale
                        scaleY = avatarScale
                    }
                    .clickable(
                        interactionSource = avatarInteraction,
                        indication = null,
                        onClick = onAvatarClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Multi-ring Apple depth
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Game.PanelRaised)
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(Game.Violet, Game.Sky.copy(alpha = 0.6f))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(avatarResource),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                }

                // Apple-style camera/edit overlay badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Game.Blue, Color(0xFF0066D6))))
                        .border(2.dp, Game.PanelSolid, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_camera),
                        contentDescription = "Edit Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info (Name + Phone / Guest status)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Name + Edit button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameText(
                        text = user.displayName,
                        color = Game.TextPrimary,
                        size = 19.sp,
                        bold = true,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Tactile Edit Button
                    val editInteraction = remember { MutableInteractionSource() }
                    val editPressed by editInteraction.collectIsPressedAsState()
                    val editScale by animateFloatAsState(
                        targetValue = if (editPressed) 0.90f else 1f,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f),
                        label = "apple_edit_press"
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .graphicsLayer {
                                scaleX = editScale
                                scaleY = editScale
                            }
                            .clip(CircleShape)
                            .background(Color(0x1FFFFFFF))
                            .border(1.dp, Game.Stroke, CircleShape)
                            .clickable(
                                interactionSource = editInteraction,
                                indication = null,
                                onClick = onEditNameClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_edit),
                            contentDescription = "Edit Nickname",
                            tint = Game.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Metadata tags: Phone / Guest status + CEFR Level
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!user.isGuest && user.phoneNumber.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x12FFFFFF))
                                .border(1.dp, Game.Stroke, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_phone),
                                contentDescription = null,
                                tint = Game.TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            GameText(
                                text = user.phoneNumber.toFaDigits(),
                                color = Game.TextSecondary,
                                size = 12.sp,
                                bold = false
                            )
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Game.Mint)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Game.Gold.copy(alpha = 0.12f))
                                .border(1.dp, Game.Gold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Game.Gold)
                            )
                            GameText(
                                text = "حالت مهمان (موقتی)",
                                color = Game.Gold,
                                size = 11.sp,
                                bold = true
                            )
                        }
                    }

                    if (user.languageLevel.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Game.Violet.copy(alpha = 0.15f))
                                .border(1.dp, Game.Violet.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            GameText(
                                text = "سطح ${user.languageLevel}",
                                color = Game.Violet,
                                size = 11.sp,
                                bold = true
                            )
                        }
                    }
                }
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ProfileHeaderCardPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        ProfileHeaderCard(
            user = UserProfile(
                id = "1",
                phoneNumber = "09123456789",
                nickName = "مسیحا",
                avatar = "avatar_b1",
                languageLevel = "B1"
            ),
            onAvatarClick = {},
            onEditNameClick = {}
        )
    }
}
