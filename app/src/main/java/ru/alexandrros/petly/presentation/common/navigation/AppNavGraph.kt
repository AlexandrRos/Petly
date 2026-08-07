package ru.alexandrros.petly.presentation.common.navigation


import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.alexandrros.petly.presentation.login.LoginRoute
import ru.alexandrros.petly.presentation.mainscreen.MainScreen
import ru.alexandrros.petly.presentation.registration.RegisterRoute
import ru.alexandrros.petly.presentation.profile.ProfileViewModel


@Composable
fun AppNavGraph(
    startDestination: String,
    loginViewModelFactory: ViewModelProvider.Factory,
    registerViewModelFactory: ViewModelProvider.Factory,
    profileViewModelFactory: ProfileViewModel.Factory,
    petListViewModelFactory: ViewModelProvider.Factory,
    requestViewModelFactory: ViewModelProvider.Factory,
    requestDetailViewModelFactory: (String) -> ViewModelProvider.Factory,
    petDetailViewModelFactory: (String) -> ViewModelProvider.Factory,
    editProfileViewModelFactory: ViewModelProvider.Factory,
    specialistListViewModelFactory: ViewModelProvider.Factory,
    specialistDetailViewModelFactory: (String) -> ViewModelProvider.Factory
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
            MainScreen(
                profileViewModelFactory = profileViewModelFactory,
                outerNavController = navController,
                petListViewModelFactory = petListViewModelFactory,
                requestViewModelFactory = requestViewModelFactory,
                requestDetailViewModelFactory = requestDetailViewModelFactory,
                petDetailViewModelFactory = petDetailViewModelFactory,
                editProfileViewModelFactory = editProfileViewModelFactory,
                specialistListViewModelFactory = specialistListViewModelFactory,
                specialistDetailViewModelFactory = specialistDetailViewModelFactory
            )
        }
    }
}