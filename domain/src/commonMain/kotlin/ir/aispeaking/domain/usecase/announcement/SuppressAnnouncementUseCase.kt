package ir.aispeaking.domain.usecase.announcement

import ir.aispeaking.domain.repository.AnnouncementRepository
import org.koin.core.annotation.Factory

@Factory
class SuppressAnnouncementUseCase(
    private val announcementRepository: AnnouncementRepository
) {
    operator fun invoke(announcementId: String, durationHours: Int = 24) {
        announcementRepository.suppressAnnouncement(announcementId, durationHours)
    }
}
