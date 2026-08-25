package ir.aispeaking.sharedui.ui.permission


import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.RecordAudio
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.permission_audio_description
import ir.aispeaking.sharedui.permission_audio_title
import org.jetbrains.compose.resources.StringResource

class AudioPermission : AppPermission {
    override val permission: Permission = Permission.RecordAudio

    override fun title(): StringResource = Res.string.permission_audio_title
    override fun description(): StringResource = Res.string.permission_audio_description
}