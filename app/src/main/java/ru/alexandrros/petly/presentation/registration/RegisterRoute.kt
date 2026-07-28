package ru.alexandrros.petly.presentation.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun RegisterRoute(
    factory: ViewModelProvider.Factory,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: RegisterViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegisterScreen(
        uiState = uiState,
        onRegisterClick = { email, password, name ->
            viewModel.register(email, password, name, onRegisterSuccess)
        },
        onNavigateToLogin = onNavigateToLogin,
        onResetState = { viewModel.resetState() }
    )
}