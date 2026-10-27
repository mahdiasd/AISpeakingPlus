@file:OptIn(kotlin.time.ExperimentalTime::class)
package ir.aispeaking.storage.preferences.announcement

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import org.koin.core.annotation.Single

@Single
class AnnouncementPreferencesImpl(
    private val settings: Settings
) : AnnouncementPreferences {

    override fun isSuppressed(announcementId: String): Boolean {
        val key = "${SharedKeyConstant.ANNOUNCEMENT_SUPPRESS_PREFIX}$announcementId"
        val expiresAt = settings.getLong(key, 0L)
        if (expiresAt <= 0L) return false

        val now = Clock.System.now().toEpochMilliseconds()
        return now < expiresAt
    }

    override fun suppress(announcementId: String, durationHours: Int) {
        val key = "${SharedKeyConstant.ANNOUNCEMENT_SUPPRESS_PREFIX}$announcementId"
        val expiresAt = (Clock.System.now() + durationHours.hours).toEpochMilliseconds()
        settings.putLong(key, expiresAt)
    }
}
