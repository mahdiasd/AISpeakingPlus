package ir.aispeaking.sharedui.ui.utils.avatar

import ir.aispeaking.sharedui.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ir.aispeaking.sharedui.ui.model.avatar.ProfileAvatar
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import kotlinx.collections.immutable.ImmutableList

object AvatarUtils {

    fun provideProfileAvatars(): ImmutableList<ProfileAvatar> {
        return immutableListOf(
            ProfileAvatar(Res.drawable.avatar_g1, "G-1"),
            ProfileAvatar(Res.drawable.avatar_g2, "G-2"),
            ProfileAvatar(Res.drawable.avatar_g3, "G-3"),
            ProfileAvatar(Res.drawable.avatar_g4, "G-4"),
            ProfileAvatar(Res.drawable.avatar_g5, "G-5"),
            ProfileAvatar(Res.drawable.avatar_g6, "G-6"),
            ProfileAvatar(Res.drawable.avatar_g7, "G-7"),
            ProfileAvatar(Res.drawable.avatar_g8, "G-8"),
            ProfileAvatar(Res.drawable.avatar_g9, "G-9"),
            ProfileAvatar(Res.drawable.avatar_g10, "G-10"),
            ProfileAvatar(Res.drawable.avatar_g11, "G-11"),
            ProfileAvatar(Res.drawable.avatar_g12, "G-12"),

            ProfileAvatar(Res.drawable.avatar_b1, "B-1"),
            ProfileAvatar(Res.drawable.avatar_b2, "B-2"),
            ProfileAvatar(Res.drawable.avatar_b3, "B-3"),
            ProfileAvatar(Res.drawable.avatar_b4, "B-4"),
            ProfileAvatar(Res.drawable.avatar_b5, "B-5"),
            ProfileAvatar(Res.drawable.avatar_b6, "B-6"),
            ProfileAvatar(Res.drawable.avatar_b7, "B-7"),
            ProfileAvatar(Res.drawable.avatar_b8, "B-8"),
            ProfileAvatar(Res.drawable.avatar_b9, "B-9"),
            ProfileAvatar(Res.drawable.avatar_b10, "B-10"),
            ProfileAvatar(Res.drawable.avatar_b11, "B-11"),
            ProfileAvatar(Res.drawable.avatar_b12, "B-12"),
        )
    }

    @Composable
    fun findAvatarByNameComposable(avatar: String?): ProfileAvatar? {
        val localAvatars by remember { mutableStateOf(provideProfileAvatars()) }
        return remember(avatar) {
            derivedStateOf { localAvatars.find { it.name == avatar } }
        }.value
    }

    fun findAvatarByName(avatar: String?): ProfileAvatar? {
        if (avatar == null) {
            return null
        }
        return provideProfileAvatars().find { it.name == avatar }
    }
}