package ir.aispeaking.domain.repository

import ir.aispeaking.domain.model.Announcement

interface AnnouncementRepository {
    suspend fun getActiveAnnouncement(): Announcement?
    fun isAnnouncementSuppressed(announcementId: String): Boolean
    fun suppressAnnouncement(announcementId: String, durationHours: Int = 24)
}
