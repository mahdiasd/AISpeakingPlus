package ir.aispeaking.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GameTextField
import kotlinx.coroutines.delay

/**
 * Returns true if the first word of the text starts with an English (Latin) letter.
 * Returns false (RTL) for Persian/Arabic, empty text, or other scripts.
 */
fun isEnglishFirstWord(text: String): Boolean {
    val trimmed = text.trimStart()
    if (trimmed.isEmpty()) return false
    val firstWord = trimmed.substringBefore(' ')
    val firstLetter = firstWord.firstOrNull { it.isLetter() }
    return if (firstLetter != null) {
        firstLetter in 'a'..'z' || firstLetter in 'A'..'Z'
    } else {
        val firstChar = firstWord.firstOrNull() ?: return false
        firstChar in 'a'..'z' || firstChar in 'A'..'Z'
    }
}

@Composable
fun EditNickNameDialog(
    visible: Boolean = true,
    currentNickName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textFieldValue by remember(currentNickName, visible) {
        val initialText = currentNickName
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(visible) {
        if (visible) {
            delay(120)
            try {
                focusRequester.requestFocus()
            } catch (_: Throwable) {}
        }
    }

    val isLtr = remember(textFieldValue.text) {
        isEnglishFirstWord(textFieldValue.text)
    }

    GameModal(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GameText(text = "ویرایش نام کاربری", size = 18.sp, bold = true)
            GameTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = "نام یا نام مستعار",
                placeholder = "نام جدید خود را وارد کنید",
                ltr = isLtr,
                fieldModifier = Modifier.focusRequester(focusRequester),
                onImeAction = {
                    val trimmed = textFieldValue.text.trim()
                    if (trimmed.isNotBlank()) onSave(trimmed)
                }
            )
            GameButton(
                text = "ذخیره تغییرات",
                onClick = {
                    val trimmed = textFieldValue.text.trim()
                    if (trimmed.isNotBlank()) onSave(trimmed)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = textFieldValue.text.isNotBlank()
            )
            GameButton(
                text = "انصراف",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Glass,
                height = 46.dp,
                textSize = 14.sp
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun EditNickNameDialogPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        EditNickNameDialog(
            currentNickName = "Ali",
            onSave = {},
            onDismiss = {}
        )
    }
}
