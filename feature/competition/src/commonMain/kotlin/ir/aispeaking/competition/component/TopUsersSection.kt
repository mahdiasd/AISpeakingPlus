package ir.aispeaking.competition.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.user.UserSummary
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.show_less
import ir.aispeaking.sharedui.show_more
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.coloredShadow
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopUsersSection(
    users: ImmutableList<UserSummary>,
) {
    val localAvatars by remember { mutableStateOf(AvatarUtils.provideProfileAvatars()) }
    var expand by remember { mutableStateOf(false) }

    val fourToTenUsers by remember {
        derivedStateOf { if (users.size > 3) users.drop(3).toImmutableList() else immutableListOf() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = AppTheme.shapes.roundMedium)
            .coloredShadow(
                color = AppTheme.colors.primary,
                borderRadius = 10.dp,
                shadowRadius = 10.dp,
                offsetY = 2.dp,
                alpha = 0.6f
            )
            .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.Top)
    ) {

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            users.getOrNull(1)?.let { user ->
                TopThreeUserItem(modifier = Modifier.weight(1f).padding(top = 50.dp), user, 1, localAvatars)
            }
            users.getOrNull(0)?.let { user ->
                TopThreeUserItem(modifier = Modifier.weight(1f), user, 0, localAvatars)
            }
            users.getOrNull(2)?.let { user ->
                TopThreeUserItem(modifier = Modifier.weight(1f).padding(top = 50.dp), user, 2, localAvatars)
            }
        }

        DualContentRow(
            modifier = Modifier
                .fillMaxWidth()
                .animateClickable {
                    expand = !expand
                },
            leftContent = {
                BodyMediumBoldText(
                    text = stringResource(if (expand) Res.string.show_less else Res.string.show_more),
                    color = AppTheme.colors.primary
                )
            },
            rightContent = {
                AppIcon(
                    modifier = Modifier.rotate(90f * if (expand) 1 else -1),
                    icon = Res.drawable.ic_arrow_left,
                    tint = AppTheme.colors.primary,
                    size = 12.dp
                )
            },
        )

        AnimatedVisibility(visible = expand) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .clipScrollableContainer(Orientation.Vertical),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(items = fourToTenUsers, key = { _, item -> item.uid }) { index, item ->
                    UserDefaultItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                        index = index + 3,
                        localAvatars = localAvatars,
                    )
                }
            }
        }
    }
}


@LightDarkPreview
@Composable
private fun TopUsersSectionPreview() {
    AppTheme {
        TopUsersSection(
            users = FakeData.provideUsersSummary(),
        )
    }
}