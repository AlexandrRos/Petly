package ru.alexandrros.petly.presentation.login


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun LoginRoute(
    factory: ViewModelProvider.Factory,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginScreen(
        uiState = uiState,
        onLoginClick = { email, password ->
            viewModel.login(email, password, onLoginSuccess)
        },
        onNavigateToRegister = onNavigateToRegister,
        onResetState = { viewModel.resetState() }
    )
}