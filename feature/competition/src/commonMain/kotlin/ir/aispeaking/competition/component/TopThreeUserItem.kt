package ir.aispeaking.competition.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.user.UserSummary
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.model.avatar.ProfileAvatar
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.PointsColor
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource

@Composable
fun TopThreeUserItem(
    modifier: Modifier = Modifier,
    item: UserSummary,
    index: Int,
    localAvatars: ImmutableList<ProfileAvatar>,
) {
    val avatarDrawable by remember(item) {
        derivedStateOf { localAvatars.find { it.name == item.avatar } }
    }

    val borderColor by remember(index) {
        derivedStateOf {
            when (index) {
                0 -> Color(0xFFFFD700) // Gold
                1 -> Color(0xFFC0C0C0) // Silver
                else -> Color(0xFFCD7F32) // Bronze (assuming index 2 is bronze, adjust if needed)
            }
        }
    }


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically)
    ) {

        avatarDrawable?.drawable?.let {
            Image(
                modifier = Modifier
                    .background(color = AppTheme.colors.outline, CircleShape)
                    .border(2.dp, color = borderColor, CircleShape)
                    .clip(CircleShape)
                    .size(65.dp),
                painter = painterResource(it),
                contentScale = ContentScale.FillBounds,
                contentDescription = "Avatar of $index top user"
            )
        }

        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BodyLargeBoldText(text = "${index + 1}")
            Spacer(
                Modifier
                    .height(35.dp)
                    .width(0.5.dp)
                    .background(AppTheme.colors.outline, shape = AppTheme.shapes.roundLarge)
            )

            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.CenterVertically
                )
            ) {
                LabelSmallBoldText(
                    text = item.nickName,
                    overflow = TextOverflow.MiddleEllipsis,
                    maxLines = 2
                )
                DualContentRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterHorizontally),
                    leftContent = {
                        LabelSmallBoldText(text = "${item.score}")
                    },
                    rightContent = {
                        AppIcon(
                            icon = Res.drawable.ic_points,
                            size = 8.dp,
                            tint = PointsColor
                        )
                    },
                )
            }

        }
    }
}

@Composable
fun UserDefaultItem(
    modifier: Modifier = Modifier,
    item: UserSummary,
    index: Int,
    localAvatars: ImmutableList<ProfileAvatar>,
) {
    val avatarDrawable by remember(item) {
        derivedStateOf { localAvatars.find { it.name == item.avatar } }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            8.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyLargeBoldText(text = "${index + 1}")

        avatarDrawable?.drawable?.let {
            Image(
                modifier = Modifier
                    .background(color = AppTheme.colors.outline, CircleShape)
                    .border(1.5.dp, color = AppTheme.colors.outlineVariant, CircleShape)
                    .clip(CircleShape)
                    .size(50.dp),
                painter = painterResource(it),
                contentScale = ContentScale.FillBounds,
                contentDescription = "Avatar of $index top user"
            )
        }

        LabelMediumBoldText(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            text = item.nickName,
            overflow = TextOverflow.MiddleEllipsis
        )
        DualContentRow(
            leftContent = { BodyMediumBoldText(text = "${item.score}") },
            rightContent = {
                AppIcon(
                    icon = Res.drawable.ic_points,
                    size = 16.dp,
                    tint = PointsColor
                )
            },
        )
    }

}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        val localAvatars by remember { mutableStateOf(AvatarUtils.provideProfileAvatars()) }
        TopThreeUserItem(
            modifier = Modifier,
            item = FakeData.provideUsersSummary().first(),
            index = 0,
            localAvatars = localAvatars
        )
    }
}

@LightDarkPreview
@Composable
private fun UserItemFlatPreview() {
    AppTheme {
        val localAvatars by remember { mutableStateOf(AvatarUtils.provideProfileAvatars()) }
        UserDefaultItem(
            modifier = Modifier,
            item = FakeData.provideUsersSummary().first(),
            index = 0,
            localAvatars = localAvatars
        )
    }
}