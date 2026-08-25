package ir.aispeaking.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.aispeaking.feature.auth.AuthUiEvent
import ir.aispeaking.feature.auth.OnAction
import ir.aispeaking.feature.auth.inputs.OtpCode
import ir.aispeaking.sharedui.ui.core.input.AppTextField
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.validation.errorMessage
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun OtpInputsSection(
    modifier: Modifier,
    otpCodes: ImmutableList<OtpCode>,
    onAction: OnAction
) {
    val focusRequesters = remember {
        List(otpCodes.size) { FocusRequester() }
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        otpCodes.forEachIndexed { index, otpCode ->
            AppTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .imePadding()
                    .aspectRatio(1f)
                    .focusRequester(focusRequesters[index]),
                textAlign = TextAlign.Center,
                errorText = otpCode.validate().errorMessage()?.let { stringResource(it) },
                value = otpCode.value,
                shape = AppTheme.shapes.roundSmall,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (index == otpCodes.size - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < otpCodes.size - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    },
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                showClearIcon = false,
                onValueChange = { newValue ->
                    val sanitizedValue = if (newValue.length > 1) newValue.take(1) else newValue
                    onAction(
                        AuthUiEvent.OnChangeOTP(
                            newText = sanitizedValue,
                            indexInList = index
                        )
                    )
                    when {
                        // Move to next field when a character is entered
                        sanitizedValue.isNotEmpty() && index < otpCodes.size - 1 -> {
                            focusRequesters[index + 1].requestFocus()
                        }
                        // Move to previous field when deleting
                        sanitizedValue.isEmpty() && index > 0 -> {
                            focusRequesters[index - 1].requestFocus()
                        }
                        else -> keyboardController?.hide()
                    }
                },
                hint = stringResource(otpCode.hint)
            )

        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        OtpInputsSection(
            modifier = Modifier.fillMaxWidth(),
            otpCodes = immutableListOf(
                OtpCode(),
                OtpCode(),
                OtpCode(),
                OtpCode()
            ),
            onAction = { }
        )
    }
}