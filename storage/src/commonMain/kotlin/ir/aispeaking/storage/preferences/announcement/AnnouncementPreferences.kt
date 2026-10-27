package ir.aispeaking.storage.preferences.announcement

interface AnnouncementPreferences {
    fun isSuppressed(announcementId: String): Boolean
    fun suppress(announcementId: String, durationHours: Int = 24)
}
