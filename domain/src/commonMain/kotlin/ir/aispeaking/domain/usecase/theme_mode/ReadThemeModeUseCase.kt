package ir.aispeaking.domain.usecase.theme_mode

import ir.aispeaking.domain.repository.theme_mode.ThemeModeRepository
import org.koin.core.annotation.Single

@Single
class ReadThemeModeUseCase(private val repo: ThemeModeRepository) {
    suspend operator fun invoke() = repo.read()
}