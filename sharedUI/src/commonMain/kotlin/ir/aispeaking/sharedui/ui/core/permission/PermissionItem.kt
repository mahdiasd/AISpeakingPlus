package ir.aispeaking.sharedui.ui.core.permission;

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.permission.AppPermission
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PermissionItem(
    modifier: Modifier,
    appPermission: AppPermission,
    addSpace: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        TitleBoldText(text = stringResource(appPermission.title()))

        BodyMediumText(text = stringResource(appPermission.description()), color = AppTheme.colors.outline)

        if (addSpace) VerticalSpace()
    }
}

