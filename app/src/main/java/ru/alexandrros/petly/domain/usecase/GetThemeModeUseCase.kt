package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.ThemeMode
import ru.alexandrros.petly.domain.repository.ThemeRepository

class GetThemeModeUseCase(private val themeRepository: ThemeRepository) {
    operator fun invoke(): Flow<ThemeMode> = themeRepository.themeMode
}