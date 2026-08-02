package ru.alexandrros.petly.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: EditProfileViewModel = viewModel(factory = viewModelFactory)
    val name by viewModel.name.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Редактировать профиль", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = currentUser?.email ?: "",
                onValueChange = {},
                label = { Text("Email") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранение…")
                } else {
                    Text("Сохранить")
                }
            }
        }
    }
}