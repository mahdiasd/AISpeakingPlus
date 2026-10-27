package ir.aispeaking.domain.usecase.announcement

import ir.aispeaking.domain.model.Announcement
import ir.aispeaking.domain.repository.AnnouncementRepository
import org.koin.core.annotation.Factory

@Factory
class GetActiveAnnouncementUseCase(
    private val announcementRepository: AnnouncementRepository
) {
    suspend operator fun invoke(): Announcement? {
        val active = announcementRepository.getActiveAnnouncement() ?: return null
        // If blocking, it can never be suppressed
        if (active.isBlocking) return active
        // If regular, check if user chose not to see it for 24h
        if (announcementRepository.isAnnouncementSuppressed(active.id)) return null
        return active
    }
}
