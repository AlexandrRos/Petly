package ru.alexandrros.petly.presentation.requests

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.alexandrros.petly.presentation.common.components.DetailList
import ru.alexandrros.petly.presentation.common.components.DetailRow
import ru.alexandrros.petly.presentation.common.components.SectionCard


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
    val coroutineScope = rememberCoroutineScope()

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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val contentWindowInsets = if (isLandscape) {
        WindowInsets.safeDrawing
    } else {
        WindowInsets.systemBars.only(WindowInsetsSides.Top)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = contentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text("Заявка", style = MaterialTheme.typography.headlineSmall) },
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
                text = req.petName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = req.species,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionCard(title = "Информация о заявке") {
                DetailRow("Создана", formatTimestamp(req.createdAt))
                DetailRow(
                    "Статус",
                    if (req.specialistUserId != null) "Принята специалистом" else "Ожидает специалиста"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            pet?.let { currentPet ->
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
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } ?: run {
                SectionCard(title = "Информация о питомце") {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val specialist = specialistUser
            if (req.specialistUserId != null && specialist != null) {
                SectionCard(
                    title = "Специалист",
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("not yet implemented")
                        }
                    }
                ) {
                    DetailRow("Email", specialist.email)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (canAccept) {
                Button(
                    onClick = { viewModel.acceptRequest() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Принять заявку")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
        }
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