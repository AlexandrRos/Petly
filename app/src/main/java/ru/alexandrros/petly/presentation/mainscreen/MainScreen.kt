package ru.alexandrros.petly.presentation.mainscreen


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import ru.alexandrros.petly.presentation.profile.EditProfileScreen
import ru.alexandrros.petly.presentation.profile.Profile
import ru.alexandrros.petly.presentation.requests.RequestDetailScreen
import ru.alexandrros.petly.presentation.requests.RequestsScreen
import ru.alexandrros.petly.presentation.specialists.SpecialistsScreen
import ru.alexandrros.petly.presentation.userpets.AddEditPetScreen
import ru.alexandrros.petly.presentation.userpets.PetDetailScreen
import ru.alexandrros.petly.presentation.userpets.UserPets
import ru.alexandrros.petly.presentation.userpets.PetListViewModel
import ru.alexandrros.petly.presentation.requests.RequestDetailViewModel
import ru.alexandrros.petly.presentation.requests.RequestViewModel
import ru.alexandrros.petly.presentation.profile.ProfileViewModel
import ru.alexandrros.petly.presentation.specialists.SpecialistDetailScreen
import ru.alexandrros.petly.presentation.specialists.SpecialistDetailViewModel
import ru.alexandrros.petly.presentation.specialists.SpecialistListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    profileViewModelFactory: ProfileViewModel.Factory,
    petListViewModelFactory: ViewModelProvider.Factory,
    outerNavController: NavController,
    requestViewModelFactory: ViewModelProvider.Factory,
    requestDetailViewModelFactory: (String) -> ViewModelProvider.Factory,
    petDetailViewModelFactory: (String) -> ViewModelProvider.Factory,
    editProfileViewModelFactory: ViewModelProvider.Factory,
    specialistListViewModelFactory: ViewModelProvider.Factory,
    specialistDetailViewModelFactory: (String) -> ViewModelProvider.Factory
) {
    val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)
    val petListViewModel: PetListViewModel = viewModel(factory = petListViewModelFactory)
    val nestedNavController = rememberNavController()
    val pets by petListViewModel.pets.collectAsState()
    val currentUser by profileViewModel.currentUser.collectAsState()
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
            composable(Screen.Specialists.route) {
                val vm: SpecialistListViewModel = viewModel(factory = specialistListViewModelFactory)
                SpecialistsScreen(
                    viewModel = vm,
                    onSpecialistClick = { specialistId ->
                        nestedNavController.navigate(Screen.SpecialistDetail.createRoute(specialistId))
                    }
                )
            }

            composable(
                route = Screen.SpecialistDetail.route,
                arguments = listOf(navArgument("specialistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val specialistId = backStackEntry.arguments?.getString("specialistId") ?: ""
                val vm: SpecialistDetailViewModel = viewModel(
                    factory = specialistDetailViewModelFactory(specialistId)
                )
                SpecialistDetailScreen(
                    viewModel = vm,
                    onBackClick = { nestedNavController.popBackStack() }
                )
            }

            composable(Screen.Pets.route) {
                UserPets(
                    pets = pets,
                    onPetClick = { pet ->
                        nestedNavController.navigate(Screen.PetDetail.createRoute(pet.id))
                    },
                    onAddClick = { nestedNavController.navigate(Screen.AddPet.route) }
                )
            }

            composable(Screen.Requests.route) {
                val isSpecialist = currentUser?.specialist != null && currentUser?.specialist != "None"
                val requestViewModel: RequestViewModel = viewModel(factory = requestViewModelFactory)

                LaunchedEffect(isSpecialist) {
                    requestViewModel.setSpecialist(isSpecialist)
                }

                RequestsScreen(
                    requestViewModel = requestViewModel,
                    isSpecialist = isSpecialist,
                    onRequestClick = { requestId ->
                        nestedNavController.navigate(Screen.RequestDetail.createRoute(requestId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                Profile(
                    profileViewModel = profileViewModel,
                    onLogout = {
                        profileViewModel.logout()
                        outerNavController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSpecialistToggle = { isSpecialist -> profileViewModel.setSpecialist(isSpecialist) },
                    onEditProfile = { nestedNavController.navigate(Screen.EditProfile.route) }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBackClick = { nestedNavController.popBackStack() },
                    viewModelFactory = editProfileViewModelFactory
                )
            }

            composable(
                route = Screen.PetDetail.route,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                PetDetailScreen(
                    onBackClick = { nestedNavController.popBackStack() },
                    onEditClick = { petToEdit ->
                        nestedNavController.navigate(Screen.EditPet.createRoute(petToEdit.id))
                    },
                    viewModelFactory = petDetailViewModelFactory(petId)
                )
            }

            composable(Screen.AddPet.route) {
                AddEditPetScreen(
                    pet = null,
                    onSave = { newPet ->
                        petListViewModel.addPet(newPet)
                        nestedNavController.popBackStack()
                    },
                    onCancel = { nestedNavController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditPet.route,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
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

            composable(
                route = Screen.RequestDetail.route,
                arguments = listOf(navArgument("requestId") { type = NavType.StringType })
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                val viewModel: RequestDetailViewModel = viewModel(
                    factory = requestDetailViewModelFactory(requestId)
                )
                RequestDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { nestedNavController.popBackStack() },
                    onSpecialistClick = { specialistId ->
                        nestedNavController.navigate(Screen.SpecialistDetail.createRoute(specialistId))
                    }
                )
            }
        }
    }
}