package ir.aispeaking.main.component;

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import ir.aispeaking.main.MainUiEvent
import ir.aispeaking.main.OnAction
import ir.aispeaking.main.model.BottomBarItem
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BottomBarItemScreen(
    bottomBarItems: ImmutableList<BottomBarItem>,
    currentBottomBar: BottomBarItem,
    themeMode: ThemeMode = ThemeMode.System(),
    onAction: OnAction,
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isDark by remember(themeMode) {
        derivedStateOf {
            when {
                themeMode is ThemeMode.System && isSystemInDarkTheme -> true
                themeMode is ThemeMode.Dark -> true
                themeMode is ThemeMode.Light -> false
                else -> false
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceContainer)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (bottomBarItem in bottomBarItems) {
            val isSelected = bottomBarItem == currentBottomBar
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .animateClickable {
                        onAction(MainUiEvent.OnChangeTab(bottomBarItem))
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically)
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(
                        if (isSelected) {
                            if (isDark) bottomBarItem.activeIconDark else bottomBarItem.activeIconLight
                        } else {
                            bottomBarItem.icon
                        }
                    ),
                    contentDescription = stringResource(bottomBarItem.name),
                    colorFilter = if (isSelected) null else ColorFilter.tint(AppTheme.colors.onSurface)
                )

                if (isSelected)
                    LabelSmallBoldText(
                        text = stringResource(bottomBarItem.name),
                        color = AppTheme.colors.primary
                    )
                else
                    LabelSmallText(
                        text = stringResource(bottomBarItem.name),
                        color = AppTheme.colors.onSurface
                    )
            }
        }
    }
}
