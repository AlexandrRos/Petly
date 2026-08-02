package ru.alexandrros.petly.presentation.userpets

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.presentation.common.components.DetailList
import ru.alexandrros.petly.presentation.common.components.DetailRow
import ru.alexandrros.petly.presentation.common.components.SectionCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    onBackClick: () -> Unit,
    onEditClick: (Pet) -> Unit,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: PetDetailViewModel = viewModel(factory = viewModelFactory)
    val snackbarHostState = remember { SnackbarHostState() }
    val pet by viewModel.pet.collectAsState()
    val isCreating by viewModel.isCreatingRequest.collectAsState()


    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val currentWindowInsets = if (isLandscape) {
        WindowInsets.safeDrawing
    } else {
        WindowInsets.systemBars.only(WindowInsetsSides.Top)
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = currentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text(pet?.name ?: "", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    pet?.let { currentPet ->
                        IconButton(onClick = { onEditClick(currentPet) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        pet?.let { currentPet ->
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
                    text = currentPet.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentPet.species + if (currentPet.breed != null) " • ${currentPet.breed}" else "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionCard(title = "Основная информация") {
                    DetailRow("Вид", currentPet.species)
                    currentPet.breed?.let { DetailRow("Порода", it) }
                    currentPet.age?.let { DetailRow("Возраст", "$it лет") }
                    currentPet.weight?.let { DetailRow("Вес", "$it кг") }
                    DetailRow("Пол", if (currentPet.isMale) "Мужской" else "Женский")
                    currentPet.sterilizationStatus?.let {
                        DetailRow("Стерилизация", if (it) "Да" else "Нет")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val healthItems = listOfNotNull(
                    if (currentPet.vaccinations.isNotEmpty()) "Вакцинации" to currentPet.vaccinations else null,
                    if (currentPet.chronicDiseases.isNotEmpty()) "Хронические заболевания" to currentPet.chronicDiseases else null,
                    if (currentPet.allergies.isNotEmpty()) "Аллергии" to currentPet.allergies else null,
                    if (currentPet.medications.isNotEmpty()) "Принимаемые лекарства" to currentPet.medications else null
                )
                if (healthItems.isNotEmpty()) {
                    SectionCard(title = "Здоровье") {
                        healthItems.forEach { (label, items) ->
                            DetailList(label, items)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (currentPet.personalityTraits.isNotEmpty()) {
                    SectionCard(title = "Характер") {
                        currentPet.personalityTraits.forEach { trait ->
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
                    currentPet.feedingSchedule?.let { "Режим кормления" to it },
                    currentPet.walkingSchedule?.let { "Расписание прогулок" to it }
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
                        viewModel.createRequest(
                            petId = currentPet.id,
                            petName = currentPet.name,
                            species = currentPet.species
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Загрузка…")
                    } else {
                        Text("Запросить помощь")
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Питомец не найден", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}