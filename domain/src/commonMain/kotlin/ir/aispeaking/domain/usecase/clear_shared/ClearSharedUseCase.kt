package ir.aispeaking.domain.usecase.clear_shared

import ir.aispeaking.domain.repository.clear_shared.ClearSharedRepository
import org.koin.core.annotation.Single

@Single
class ClearSharedUseCase(private val repo: ClearSharedRepository) {
    suspend operator fun invoke() = repo.clear()
}