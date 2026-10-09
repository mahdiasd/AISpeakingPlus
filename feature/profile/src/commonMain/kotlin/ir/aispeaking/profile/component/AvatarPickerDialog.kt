package ir.aispeaking.profile.component

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import org.jetbrains.compose.resources.painterResource

import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GameIconButton
@Composable
fun AvatarPickerDialog(
    visible: Boolean = true,
    currentAvatar: String,
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val avatars = AvatarUtils.provideProfileAvatars()

    GameModal(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameText(text = "انتخاب آواتار", size = 18.sp, bold = true)
                GameIconButton(
                    icon = Res.drawable.ic_close,
                    onClick = onDismiss,
                    contentDescription = "بستن",
                    size = 36.dp,
                    iconSize = 16.dp
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(avatars) { profileAvatar ->
                    val isSelected = profileAvatar.name.equals(currentAvatar, ignoreCase = true) ||
                        currentAvatar.contains(profileAvatar.name.replace("-", "").lowercase())

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Game.Violet.copy(alpha = 0.3f) else Color(0x11FFFFFF))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Game.Mint else Color(0x22FFFFFF),
                                shape = CircleShape
                            )
                            .clickable { onAvatarSelected(profileAvatar.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(profileAvatar.drawable),
                            contentDescription = profileAvatar.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AvatarPickerDialogPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        AvatarPickerDialog(
            currentAvatar = "avatar1",
            onAvatarSelected = {},
            onDismiss = {}
        )
    }
}
