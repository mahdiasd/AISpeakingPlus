package ir.speaking.feature.config.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfigRequestDto(
    val deviceName: String? = null,
    val androidVersion: String? = null,
    val firebaseToken: String? = null,
    val appVersion: String? = null,
    val versionCode: Int? = null
)

@Serializable
data class UpdateResponseDto(
    @SerialName("force_version")
    val forceVersion: Int = 10,

    @SerialName("last_version")
    val lastVersion: Int = 10,

    @SerialName("suggest_version")
    val suggestVersion: Int = 10,

    val link: String = "https://cafebazaar.ir/app/ir.aispeaking",

    val message: String = "نسخه جدید AI Speaking با مراحل داستانی جذاب و ویژگی‌های نوین منتشر شد. برای ادامه لطفاً برنامه را بروزرسانی نمایید."
)

@Serializable
data class WelcomeMessageResponseDto(
    val id: String = "welcome_v2",
    val message: String = "به نسخه جدید AI Speaking خوش آمدید!",
    val title: String = "ارتقای سیستم",
    val imageUrl: String = "",
    val isReadByUser: Boolean = false
)

@Serializable
data class ConfigResponseDto(
    val update: UpdateResponseDto,
    val welcomeMessage: WelcomeMessageResponseDto? = null,
    val tokenAlive: Boolean = true
)

@Serializable
data class AppVersionConfigDto(
    val platform: String = "android",
    val lastVersion: Int,
    val minVersion: Int,
    val suggestVersion: Int,
    val link: String,
    val message: String,
    val updatedAt: String? = null
)

@Serializable
data class UpdateAppVersionConfigRequest(
    val lastVersion: Int,
    val minVersion: Int,
    val suggestVersion: Int,
    val link: String,
    val message: String
)
