package ru.alexandrros.petly.presentation.mainscreen


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import ru.alexandrros.petly.presentation.requests.RequestsScreen
import ru.alexandrros.petly.presentation.specialists.Specialists
import ru.alexandrros.petly.presentation.userpets.AddEditPetScreen
import ru.alexandrros.petly.presentation.userpets.PetDetailScreen
import ru.alexandrros.petly.presentation.userpets.UserPets
import ru.alexandrros.petly.presentation.viewmodel.PetListViewModel
import ru.alexandrros.petly.presentation.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userViewModel: UserViewModel,
    petListViewModelFactory: ViewModelProvider.Factory,
    outerNavController: NavController
) {
    val petListViewModel: PetListViewModel = viewModel(factory = petListViewModelFactory)
    val nestedNavController = rememberNavController()
    val pets by petListViewModel.pets.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val bottomNavItems = listOf(
        TabInfo(Screen.Pets, "Ваши питомцы", Icons.Filled.Pets),
        TabInfo(Screen.Requests, "Заявки", Icons.Filled.Newspaper),
        TabInfo(Screen.Specialists, "Специалисты", Icons.Filled.Search),
        TabInfo(Screen.Profile, "Аккаунт", Icons.Filled.AccountCircle)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
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
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    pets = pets,
                    onPetClick = { pet ->
                        // pet.id is now String
                        nestedNavController.navigate(Screen.PetDetail.createRoute(pet.id))
                    },
                    onAddClick = { nestedNavController.navigate(Screen.AddPet.route) }
                )
            }

            composable(Screen.Requests.route) { RequestsScreen() }

            composable(Screen.Profile.route) {
                Profile(
                    user = currentUser,
                    onLogout = {
                        userViewModel.logout()
                        // Navigate back to login screen
                        outerNavController.navigate("login") {
                            popUpTo(0) { inclusive = true } // clear back stack
                        }
                    }
                )
            }

            composable(
                route = Screen.PetDetail.route,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })   // String
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""   // getString
                val pet = petListViewModel.getPetById(petId)
                if (pet != null) {
                    PetDetailScreen(
                        pet = pet,
                        onBackClick = { nestedNavController.popBackStack() },
                        onEditClick = { petToEdit ->
                            nestedNavController.navigate(Screen.EditPet.createRoute(petToEdit.id))
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Питомец не найден", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            composable(Screen.AddPet.route) {
                AddEditPetScreen(
                    pet = null,
                    onSave = { newPet ->
                        petListViewModel.addPet(newPet)   // id & userId will be filled by repository
                        nestedNavController.popBackStack()
                    },
                    onCancel = { nestedNavController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditPet.route,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })   // String
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                val pet = petListViewModel.getPetById(petId)
                if (pet != null) {
                    AddEditPetScreen(
                        pet = pet,
                        onSave = { updatedPet ->
                            petListViewModel.updatePet(updatedPet)
                            nestedNavController.popBackStack()
                        },
                        onCancel = { nestedNavController.popBackStack() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Питомец не найден", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}