package ir.aispeaking.sharedui.ui.core.permission

// Calf multiplatform permissions imports based on official documentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Notification
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.RecordAudio
import com.mohamedrejeb.calf.permissions.rememberMultiplePermissionsState
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui._continue
import ir.aispeaking.sharedui.ui.permission.AudioPermission
import ir.aispeaking.sharedui.permission.PostNotificationPermission
import ir.aispeaking.sharedui.permission_guide
import ir.aispeaking.sharedui.request_permissions
import ir.aispeaking.sharedui.ui.core.button.AppCompactButton
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.permission.AppPermission
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PerPermissionScreen(
    permissions: ImmutableList<AppPermission>,
    onDismissDialog: () -> Unit,
) {
    // Map custom AppPermission models to Calf's cross-platform Permission types
    val calfPermissions = remember(permissions) {
        permissions.mapNotNull { appPermission ->
            when (appPermission) {
                is AudioPermission -> Permission.RecordAudio
                is PostNotificationPermission -> Permission.Notification
                // Camera permission is removed as requested
                else -> null
            }
        }
    }

    // Use Calf's rememberMultiplePermissionsState as per the official documentation
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = calfPermissions
    )

    // Check if the only requested permission is PostNotificationPermission
    val isOnlyNotification = permissions.size == 1 && permissions.first() is PostNotificationPermission

    if (isOnlyNotification) {
        LaunchedEffect(Unit) {
            // Trigger the Calf permission request
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    } else {
        Dialog(
            onDismissRequest = onDismissDialog,
            content = {
                DialogContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = AppTheme.shapes.roundMedium)
                        .background(
                            color = AppTheme.colors.surfaceContainerHighest,
                            shape = AppTheme.shapes.roundMedium
                        )
                        .padding(16.dp),
                    permissions = permissions,
                    onAllow = {
                        // Trigger the Calf permission request natively on iOS/Android
                        multiplePermissionsState.launchMultiplePermissionRequest()
                    }
                )
            }
        )
    }
}

@Composable
private fun DialogContent(
    modifier: Modifier,
    permissions: ImmutableList<AppPermission>,
    onAllow: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        BodyMediumText(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("per-permission-title"),
            textAlign = TextAlign.Center,
            text = stringResource(Res.string.permission_guide)
        )

        permissions.forEach { appPermission ->
            // Use class simpleName as a safe key for Compose Multiplatform
            key(appPermission::class.simpleName) {
                BodyMediumText(
                    modifier = Modifier,
                    text = "- " + stringResource(appPermission.title())
                )
            }
        }

        LabelSmallText(
            text = stringResource(Res.string.request_permissions),
            textAlign = TextAlign.Center
        )

        AppCompactButton(
            modifier = Modifier.align(Alignment.End),
            text = Res.string._continue,
            onClick = onAllow
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        DialogContent(
            modifier = Modifier,
            permissions = immutableListOf(AudioPermission(), PostNotificationPermission()),
            onAllow = {}
        )
    }
}