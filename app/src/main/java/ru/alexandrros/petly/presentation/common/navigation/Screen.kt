package ru.alexandrros.petly.presentation.common.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Registration : Screen("registration")
    data object Main : Screen("main")
    data object Pets : Screen("pets")
    data object Specialists : Screen("specialists")
    data object Profile : Screen("profile")
    data object Requests : Screen("requests")
    data object AddPet : Screen("add_pet")
    data object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: String) = "pet_detail/$petId"
    }
    data object EditPet : Screen("edit_pet/{petId}") {
        fun createRoute(petId: String) = "edit_pet/$petId"
    }
    data object RequestDetail : Screen("request_detail/{requestId}") {
        fun createRoute(requestId: String) = "request_detail/$requestId"
    }
    object EditProfile : Screen("edit_profile")
    data object SpecialistDetail : Screen("specialist_detail/{specialistId}") {
        fun createRoute(specialistId: String) = "specialist_detail/$specialistId"
    }
}