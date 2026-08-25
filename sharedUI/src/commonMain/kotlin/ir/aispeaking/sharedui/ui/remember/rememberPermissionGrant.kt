package ir.aispeaking.sharedui.ui.remember

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.rememberMultiplePermissionsState
import ir.aispeaking.sharedui.ui.permission.AppPermission
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionGrant(permissions: ImmutableList<AppPermission>): Boolean {

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = permissions.map { it.permission }
    )

    return multiplePermissionsState.allPermissionsGranted
}