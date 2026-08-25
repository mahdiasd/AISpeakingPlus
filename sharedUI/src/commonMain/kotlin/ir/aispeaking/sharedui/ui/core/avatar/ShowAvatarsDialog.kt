package ir.aispeaking.sharedui.ui.core.avatar



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.avatar_dialog_title
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.model.avatar.ProfileAvatar
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowAvatarsDialog(
    avatars: ImmutableList<ProfileAvatar>,
    onClick: (ProfileAvatar) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                32.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            BodyMediumText(
                text = stringResource(Res.string.avatar_dialog_title)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 4,
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                avatars.forEach {
                    AvatarItem(profileAvatar = it, onClick = onClick)
                }
            }
        }
    }
}
