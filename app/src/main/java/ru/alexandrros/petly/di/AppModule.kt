package ru.alexandrros.petly.di


import kotlinx.coroutines.flow.map
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.alexandrros.petly.data.repository.FirebasePetRepository
import ru.alexandrros.petly.data.repository.FirebaseRequestRepository
import ru.alexandrros.petly.data.repository.FirebaseUserRepository
import ru.alexandrros.petly.data.repository.MockUserRatingRepository
import ru.alexandrros.petly.data.repository.ThemeRepositoryImpl
import ru.alexandrros.petly.domain.repository.PetRepository
import ru.alexandrros.petly.domain.repository.RequestRepository
import ru.alexandrros.petly.domain.repository.ThemeRepository
import ru.alexandrros.petly.domain.repository.UserRatingRepository
import ru.alexandrros.petly.domain.repository.UserRepository
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
import ru.alexandrros.petly.domain.usecase.GetThemeModeUseCase
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetUserRatingUseCase
import ru.alexandrros.petly.domain.usecase.GetUserReviewsUseCase
import ru.alexandrros.petly.domain.usecase.IsUserLoggedInUseCase
import ru.alexandrros.petly.domain.usecase.LoginUseCase
import ru.alexandrros.petly.domain.usecase.LogoutUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.ObservePetByUserUseCase
import ru.alexandrros.petly.domain.usecase.ObserveUserPetsUseCase
import ru.alexandrros.petly.domain.usecase.RegisterUseCase
import ru.alexandrros.petly.domain.usecase.SetThemeModeUseCase
import ru.alexandrros.petly.domain.usecase.UpdatePetUseCase
import ru.alexandrros.petly.domain.usecase.UpdateSpecialistUseCase
import ru.alexandrros.petly.domain.usecase.UpdateUserPhotoUseCase
import ru.alexandrros.petly.domain.usecase.UpdateUserProfileUseCase
import ru.alexandrros.petly.presentation.login.LoginViewModel
import ru.alexandrros.petly.presentation.profile.EditProfileViewModel
import ru.alexandrros.petly.presentation.profile.ProfileViewModel
import ru.alexandrros.petly.presentation.registration.RegisterViewModel
import ru.alexandrros.petly.presentation.requests.RequestDetailViewModel
import ru.alexandrros.petly.presentation.requests.RequestsViewModel
import ru.alexandrros.petly.users.UserDetailViewModel
import ru.alexandrros.petly.presentation.specialists.SpecialistListViewModel
import ru.alexandrros.petly.presentation.userpets.AddEditPetViewModel
import ru.alexandrros.petly.presentation.userpets.PetDetailViewModel
import ru.alexandrros.petly.presentation.userpets.PetsViewModel

val appModule = module {

    // ---------- Repositories ----------

    single<UserRepository> {
        FirebaseUserRepository()
    }

    single<RequestRepository> {
        FirebaseRequestRepository()
    }

    single<PetRepository> {
        FirebasePetRepository(
            userIdFlow = get<UserRepository>()
                .getCurrentUser()
                .map { it?.uid ?: "" }
        )
    }

    single<ThemeRepository> {
        ThemeRepositoryImpl(androidContext())
    }

    single<UserRatingRepository> { MockUserRatingRepository() }

    // ---------- Use cases ----------

    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ObserveCurrentUserUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { UpdateSpecialistUseCase(get()) }
    factory { GetUserByIdUseCase(get()) }

    factory { ObserveUserPetsUseCase(get()) }
    factory { GetPetByIdUseCase(get()) }
    factory { AddPetUseCase(get()) }
    factory { UpdatePetUseCase(get()) }
    factory { DeletePetUseCase(get()) }
    factory { GetPetByUserUseCase(get()) }
    factory { ObservePetByUserUseCase(get()) }

    factory { GetRequestByIdUseCase(get()) }
    factory { AcceptRequestUseCase(get()) }
    factory { DeleteRequestUseCase(get()) }
    factory { GetRequestsByCreatorUseCase(get()) }
    factory { GetAllRequestsUseCase(get()) }
    factory { CheckExistingRequestUseCase(get()) }
    factory { CreateRequestUseCase(get()) }

    factory { IsUserLoggedInUseCase(get()) }
    factory { UpdateUserProfileUseCase(get()) }
    factory { UpdateUserPhotoUseCase(get()) }
    factory { GetAllSpecialistsUseCase(get()) }
    factory { GetThemeModeUseCase(get()) }
    factory { SetThemeModeUseCase(get()) }

    factory { GetUserRatingUseCase(get()) }
    factory { GetUserReviewsUseCase(get()) }

    // ---------- ViewModels ----------

    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }

    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get()) }

    viewModel { EditProfileViewModel(get(), get()) }

    viewModel { PetsViewModel(get()) }

    viewModel { RequestsViewModel(get(), get(), get(), get()) }

    viewModel { (requestId: String) ->
        RequestDetailViewModel(
            requestId = requestId,
            getRequestByIdUseCase = get(),
            acceptRequestUseCase = get(),
            deleteRequestUseCase = get(),
            getPetByUserUseCase = get(),
            getUserByIdUseCase = get(),
            observeCurrentUserUseCase = get()
        )
    }

    viewModel { (petId: String) ->
        PetDetailViewModel(
            petId = petId,
            observeCurrentUserUseCase = get(),
            observePetByUserUseCase = get(),
            checkExistingRequestUseCase = get(),
            createRequestUseCase = get()
        )
    }

    viewModel { (petId: String?) ->
        AddEditPetViewModel(
            petId = petId,
            observeCurrentUserUseCase = get(),
            getPetByIdUseCase = get(),
            addPetUseCase = get(),
            updatePetUseCase = get()
        )
    }

    viewModel { SpecialistListViewModel(get()) }

    viewModel { (userId: String) ->
        UserDetailViewModel(
            userId = userId,
            getUserByIdUseCase = get(),
            getUserRatingUseCase = get(),
            getUserReviewsUseCase = get()
        )
    }
}