package ir.aispeaking.sharedui.ui.permission

import com.mohamedrejeb.calf.permissions.Permission
import org.jetbrains.compose.resources.StringResource

interface AppPermission {
    val permission: Permission
    fun title(): StringResource
    fun description(): StringResource
}