package ir.aispeaking.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.profile.OnAction
import ir.aispeaking.profile.ProfileUiEvent
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_edit_2
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.name
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.PointsColor
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun InfoSection(
    modifier: Modifier,
    user: User,
    onAction: OnAction
) {
    DualContentRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        leftContent = {
            Image(
                modifier = Modifier
                    .size(100.dp)
                    .background(color = Color.White, CircleShape)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = AppTheme.colors.primary,
                        shape = CircleShape
                    )
                ,
                painter = painterResource(AvatarUtils.findAvatarByNameComposable(user.avatar)!!.drawable),
                contentScale = ContentScale.FillHeight,
                contentDescription = "User avatar"
            )

        },
        rightContent = {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterVertically)
            ) {
                DualContentRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    leftContent = {
                        LabelMediumBoldText(
                            modifier = Modifier
                                .animateClickable { onAction(ProfileUiEvent.NavigateToEditProfile) }
                                .shadow(1.dp, shape = AppTheme.shapes.roundSmall)
                                .background(AppTheme.colors.primaryContainer, shape = AppTheme.shapes.roundSmall)
                                .border(1.dp, color = AppTheme.colors.primary, shape = AppTheme.shapes.roundSmall)
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            text = stringResource(user.languageLevel.name()),
                            color = AppTheme.colors.onPrimaryContainer
                        )
                    },
                    rightContent = {
                        AppIcon(
                            modifier = Modifier
                                .animateClickable { onAction(ProfileUiEvent.NavigateToEditProfile) }
                                .padding(4.dp),
                            size = 32.dp,
                            icon = Res.drawable.ic_edit_2,
                            tint = AppTheme.colors.onSurface
                        )
                    }
                )

                BodyLargeBoldText(text = user.nickName)
                DualContentRow(
                    leftContent = {
                        BodyLargeBoldText(
                            modifier = Modifier,
                            text = user.score.toString()
                        )
                    },
                    rightContent = {
                        AppIcon(
                            size = 16.dp,
                            icon = Res.drawable.ic_points,
                            tint = PointsColor
                        )
                    }
                )

            }

        }
    )
}


@LightDarkPreview
@Composable
private fun ProfilePreview() {
    AppTheme {
        InfoSection(
            modifier = Modifier.wrapContentWidth(),
            user = FakeData.provideUsers().first(),
            onAction = {}
        )
    }
}
