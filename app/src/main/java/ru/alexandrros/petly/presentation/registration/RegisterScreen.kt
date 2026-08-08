package ru.alexandrros.petly.presentation.registration


import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.alexandrros.petly.presentation.common.components.rememberContentInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onRegisterClick: (email: String, password: String, name: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onResetState: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            email = ""
            password = ""
            name = ""
            onResetState()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val contentInsets = rememberContentInsets()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(contentInsets)
        ) {
            if (isLandscape) {
                LandscapeContent(
                    email = email,
                    password = password,
                    name = name,
                    uiState = uiState,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onNameChange = { name = it },
                    onRegisterClick = onRegisterClick,
                    onNavigateToLogin = onNavigateToLogin
                )
            } else {
                PortraitContent(
                    email = email,
                    password = password,
                    name = name,
                    uiState = uiState,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onNameChange = { name = it },
                    onRegisterClick = onRegisterClick,
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}

@Composable
private fun PortraitContent(
    email: String,
    password: String,
    name: String,
    uiState: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Регистрация",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Создайте новый аккаунт",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterForm(
                email = email,
                password = password,
                name = name,
                uiState = uiState,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onNameChange = onNameChange,
                onRegisterClick = onRegisterClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Уже есть аккаунт? Войти",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LandscapeContent(
    email: String,
    password: String,
    name: String,
    uiState: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Регистрация",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Создайте новый аккаунт",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Уже есть аккаунт? Войти",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterForm(
                email = email,
                password = password,
                name = name,
                uiState = uiState,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onNameChange = onNameChange,
                onRegisterClick = onRegisterClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterForm(
    email: String,
    password: String,
    name: String,
    uiState: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Имя") },
                leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Имя") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(imageVector = Icons.Filled.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Пароль") },
                leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = "Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank()) {
                            onRegisterClick(email, password, name)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = uiState.isError && uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onRegisterClick(email, password, name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Зарегистрироваться", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}