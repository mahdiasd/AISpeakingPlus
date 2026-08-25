package ir.aispeaking.domain.usecase.theme_mode

import ir.aispeaking.domain.model.theme_mode.ThemeMode
import ir.aispeaking.domain.repository.theme_mode.ThemeModeRepository
import org.koin.core.annotation.Single

@Single
class SaveThemeModeUseCase(private val repo: ThemeModeRepository) {
    suspend operator fun invoke(mode: String) = repo.save(ThemeMode(mode = mode))
}