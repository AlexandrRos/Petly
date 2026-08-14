package ru.alexandrros.petly.presentation.common.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.alexandrros.petly.presentation.login.LoginRoute
import ru.alexandrros.petly.presentation.mainscreen.MainScreen
import ru.alexandrros.petly.presentation.registration.RegisterRoute

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginRoute(
                onLoginSuccess = { navController.navigate(Screen.Main.route) },
                onNavigateToRegister = { navController.navigate(Screen.Registration.route) }
            )
        }
        composable(Screen.Registration.route) {
            RegisterRoute(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(outerNavController = navController)
        }
    }
}