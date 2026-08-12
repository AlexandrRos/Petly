package ru.alexandrros.petly.presentation.userpets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ru.alexandrros.petly.presentation.common.components.OutlinedFilterChip
import ru.alexandrros.petly.presentation.common.components.SectionCard
import ru.alexandrros.petly.presentation.common.components.rememberContentInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPetScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddEditPetViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collect {
            onNavigateBack()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updatePhoto(it, context) }
    }

    val contentInsets = rememberContentInsets()

    Scaffold(
        contentWindowInsets = contentInsets,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isNew) "Добавить питомца" else "Редактировать",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Отмена")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.savePet() }) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Сохранить")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                if (uiState.photoBytes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.photoBytes)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Фото питомца",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
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
                }

                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Сменить фото",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (uiState.photoBytes != null) {
                TextButton(
                    onClick = { viewModel.updateState { it.copy(photoBytes = null) } },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Удалить фото")
                }
            }

            SectionCard(title = "Основная информация") {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateState { state -> state.copy(name = it) } },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.species,
                    onValueChange = { viewModel.updateState { state -> state.copy(species = it) } },
                    label = { Text("Вид") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.breed,
                    onValueChange = { viewModel.updateState { state -> state.copy(breed = it) } },
                    label = { Text("Порода (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.ageText,
                    onValueChange = { viewModel.updateState { state -> state.copy(ageText = it) } },
                    label = { Text("Возраст (лет)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.weightText,
                    onValueChange = { viewModel.updateState { state -> state.copy(weightText = it) } },
                    label = { Text("Вес (кг)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Пол", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedFilterChip(
                        selected = uiState.isMale,
                        onClick = { viewModel.updateState { it.copy(isMale = true) } },
                        text = "Мужской"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedFilterChip(
                        selected = !uiState.isMale,
                        onClick = { viewModel.updateState { it.copy(isMale = false) } },
                        text = "Женский"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Стерилизация",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.sterilizationStatus,
                        onCheckedChange = { viewModel.updateState {  state -> state.copy(sterilizationStatus = it) } }
                    )
                }
            }

            SectionCard(title = "Здоровье") {
                OutlinedTextField(
                    value = uiState.vaccinationsText,
                    onValueChange = { viewModel.updateState { state -> state.copy(vaccinationsText = it) } },
                    label = { Text("Вакцинации (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.chronicDiseasesText,
                    onValueChange = { viewModel.updateState { state -> state.copy(chronicDiseasesText = it) } },
                    label = { Text("Хронические заболевания (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.allergiesText,
                    onValueChange = { viewModel.updateState { state -> state.copy(allergiesText = it) } },
                    label = { Text("Аллергии (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.medicationsText,
                    onValueChange = { viewModel.updateState { state -> state.copy(medicationsText = it) } },
                    label = { Text("Лекарства (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(title = "Характер") {
                OutlinedTextField(
                    value = uiState.personalityTraitsText,
                    onValueChange = { viewModel.updateState { state -> state.copy(personalityTraitsText = it) } },
                    label = { Text("Особенности характера (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(title = "Расписание") {
                OutlinedTextField(
                    value = uiState.feedingSchedule,
                    onValueChange = { viewModel.updateState { state -> state.copy(feedingSchedule = it) } },
                    label = { Text("Режим кормления") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.walkingSchedule,
                    onValueChange = { viewModel.updateState { state -> state.copy(walkingSchedule = it) } },
                    label = { Text("Расписание прогулок") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}