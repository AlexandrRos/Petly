package ru.alexandrros.petly.presentation.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel


@Composable
fun RegisterRoute(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
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