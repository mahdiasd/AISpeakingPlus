package ir.aispeaking.data.repository.announcement

import ir.aispeaking.data.mapper.announcement.toDomain
import ir.aispeaking.domain.model.Announcement
import ir.aispeaking.domain.repository.AnnouncementRepository
import ir.aispeaking.network.api.announcement.AnnouncementApi
import ir.aispeaking.storage.preferences.announcement.AnnouncementPreferences
import org.koin.core.annotation.Single

@Single
class AnnouncementRepositoryImpl(
    private val announcementApi: AnnouncementApi,
    private val announcementPreferences: AnnouncementPreferences
) : AnnouncementRepository {

    override suspend fun getActiveAnnouncement(): Announcement? {
        return try {
            announcementApi.getActiveAnnouncement()?.toDomain()
        } catch (_: Throwable) {
            null
        }
    }

    override fun isAnnouncementSuppressed(announcementId: String): Boolean {
        return announcementPreferences.isSuppressed(announcementId)
    }

    override fun suppressAnnouncement(announcementId: String, durationHours: Int) {
        announcementPreferences.suppress(announcementId, durationHours)
    }
}
