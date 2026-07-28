package ru.alexandrros.petly.presentation.userpets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.presentation.common.components.SectionCard
import ru.alexandrros.petly.presentation.viewmodel.RequestViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    pet: Pet,
    onBackClick: () -> Unit,
    onEditClick: (Pet) -> Unit,
    requestViewModelFactory: ViewModelProvider.Factory
) {
    val requestViewModel: RequestViewModel = viewModel(factory = requestViewModelFactory)
    val snackbarHostState = remember { SnackbarHostState() }
    val isCreating by requestViewModel.isCreatingRequest.collectAsState()
    LaunchedEffect(Unit) {
        requestViewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        //TODO: disable insets for album orientation
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { Text(pet.name, style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(pet) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = pet.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = pet.species + if (pet.breed != null) " • ${pet.breed}" else "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionCard(title = "Основная информация") {
                DetailRow("Вид", pet.species)
                pet.breed?.let { DetailRow("Порода", it) }
                pet.age?.let { DetailRow("Возраст", "$it лет") }
                pet.weight?.let { DetailRow("Вес", "$it кг") }
                DetailRow("Пол", if (pet.isMale) "Мужской" else "Женский")
                pet.sterilizationStatus?.let {
                    DetailRow("Стерилизация", if (it) "Да" else "Нет")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val healthItems = listOfNotNull(
                if (pet.vaccinations.isNotEmpty()) "Вакцинации" to pet.vaccinations else null,
                if (pet.chronicDiseases.isNotEmpty()) "Хронические заболевания" to pet.chronicDiseases else null,
                if (pet.allergies.isNotEmpty()) "Аллергии" to pet.allergies else null,
                if (pet.medications.isNotEmpty()) "Принимаемые лекарства" to pet.medications else null
            )
            if (healthItems.isNotEmpty()) {
                SectionCard(title = "Здоровье") {
                    healthItems.forEach { (label, items) ->
                        DetailList(label, items)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (pet.personalityTraits.isNotEmpty()) {
                SectionCard(title = "Характер") {
                    pet.personalityTraits.forEach { trait ->
                        AssistChip(
                            onClick = {},
                            label = { Text(trait) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val scheduleItems = listOfNotNull(
                pet.feedingSchedule?.let { "Режим кормления" to it },
                pet.walkingSchedule?.let { "Расписание прогулок" to it }
            )
            if (scheduleItems.isNotEmpty()) {
                SectionCard(title = "Расписание") {
                    scheduleItems.forEach { (label, value) ->
                        DetailRow(label, value)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    requestViewModel.createRequest(
                        petId = pet.id,
                        petName = pet.name,
                        species = pet.species
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Запросить помощь")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailList(label: String, items: List<String>) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}