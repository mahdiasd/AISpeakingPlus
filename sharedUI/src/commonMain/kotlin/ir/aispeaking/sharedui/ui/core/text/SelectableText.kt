package ir.aispeaking.sharedui.ui.core.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay

@Composable
fun SelectableText(
    modifier: Modifier,
    text: String,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppTheme.colors.onSurface,
        unfocusedTextColor = AppTheme.colors.onSurface,
        focusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        errorBorderColor = Color.Transparent,
        focusedLabelColor = Color.Transparent,
    ),
    textStyle: TextStyle = AppTheme.typography.bodyMedium,
    enable: Boolean = true,
    debounceTimeMillis: Long = 500, // Configurable debounce time
    onSelected: (String) -> Unit
) {
    var textInput by remember { mutableStateOf(TextFieldValue(text)) }
    val latestSelection = remember { mutableStateOf("") }
    val shouldDeselect = remember { mutableStateOf(false) }

    // Synchronize textInput when text prop updates (e.g. streaming or new turns)
    LaunchedEffect(text) {
        if (textInput.text != text) {
            textInput = textInput.copy(text = text)
        }
    }

    // Handle debouncing and API call
    LaunchedEffect(latestSelection.value) {
        if (latestSelection.value.isNotEmpty()) {
            delay(debounceTimeMillis)
            if (enable) {
                onSelected(latestSelection.value)
                // Trigger deselection after API call
                shouldDeselect.value = true
            }
        }
    }

    // Handle deselection
    LaunchedEffect(shouldDeselect.value) {
        if (shouldDeselect.value) {
            // Clear selection but keep text content
            textInput = TextFieldValue(
                text = textInput.text,
                selection = TextRange.Zero // This removes the selection
            )
            shouldDeselect.value = false
            latestSelection.value = ""
        }
    }

    TextField(
        modifier = modifier,
        value = textInput,
        textStyle = textStyle,
        onValueChange = { newValue ->
            // We need to preserve the text but can update the selection
            textInput = newValue

            val currentSelection = newValue.getSelectedText().text
            if (currentSelection.isNotEmpty() && currentSelection != latestSelection.value) {
                latestSelection.value = currentSelection
            }
        },
        readOnly = true,
        colors = colors
    )
}

@Composable
fun SelectableText(
    modifier: Modifier,
    text: String,
    textStyle: TextStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.onSurface),
    enable: Boolean = true,
    debounceTimeMillis: Long = 500, // Configurable debounce time
    onSelected: (String) -> Unit
) {
    var textInput by remember { mutableStateOf(TextFieldValue(text)) }
    val latestSelection = remember { mutableStateOf("") }
    val shouldDeselect = remember { mutableStateOf(false) }

    // Synchronize textInput when text prop updates (e.g. streaming or new turns)
    LaunchedEffect(text) {
        if (textInput.text != text) {
            textInput = textInput.copy(text = text)
        }
    }

    // Handle debouncing and API call
    LaunchedEffect(latestSelection.value) {
        if (latestSelection.value.isNotEmpty()) {
            delay(debounceTimeMillis)
            if (enable) {
                onSelected(latestSelection.value)
                // Trigger deselection after API call
                shouldDeselect.value = true
            }
        }
    }

    // Handle deselection
    LaunchedEffect(shouldDeselect.value) {
        if (shouldDeselect.value) {
            // Clear selection but keep text content
            textInput = TextFieldValue(
                text = textInput.text,
                selection = TextRange.Zero // This removes the selection
            )
            shouldDeselect.value = false
            latestSelection.value = ""
        }
    }

    BasicTextField(
        modifier = modifier,
        value = textInput,
        onValueChange = { newValue ->
            // We need to preserve the text but can update the selection
            textInput = newValue

            val currentSelection = newValue.getSelectedText().text
            if (currentSelection.isNotEmpty() && currentSelection != latestSelection.value) {
                latestSelection.value = currentSelection
            }
        },
        textStyle = textStyle,
        readOnly = true,

        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(modifier)
            ) {
                innerTextField()
            }
        }
    )
}