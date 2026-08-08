package ru.alexandrros.petly.presentation.common.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.max


val LocalBottomBarHeight = compositionLocalOf { 0.dp }
@Composable
fun rememberContentInsets(): WindowInsets {
    val bottomBarHeight = LocalBottomBarHeight.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    // Safe drawing covers status bar, cutouts, and side nav bars in landscape
    val safe = WindowInsets.safeDrawing
    val topPx = safe.getTop(density)
    val leftPx = safe.getLeft(density, layoutDirection)
    val rightPx = safe.getRight(density, layoutDirection)

    // Raw keyboard height
    val keyboardPx = WindowInsets.ime.getBottom(density)

    val bottomBarPx = with(density) { bottomBarHeight.roundToPx() }
    val bottomPx = max(keyboardPx, bottomBarPx)

    return WindowInsets(leftPx, topPx, rightPx, bottomPx)
}