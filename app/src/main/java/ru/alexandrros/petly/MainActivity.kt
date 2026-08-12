package ru.alexandrros.petly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alexandrros.petly.data.repository.FirebasePetRepository
import ru.alexandrros.petly.data.repository.FirebaseRequestRepository
import ru.alexandrros.petly.data.repository.FirebaseUserRepository
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.usecase.AcceptRequestUseCase
import ru.alexandrros.petly.domain.usecase.AddPetUseCase
import ru.alexandrros.petly.domain.usecase.CheckExistingRequestUseCase
import ru.alexandrros.petly.domain.usecase.CreateRequestUseCase
import ru.alexandrros.petly.domain.usecase.DeletePetUseCase
import ru.alexandrros.petly.domain.usecase.DeleteRequestUseCase
import ru.alexandrros.petly.domain.usecase.GetAllRequestsUseCase
import ru.alexandrros.petly.domain.usecase.GetAllSpecialistsUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByUserUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestsByCreatorUseCase
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase
import ru.alexandrros.petly.domain.usecase.IsUserLoggedInUseCase
import ru.alexandrros.petly.domain.usecase.LoginUseCase
import ru.alexandrros.petly.domain.usecase.LogoutUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.ObservePetByUserUseCase
import ru.alexandrros.petly.domain.usecase.ObserveUserPetsUseCase
import ru.alexandrros.petly.domain.usecase.RegisterUseCase
import ru.alexandrros.petly.domain.usecase.UpdatePetUseCase
import ru.alexandrros.petly.domain.usecase.UpdateSpecialistUseCase
import ru.alexandrros.petly.domain.usecase.UpdateUserPhotoUseCase
import ru.alexandrros.petly.domain.usecase.UpdateUserProfileUseCase
import ru.alexandrros.petly.presentation.common.navigation.AppNavGraph
import ru.alexandrros.petly.presentation.common.navigation.Screen
import ru.alexandrros.petly.presentation.common.theme.PetlyTheme
import ru.alexandrros.petly.presentation.login.LoginViewModel
import ru.alexandrros.petly.presentation.profile.EditProfileViewModel
import ru.alexandrros.petly.presentation.profile.ProfileViewModel
import ru.alexandrros.petly.presentation.registration.RegisterViewModel
import ru.alexandrros.petly.presentation.requests.RequestDetailViewModel
import ru.alexandrros.petly.presentation.requests.RequestsViewModel
import ru.alexandrros.petly.presentation.specialists.SpecialistDetailViewModel
import ru.alexandrros.petly.presentation.specialists.SpecialistListViewModel
import ru.alexandrros.petly.presentation.userpets.AddEditPetViewModel
import ru.alexandrros.petly.presentation.userpets.PetDetailViewModel
import ru.alexandrros.petly.presentation.userpets.PetsViewModel

class MainActivity : ComponentActivity() {

    // Data‑layer dependencies (lazy, created only when needed)
    private val userRepository by lazy { FirebaseUserRepository() }
    private val requestRepository by lazy { FirebaseRequestRepository() }
    private val userIdFlow: Flow<String> by lazy {
        userRepository.getCurrentUser().map { it?.uid ?: "" }
    }
    private val petRepository by lazy {
        FirebasePetRepository(userIdFlow = userIdFlow)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            PetlyTheme {
                // ---------- Use cases (no remember needed for top‑level repos) ----------
                val loginUseCase = LoginUseCase(userRepository)
                val registerUseCase = RegisterUseCase(userRepository)

                val observeCurrentUserUseCase = ObserveCurrentUserUseCase(userRepository)
                val logoutUseCase = LogoutUseCase(userRepository)
                val updateSpecialistUseCase = UpdateSpecialistUseCase(userRepository)
                val getUserByIdUseCase = GetUserByIdUseCase(userRepository)

                val observeUserPetsUseCase = ObserveUserPetsUseCase(petRepository)
                val getPetByIdUseCase = GetPetByIdUseCase(petRepository)
                val addPetUseCase = AddPetUseCase(petRepository)
                val updatePetUseCase = UpdatePetUseCase(petRepository)
                val deletePetUseCase = DeletePetUseCase(petRepository)
                val getPetByUserUseCase = GetPetByUserUseCase(petRepository)
                //for PetDetailScreen update after editing
                val observePetByUserUseCase = ObservePetByUserUseCase(petRepository)

                val getRequestByIdUseCase = GetRequestByIdUseCase(requestRepository)
                val acceptRequestUseCase = AcceptRequestUseCase(requestRepository)
                val deleteRequestUseCase = DeleteRequestUseCase(requestRepository)
                val getRequestsByCreatorUseCase = GetRequestsByCreatorUseCase(requestRepository)
                val getAllRequestsUseCase = GetAllRequestsUseCase(requestRepository)
                val checkExistingRequestUseCase = CheckExistingRequestUseCase(requestRepository)
                val createRequestUseCase = CreateRequestUseCase(requestRepository)

                val isUserLoggedInUseCase = IsUserLoggedInUseCase(userRepository)
                val updateUserProfileUseCase = UpdateUserProfileUseCase(userRepository)
                val updateUserPhotoUseCase = UpdateUserPhotoUseCase(userRepository)

                val getAllSpecialistsUseCase = GetAllSpecialistsUseCase(userRepository)

                // ---------- ViewModel factories ----------
                val loginFactory = LoginViewModel.provideFactory(loginUseCase)
                val registerFactory = RegisterViewModel.provideFactory(registerUseCase)

                val profileViewModelFactory = ProfileViewModel.Factory(
                    observeCurrentUserUseCase,
                    logoutUseCase,
                    updateSpecialistUseCase,
                    updateUserPhotoUseCase
                )
                val editProfileViewModelFactory = EditProfileViewModel.Factory(
                    observeCurrentUserUseCase,
                    updateUserProfileUseCase
                )

                val petsViewModelFactory = PetsViewModel.Factory(
                    observeUserPetsUseCase,
                    getPetByIdUseCase,
                    addPetUseCase,
                    updatePetUseCase,
                    deletePetUseCase
                )

                val requestsViewModelFactory = RequestsViewModel.Factory(
                    observeCurrentUserUseCase,
                    getRequestsByCreatorUseCase,
                    getAllRequestsUseCase,
                    getPetByUserUseCase
                )

                val requestDetailViewModelFactory: (String) -> ViewModelProvider.Factory = { requestId ->
                    RequestDetailViewModel.Factory(
                        requestId,
                        getRequestByIdUseCase,
                        acceptRequestUseCase,
                        deleteRequestUseCase,
                        getPetByUserUseCase,
                        getUserByIdUseCase,
                        observeCurrentUserUseCase
                    )
                }

                val petDetailViewModelFactory: (String) -> ViewModelProvider.Factory = { petId ->
                    PetDetailViewModel.Factory(
                        petId,
                        observeCurrentUserUseCase,
                        observePetByUserUseCase,
                        checkExistingRequestUseCase,
                        createRequestUseCase
                    )
                }

                val addEditPetViewModelFactory: (Pet?) -> ViewModelProvider.Factory = { initialPet ->
                    AddEditPetViewModel.Factory(
                        initialPet,
                        observeCurrentUserUseCase,
                        addPetUseCase,
                        updatePetUseCase
                    )
                }

                val specialistListViewModelFactory = SpecialistListViewModel.Factory(
                    getAllSpecialistsUseCase
                )
                val specialistDetailViewModelFactory: (String) -> ViewModelProvider.Factory =
                    { id ->
                        SpecialistDetailViewModel.Factory(
                            id, getUserByIdUseCase
                        )
                    }

                var startDestination by remember {
                    mutableStateOf(
                        if (isUserLoggedInUseCase()) Screen.Main.route
                        else Screen.Login.route
                    )
                }

                //For possible log-out outside app
                LaunchedEffect(Unit) {
                    observeCurrentUserUseCase().collect { user ->
                        startDestination = if (user != null) Screen.Main.route
                        else Screen.Login.route
                    }
                }

                // ---------- Navigation ----------
                AppNavGraph(
                    startDestination = startDestination,
                    loginViewModelFactory = loginFactory,
                    registerViewModelFactory = registerFactory,
                    profileViewModelFactory = profileViewModelFactory,
                    petListViewModelFactory = petsViewModelFactory,
                    addEditPetViewModelFactory = addEditPetViewModelFactory,
                    requestViewModelFactory = requestsViewModelFactory,
                    requestDetailViewModelFactory = requestDetailViewModelFactory,
                    petDetailViewModelFactory = petDetailViewModelFactory,
                    editProfileViewModelFactory = editProfileViewModelFactory,
                    specialistListViewModelFactory = specialistListViewModelFactory,
                    specialistDetailViewModelFactory = specialistDetailViewModelFactory
                )
            }
        }
    }
}