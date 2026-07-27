package ru.alexandrros.petly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.usecase.LoginUseCase

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)  // сбрасываем предыдущие ошибки/успех
            loginUseCase(email, password)
                .onSuccess {
                    _uiState.value = LoginUiState(isSuccess = true)
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.value = LoginUiState(
                        isError = true,
                        errorMessage = throwable.message ?: "Ошибка входа"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }

    companion object {
        fun provideFactory(loginUseCase: LoginUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(loginUseCase) as T
                }
            }
    }
}