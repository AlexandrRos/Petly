package ru.alexandrros.petly.presentation.mainscreen


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.alexandrros.petly.presentation.common.components.LocalBottomBarHeight
import ru.alexandrros.petly.presentation.common.navigation.Screen
import ru.alexandrros.petly.presentation.mainscreen.model.TabInfo
import ru.alexandrros.petly.presentation.profile.EditProfileScreen
import ru.alexandrros.petly.presentation.profile.ProfileScreen
import ru.alexandrros.petly.presentation.requests.RequestDetailScreen
import ru.alexandrros.petly.presentation.requests.RequestsScreen
import ru.alexandrros.petly.presentation.specialists.SpecialistDetailScreen
import ru.alexandrros.petly.presentation.specialists.SpecialistsScreen
import ru.alexandrros.petly.presentation.userpets.AddEditPetScreen
import ru.alexandrros.petly.presentation.userpets.PetDetailScreen
import ru.alexandrros.petly.presentation.userpets.PetsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    outerNavController: NavController
) {
    val nestedNavController = rememberNavController()
    val bottomNavItems = listOf(
        TabInfo(Screen.Pets, "Ваши питомцы", Icons.Filled.Pets),
        TabInfo(Screen.Requests, "Заявки", Icons.Filled.Newspaper),
        TabInfo(Screen.Specialists, "Специалисты", Icons.Filled.Search),
        TabInfo(Screen.Profile, "Аккаунт", Icons.Filled.AccountCircle)
    )

    // Safe pop: only pops if there is a previous entry (avoid popping root)
    val safePopBackStack: () -> Unit = {
        if (nestedNavController.previousBackStackEntry != null) {
            nestedNavController.popBackStack()
        }
    }

    // Root routes of each tab – used to detect when on a root screen
    val rootRoutes = setOf(
        Screen.Pets.route,
        Screen.Requests.route,
        Screen.Specialists.route,
        Screen.Profile.route
    )

    // Intercept system back press when on a root tab
    BackHandler(enabled = rootRoutes.contains(
        nestedNavController.currentBackStackEntry?.destination?.route
    )) {
        if (outerNavController.previousBackStackEntry != null) {
            outerNavController.popBackStack()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine which parent tab the current route belongs to
                fun isTabSelected(tab: Screen): Boolean = when (tab) {
                    Screen.Pets -> currentRoute in setOf(
                        Screen.Pets.route,
                        Screen.PetDetail.route,
                        Screen.AddPet.route,
                        Screen.EditPet.route
                    )
                    Screen.Requests -> currentRoute in setOf(
                        Screen.Requests.route,
                        Screen.RequestDetail.route
                    )
                    Screen.Specialists -> currentRoute in setOf(
                        Screen.Specialists.route,
                        Screen.SpecialistDetail.route
                    )
                    Screen.Profile -> currentRoute in setOf(
                        Screen.Profile.route,
                        Screen.EditProfile.route
                    )
                    else -> currentRoute == tab.route
                }

                bottomNavItems.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = isTabSelected(tab.screen),
                        onClick = {
                            // If already on the root of this tab -> do nothing
                            if (currentRoute == tab.screen.route) {
                                return@NavigationBarItem
                            }
                            // If on a sub-screen of this tab -> pop to root
                            else if (isTabSelected(tab.screen)) {
                                nestedNavController.popBackStack(
                                    tab.screen.route,
                                    inclusive = false
                                )
                            }
                            // Otherwise switch to a different tab (save/restore state)
                            else {
                                nestedNavController.navigate(tab.screen.route) {
                                    popUpTo(nestedNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
        val bottomBarHeightDp = innerPadding.calculateBottomPadding()
        CompositionLocalProvider(LocalBottomBarHeight provides bottomBarHeightDp) {
            NavHost(
                navController = nestedNavController,
                startDestination = Screen.Pets.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Specialists.route) {
                    SpecialistsScreen(
                        onSpecialistClick = { specialistId ->
                            nestedNavController.navigate(
                                Screen.SpecialistDetail.createRoute(specialistId)
                            )
                        }
                    )
                }

                composable(
                    route = Screen.SpecialistDetail.route,
                    arguments = listOf(navArgument("specialistId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val specialistId = backStackEntry.arguments?.getString("specialistId") ?: ""
                    SpecialistDetailScreen(
                        specialistId = specialistId,
                        onBackClick = safePopBackStack
                    )
                }

                composable(Screen.Pets.route) {
                    PetsScreen(
                        onPetClick = { pet ->
                            nestedNavController.navigate(Screen.PetDetail.createRoute(pet.id))
                        },
                        onAddClick = { nestedNavController.navigate(Screen.AddPet.route) }
                    )
                }

                composable(Screen.Requests.route) {
                    RequestsScreen(
                        onRequestClick = { requestId ->
                            nestedNavController.navigate(Screen.RequestDetail.createRoute(requestId))
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            outerNavController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onEditProfile = { nestedNavController.navigate(Screen.EditProfile.route) }
                    )
                }

                composable(Screen.EditProfile.route) {
                    EditProfileScreen(
                        onBackClick = safePopBackStack
                    )
                }

                composable(
                    route = Screen.PetDetail.route,
                    arguments = listOf(navArgument("petId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val petId = backStackEntry.arguments?.getString("petId") ?: ""
                    PetDetailScreen(
                        petId = petId,
                        onBackClick = safePopBackStack,
                        onEditClick = { petToEdit ->
                            nestedNavController.navigate(Screen.EditPet.createRoute(petToEdit.id))
                        }
                    )
                }

                composable(Screen.AddPet.route) {
                    AddEditPetScreen(
                        petId = null,
                        onNavigateBack = safePopBackStack
                    )
                }

                composable(
                    route = Screen.EditPet.route,
                    arguments = listOf(navArgument("petId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val petId = backStackEntry.arguments?.getString("petId") ?: ""
                    AddEditPetScreen(
                        petId = petId,
                        onNavigateBack = safePopBackStack
                    )
                }

                composable(
                    route = Screen.RequestDetail.route,
                    arguments = listOf(navArgument("requestId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                    RequestDetailScreen(
                        requestId = requestId,
                        onBackClick = safePopBackStack,
                        onSpecialistClick = { specialistId ->
                            nestedNavController.navigate(
                                Screen.SpecialistDetail.createRoute(specialistId)
                            )
                        }
                    )
                }
            }
        }
    }
}