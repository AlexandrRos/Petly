package ru.alexandrros.petly.presentation.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.alexandrros.petly.domain.model.Pet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    viewModel: RequestDetailViewModel,
    onBackClick: () -> Unit
) {
    val request by viewModel.request.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pet by viewModel.pet.collectAsState()
    val specialistUser by viewModel.specialistUser.collectAsState()
    val canAccept by viewModel.canAccept.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(isLoading, request) {
        if (!isLoading && request == null) {
            onBackClick()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Заявка") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (viewModel.isOwner()) {
                        IconButton(onClick = { viewModel.deleteRequest() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить заявку")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val req = request
        if (req == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Заявка не найдена", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Питомец: ${req.petName}", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Вид: ${req.species}", style = MaterialTheme.typography.bodyLarge)
                    Text("Создана: ${formatTimestamp(req.createdAt)}", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            pet?.let {
                PetInfoCard(pet = it)
            } ?: run {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            val specialist = specialistUser
            if (req.specialistUserId != null && specialist != null) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Специалист", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("Email: ${specialist.email}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (canAccept) {
                Button(
                    onClick = { viewModel.acceptRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Принять заявку")
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
@Composable
private fun PetInfoCard(pet: Pet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Информация о питомце", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            PetInfoRow("Имя", pet.name)
            PetInfoRow("Вид", pet.species)
            if (!pet.breed.isNullOrBlank()) PetInfoRow("Порода", pet.breed)
            if (pet.age != null) PetInfoRow("Возраст", "${pet.age} лет")
            if (pet.weight != null) PetInfoRow("Вес", "${pet.weight} кг")
            PetInfoRow("Пол", if (pet.isMale) "Мужской" else "Женский")
            if (pet.sterilizationStatus != null) PetInfoRow("Стерилизация", if (pet.sterilizationStatus) "Да" else "Нет")

            if (pet.vaccinations.isNotEmpty()) PetInfoRow("Вакцинации", pet.vaccinations.joinToString(", "))
            if (pet.chronicDiseases.isNotEmpty()) PetInfoRow("Хронические болезни", pet.chronicDiseases.joinToString(", "))
            if (pet.allergies.isNotEmpty()) PetInfoRow("Аллергии", pet.allergies.joinToString(", "))
            if (pet.personalityTraits.isNotEmpty()) PetInfoRow("Характер", pet.personalityTraits.joinToString(", "))
            if (pet.medications.isNotEmpty()) PetInfoRow("Лекарства", pet.medications.joinToString(", "))

            if (!pet.feedingSchedule.isNullOrBlank()) PetInfoRow("Режим кормления", pet.feedingSchedule)
            if (!pet.walkingSchedule.isNullOrBlank()) PetInfoRow("Прогулки", pet.walkingSchedule)
        }
    }
}

@Composable
private fun PetInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

private fun formatTimestamp(millis: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(millis))
    } catch (e: Exception) {
        millis.toString()
    }
}