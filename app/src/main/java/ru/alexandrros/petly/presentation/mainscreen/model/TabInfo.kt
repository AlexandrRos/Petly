package ru.alexandrros.petly.presentation.mainscreen.model

import androidx.compose.ui.graphics.vector.ImageVector
import ru.alexandrros.petly.presentation.common.navigation.Screen

data class TabInfo(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)