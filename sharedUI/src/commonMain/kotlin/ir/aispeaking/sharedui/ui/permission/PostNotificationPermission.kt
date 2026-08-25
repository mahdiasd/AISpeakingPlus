package ir.aispeaking.sharedui.permission


import com.mohamedrejeb.calf.permissions.Notification
import com.mohamedrejeb.calf.permissions.Permission
import ir.aispeaking.sharedui.*


import ir.aispeaking.sharedui.ui.permission.AppPermission
import org.jetbrains.compose.resources.StringResource

class PostNotificationPermission : AppPermission {
    override val permission: Permission = Permission.Notification

    override fun title(): StringResource = Res.string.permission_notification_title
    override fun description(): StringResource = Res.string.permission_notification_description
}