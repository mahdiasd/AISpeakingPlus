package ir.aispeaking.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.InputType
import ir.aispeaking.chat.OnAction
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.no_suggestion_exist
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun SuggestsContent(
    modifier: Modifier,
    suggests: ImmutableList<String>?,
    onAction: OnAction = {},
) {
    val temp = stringResource(Res.string.no_suggestion_exist)
    val list = remember {
        derivedStateOf {
            if (suggests.isNullOrEmpty()) {
                immutableListOf(temp)
            } else suggests
        }
    }
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .clipScrollableContainer(Orientation.Horizontal),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally)
    ) {
        items(items = list.value, key = { it })
        {
            LabelMediumText(
                modifier = Modifier
                    .animateClickable {
                        if (it != temp) {
                            onAction(
                                ChatUiEvent.OnMessageChange(message = it, inputType = InputType.Text)
                            )
                        }
                    }
                    .shadow(1.dp, shape = AppTheme.shapes.roundLarge)
                    .background(
                        color = AppTheme.colors.surface,
                        shape = AppTheme.shapes.roundLarge
                    )
                    .border(
                        width = 1.dp,
                        color = if (it == temp)
                            AppTheme.colors.onSurface
                        else AppTheme.colors.primary,
                        shape = AppTheme.shapes.roundLarge
                    )
                    .padding(8.dp),
                color = if (it == temp)
                    AppTheme.colors.onSurface
                else AppTheme.colors.primary,
                text = it
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            SuggestsContent(
                modifier = Modifier.fillMaxWidth(),
                suggests = immutableListOf("This is suggest text"),
            )
            SuggestsContent(
                modifier = Modifier.fillMaxWidth(),
                suggests = immutableListOf(),
            )
        }
    }

}
