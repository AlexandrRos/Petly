package ru.alexandrros.petly.presentation.common.navigation


import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.alexandrros.petly.presentation.login.LoginRoute
import ru.alexandrros.petly.presentation.mainscreen.MainScreen
import ru.alexandrros.petly.presentation.registration.RegisterRoute


@Composable
fun AppNavGraph(
    loginViewModelFactory: ViewModelProvider.Factory,
    registerViewModelFactory: ViewModelProvider.Factory
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable(Screen.Login.route) {
            LoginRoute(
                factory = loginViewModelFactory,
                onLoginSuccess = { navController.navigate(Screen.Main.route) },
                onNavigateToRegister = { navController.navigate(Screen.Registration.route) }
            )
        }
        composable(Screen.Registration.route) {
            RegisterRoute(
                factory = registerViewModelFactory,
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}