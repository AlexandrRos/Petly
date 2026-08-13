package ru.alexandrros.petly.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.ThemeMode

interface ThemeRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}