package ru.alexandrros.petly.presentation.common.navigation

// Добавляем новые маршруты в sealed class Screen
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object Pets : Screen("pets")
    data object Specialists : Screen("specialists")
    data object Profile : Screen("profile")
    data object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: Int) = "pet_detail/$petId"
    }
    data object AddPet : Screen("add_pet")
    data object EditPet : Screen("edit_pet/{petId}") {
        fun createRoute(petId: Int) = "edit_pet/$petId"
    }
}