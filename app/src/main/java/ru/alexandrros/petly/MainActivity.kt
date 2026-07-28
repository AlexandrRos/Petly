package ru.alexandrros.petly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alexandrros.petly.data.repository.FirebasePetRepository
import ru.alexandrros.petly.data.repository.FirebaseRequestRepository
import ru.alexandrros.petly.data.repository.FirebaseUserRepository
import ru.alexandrros.petly.domain.usecase.AcceptRequestUseCase
import ru.alexandrros.petly.domain.usecase.AddPetUseCase
import ru.alexandrros.petly.domain.usecase.CheckExistingRequestUseCase
import ru.alexandrros.petly.domain.usecase.CreateRequestUseCase
import ru.alexandrros.petly.domain.usecase.DeletePetUseCase
import ru.alexandrros.petly.domain.usecase.DeleteRequestUseCase
import ru.alexandrros.petly.domain.usecase.GetAllRequestsUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByUserUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestsByCreatorUseCase
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase
import ru.alexandrros.petly.domain.usecase.LoginUseCase
import ru.alexandrros.petly.domain.usecase.LogoutUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.ObserveUserPetsUseCase
import ru.alexandrros.petly.domain.usecase.RegisterUseCase
import ru.alexandrros.petly.domain.usecase.UpdatePetUseCase
import ru.alexandrros.petly.domain.usecase.UpdateSpecialistUseCase
import ru.alexandrros.petly.presentation.common.navigation.AppNavGraph
import ru.alexandrros.petly.presentation.common.theme.PetlyTheme
import ru.alexandrros.petly.presentation.login.LoginViewModel
import ru.alexandrros.petly.presentation.userpets.PetListViewModel
import ru.alexandrros.petly.presentation.registration.RegisterViewModel
import ru.alexandrros.petly.presentation.requests.RequestDetailViewModel
import ru.alexandrros.petly.presentation.requests.RequestViewModel
import ru.alexandrros.petly.presentation.profile.ProfileViewModel
import ru.alexandrros.petly.presentation.userpets.PetDetailViewModel

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

                val getRequestByIdUseCase = GetRequestByIdUseCase(requestRepository)
                val acceptRequestUseCase = AcceptRequestUseCase(requestRepository)
                val deleteRequestUseCase = DeleteRequestUseCase(requestRepository)
                val getRequestsByCreatorUseCase = GetRequestsByCreatorUseCase(requestRepository)
                val getAllRequestsUseCase = GetAllRequestsUseCase(requestRepository)
                val checkExistingRequestUseCase = CheckExistingRequestUseCase(requestRepository)
                val createRequestUseCase = CreateRequestUseCase(requestRepository)

                // ---------- ViewModel factories ----------
                val loginFactory = LoginViewModel.provideFactory(loginUseCase)
                val registerFactory = RegisterViewModel.provideFactory(registerUseCase)

                val profileViewModelFactory = ProfileViewModel.Factory(
                    observeCurrentUserUseCase,
                    logoutUseCase,
                    updateSpecialistUseCase
                )
                // Single instance of UserViewModel for the whole app
                val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)

                val petListViewModelFactory = PetListViewModel.Factory(
                    observeUserPetsUseCase,
                    getPetByIdUseCase,
                    addPetUseCase,
                    updatePetUseCase,
                    deletePetUseCase
                )

                val requestViewModelFactory = RequestViewModel.Factory(
                    observeCurrentUserUseCase,
                    getRequestsByCreatorUseCase,
                    getAllRequestsUseCase
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
                        getPetByUserUseCase,
                        checkExistingRequestUseCase,
                        createRequestUseCase
                    )
                }

                // ---------- Navigation ----------
                AppNavGraph(
                    loginViewModelFactory = loginFactory,
                    registerViewModelFactory = registerFactory,
                    profileViewModel = profileViewModel,  // note: renamed to profileViewModel
                    petListViewModelFactory = petListViewModelFactory,
                    requestViewModelFactory = requestViewModelFactory,
                    requestDetailViewModelFactory = requestDetailViewModelFactory,
                    petDetailViewModelFactory = petDetailViewModelFactory
                )
            }
        }
    }
}