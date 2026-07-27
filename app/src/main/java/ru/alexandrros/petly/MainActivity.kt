package ru.alexandrros.petly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.google.firebase.FirebaseApp
import ru.alexandrros.petly.data.repository.FirebaseUserRepository
import ru.alexandrros.petly.domain.usecase.LoginUseCase
import ru.alexandrros.petly.domain.usecase.RegisterUseCase
import ru.alexandrros.petly.presentation.common.navigation.AppNavGraph
import ru.alexandrros.petly.presentation.common.theme.PetlyTheme
import ru.alexandrros.petly.presentation.viewmodel.LoginViewModel
import ru.alexandrros.petly.presentation.viewmodel.PetListViewModel
import ru.alexandrros.petly.presentation.viewmodel.RegisterViewModel
import ru.alexandrros.petly.presentation.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    private val userRepository by lazy { FirebaseUserRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            PetlyTheme {
                val loginUseCase = remember { LoginUseCase(userRepository) }
                val registerUseCase = remember { RegisterUseCase(userRepository) }

                val loginFactory = remember { LoginViewModel.provideFactory(loginUseCase) }
                val registerFactory = remember { RegisterViewModel.provideFactory(registerUseCase) }
                val userViewModelFactory = remember { UserViewModel.Factory(userRepository) }
                val petListViewModelFactory = remember { PetListViewModel.Factory(userRepository) }   // new

                AppNavGraph(
                    loginViewModelFactory = loginFactory,
                    registerViewModelFactory = registerFactory,
                    userViewModelFactory = userViewModelFactory,
                    petListViewModelFactory = petListViewModelFactory   // new
                )
            }
        }
    }
}