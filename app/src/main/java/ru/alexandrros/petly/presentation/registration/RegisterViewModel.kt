package ru.alexandrros.petly.presentation.registration



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.usecase.RegisterUseCase

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            registerUseCase(email, password, name)
                .onSuccess {
                    _uiState.value = RegisterUiState(isSuccess = true)
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.value = RegisterUiState(
                        isError = true,
                        errorMessage = throwable.message ?: "Ошибка регистрации"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}