package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.model.ThemeMode
import ru.alexandrros.petly.domain.repository.ThemeRepository

class SetThemeModeUseCase(private val themeRepository: ThemeRepository) {
    suspend operator fun invoke(mode: ThemeMode) {
        themeRepository.setThemeMode(mode)
    }
}