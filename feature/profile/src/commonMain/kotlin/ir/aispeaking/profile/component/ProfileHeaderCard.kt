package ir.aispeaking.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.avatar_g1
import ir.aispeaking.sharedui.ic_edit
import ir.aispeaking.sharedui.ic_phone
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0x336366F1),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with edit badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(avatarResource),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF6366F1), CircleShape)
                )

                // Small edit badge over avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1))
                        .border(1.5.dp, Color(0xFF0F172A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_edit),
                        contentDescription = "Edit Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info (Name + Phone)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.displayName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    IconButton(
                        onClick = onEditNameClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_edit),
                            contentDescription = "Edit Nickname",
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!user.isGuest && user.phoneNumber.isNotBlank()) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_phone),
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = user.phoneNumber,
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33F59E0B))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "حالت مهمان (بدون شماره)",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
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
                avatar = "avatar_b1"
            ),
            onAvatarClick = {},
            onEditNameClick = {}
        )
    }
}
