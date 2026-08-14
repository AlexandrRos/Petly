package ru.alexandrros.petly

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import org.koin.compose.koinInject
import ru.alexandrros.petly.domain.model.ThemeMode
import ru.alexandrros.petly.domain.usecase.GetThemeModeUseCase
import ru.alexandrros.petly.domain.usecase.IsUserLoggedInUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.presentation.common.navigation.AppNavGraph
import ru.alexandrros.petly.presentation.common.navigation.Screen
import ru.alexandrros.petly.presentation.common.theme.PetlyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val getThemeModeUseCase: GetThemeModeUseCase = koinInject()
            val isUserLoggedInUseCase: IsUserLoggedInUseCase = koinInject()
            val observeCurrentUserUseCase: ObserveCurrentUserUseCase = koinInject()

            val themeMode by getThemeModeUseCase()
                .collectAsState(initial = ThemeMode.SYSTEM)

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
            }

            PetlyTheme(darkTheme = darkTheme) {
                var startDestination by remember {
                    mutableStateOf(
                        if (isUserLoggedInUseCase()) Screen.Main.route
                        else Screen.Login.route
                    )
                }

                LaunchedEffect(Unit) {
                    observeCurrentUserUseCase().collect { user ->
                        startDestination = if (user != null) Screen.Main.route
                        else Screen.Login.route
                    }
                }

                AppNavGraph(startDestination = startDestination)
            }
        }
    }
}