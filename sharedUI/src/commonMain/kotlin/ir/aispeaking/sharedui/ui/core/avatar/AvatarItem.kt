package ir.aispeaking.sharedui.ui.core.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.model.avatar.ProfileAvatar
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import org.jetbrains.compose.resources.painterResource

@Composable
fun AvatarItem(profileAvatar: ProfileAvatar, onClick: (ProfileAvatar) -> Unit) {
    Image(
        modifier = Modifier
            .size(60.dp)
            .background(color = AppTheme.colors.surfaceContainer, CircleShape)
            .clip(CircleShape)
            .animateClickable {
                onClick(profileAvatar)
            },
        contentScale = ContentScale.FillBounds,
        painter = painterResource(profileAvatar.drawable),
        contentDescription = "avatar named: ${profileAvatar.name}",
    )
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        AvatarItem(AvatarUtils.provideProfileAvatars().first()) { }
    }
}
