package ru.alexandrros.petly.presentation.registration


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onRegisterClick: (email: String, password: String, name: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onResetState: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            email = ""
            password = ""
            name = ""
            onResetState()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Регистрация",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isError && uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { onRegisterClick(email, password, name) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Зарегистрироваться")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Уже есть аккаунт? Войти")
            }
        }
    }
}