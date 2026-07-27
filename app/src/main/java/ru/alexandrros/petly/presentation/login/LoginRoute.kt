package ru.alexandrros.petly.presentation.login


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.alexandrros.petly.data.repository.FakeUserRepository
import ru.alexandrros.petly.domain.usecase.LoginUseCase
import ru.alexandrros.petly.presentation.viewmodel.LoginViewModel


@Composable
fun LoginRoute(onLoginSuccess: () -> Unit) {
    val repository = remember { FakeUserRepository() }
    val useCase = remember { LoginUseCase(repository) }
    val factory = remember { LoginViewModel.provideFactory(useCase) }
    val viewModel: LoginViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginScreen(
        uiState = uiState,
        onLoginClick = { email, password ->
            viewModel.login(email, password, onLoginSuccess)
        },
        onResetState = { viewModel.resetState() }
    )
}