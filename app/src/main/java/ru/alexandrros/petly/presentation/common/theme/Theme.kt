package ru.alexandrros.petly.presentation.common.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,

    secondary = GreenGrey40,
    onSecondary = Color.White,
    secondaryContainer = GreenGrey90,
    onSecondaryContainer = GreenGrey10,

    tertiary = TealGreen40,
    onTertiary = Color.White,
    tertiaryContainer = TealGreen90,
    onTertiaryContainer = TealGreen10,

    // Neutral background & surface
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = GreenGrey95,
    onSurfaceVariant = GreenGrey30,

    error = ErrorRed,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,

    outline = GreenGrey60,
    outlineVariant = GreenGrey80,

    inverseSurface = Green30,
    inverseOnSurface = Green95,
    inversePrimary = Green80,
    surfaceTint = Green40,
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,

    secondary = GreenGrey80,
    onSecondary = GreenGrey20,
    secondaryContainer = GreenGrey30,
    onSecondaryContainer = GreenGrey90,

    tertiary = TealGreen80,
    onTertiary = TealGreen20,
    tertiaryContainer = TealGreen30,
    onTertiaryContainer = TealGreen90,

    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = GreenGrey30,
    onSurfaceVariant = GreenGrey80,

    error = ErrorRedDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,

    outline = GreenGrey60,
    outlineVariant = GreenGrey30,

    inverseSurface = Green90,
    inverseOnSurface = Green10,
    inversePrimary = Green40,
    surfaceTint = Green80,
)

@Composable
fun PetlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set window background to match Compose background
            window.setBackgroundDrawable(
                colorScheme.background.toArgb().toDrawable()
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}