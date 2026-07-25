package ru.alexandrros.petly.presentation.mainscreen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.alexandrros.petly.presentation.common.navigation.Screen
import ru.alexandrros.petly.presentation.mainscreen.model.TabInfo
import ru.alexandrros.petly.presentation.profile.Profile
import ru.alexandrros.petly.presentation.specialists.Specialists
import ru.alexandrros.petly.presentation.userpets.PetDetailScreen
import ru.alexandrros.petly.presentation.userpets.UserPets
import ru.alexandrros.petly.presentation.viewmodel.PetListViewModel


@Composable
fun MainScreen() {
    val petListViewModel: PetListViewModel = viewModel()
    val nestedNavController = rememberNavController()
    val bottomNavItems = listOf(
        TabInfo(Screen.Pets, "Ваши питомцы", Icons.Filled.Pets),
        TabInfo(Screen.Specialists, "Специалисты", Icons.Filled.Search),
        TabInfo(Screen.Profile, "Аккаунт", Icons.Filled.AccountCircle)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true,
                        onClick = {
                            nestedNavController.navigate(tab.screen.route) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = Screen.Pets.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Specialists.route) { Specialists() }
            composable(Screen.Pets.route) {
                UserPets(
                    pets = petListViewModel.pets,
                    onPetClick = { pet ->
                        nestedNavController.navigate(Screen.PetDetail.createRoute(pet.id))
                    }
                )
            }
            composable(Screen.Profile.route) { Profile() }

            composable(
                route = Screen.PetDetail.route,
                arguments = listOf(navArgument("petId") { type = NavType.IntType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getInt("petId") ?: 0
                val pet = petListViewModel.getPetById(petId)
                if (pet != null) {
                    PetDetailScreen(pet = pet)
                } else {
                    Text("Питомец не найден")
                }
            }
        }
    }
}