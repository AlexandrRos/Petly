package ru.alexandrros.petly.presentation.requests

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.presentation.common.components.DetailList
import ru.alexandrros.petly.presentation.common.components.DetailRow
import ru.alexandrros.petly.presentation.common.components.OutlinedAssistChip
import ru.alexandrros.petly.presentation.common.components.SectionCard
import ru.alexandrros.petly.presentation.common.components.rememberContentInsets
import ru.alexandrros.petly.presentation.common.formatDate
import ru.alexandrros.petly.presentation.common.formatTimestamp
import ru.alexandrros.petly.presentation.common.toYearsWord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    requestId: String,
    onBackClick: () -> Unit,
    onUserClick: (userId: String) -> Unit,
    viewModel: RequestDetailViewModel = koinViewModel(
        parameters = { parametersOf(requestId) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val contentInsets = rememberContentInsets()

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState.isLoading, uiState.request) {
        if (!uiState.isLoading && uiState.request == null) {
            onBackClick()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = contentInsets,
        topBar = {
            TopAppBar(
                title = { Text("Заявка", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (uiState.isOwner) {
                        when (uiState.deletionState) {
                            DeletionState.IDLE, DeletionState.ERROR -> {
                                IconButton(onClick = { viewModel.deleteRequest() }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить заявку"
                                    )
                                }
                            }
                            DeletionState.DELETING -> {
                                IconButton(
                                    onClick = {},
                                    enabled = false
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            DeletionState.SUCCESS -> {
                                IconButton(
                                    onClick = {},
                                    enabled = false
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Удалено",
                                        tint = Color.Green
                                    )
                                }
                            }
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.deletionState == DeletionState.SUCCESS) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLandscape) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 40.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Успешно",
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxSize(),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Заявка удалена",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Вы можете вернуться назад и продолжить работу с другими заявками.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onBackClick,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Вернуться к заявкам")
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(96.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Успешно",
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Заявка удалена",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Вы можете вернуться назад и продолжить работу с другими заявками.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = onBackClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Вернуться к заявкам")
                        }
                    }
                }
            }
            return@Scaffold
        }

        val req = uiState.request
        if (req == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Заявка не найдена", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val petPhotoBytes = uiState.pet?.photoBytes
        val specialist = uiState.specialistUser
        val ownerUser = uiState.ownerUser

        val isSpecialistCurrentUser = req.specialistUserId != null && req.specialistUserId == uiState.currentUserId
        val showOwner = uiState.currentUserId != null && uiState.currentUserId != req.creatorUserId

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLandscape) {
                if (showOwner) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                if (petPhotoBytes != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(petPhotoBytes)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Фото питомца",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
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
                            Spacer(modifier = Modifier.weight(1f))
                            SectionCard(
                                title = "Владелец",
                                onClick = {
                                    onUserClick(req.creatorUserId)
                                }
                            ) {
                                if (ownerUser == null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    UserRow(
                                        user = ownerUser,
                                        onClick = {
                                            onUserClick(req.creatorUserId)
                                        },
                                        avatarSize = 40.dp,
                                        label = null,
                                        showArrow = true
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            SectionCard(title = "Информация о заявке") {
                                DetailRow("Создана", formatTimestamp(req.createdAt))
                                DetailRow(
                                    "Статус",
                                    when (req.status) {
                                        Request.STATUS_PENDING -> "Ожидает специалиста"
                                        Request.STATUS_ACCEPTED -> "Принята специалистом"
                                        Request.STATUS_CONFIRMED -> "Подтверждена"
                                        else -> req.status
                                    }
                                )
                                DetailRow("Город", req.city)
                                req.cost?.let { DetailRow("Стоимость", "$it") }
                                DetailRow("Дата начала", formatDate(req.startDate))
                                DetailRow("Дата окончания", formatDate(req.endDate))

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Text(
                                    text = "Специалист",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                SpecialistDisplay(
                                    request = req,
                                    specialist = specialist,
                                    isSpecialistCurrentUser = isSpecialistCurrentUser,
                                    onUserClick = onUserClick
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                if (petPhotoBytes != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(petPhotoBytes)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Фото питомца",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
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
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            SectionCard(title = "Информация о заявке") {
                                DetailRow("Создана", formatTimestamp(req.createdAt))
                                DetailRow(
                                    "Статус",
                                    when (req.status) {
                                        Request.STATUS_PENDING -> "Ожидает специалиста"
                                        Request.STATUS_ACCEPTED -> "Принята специалистом"
                                        Request.STATUS_CONFIRMED -> "Подтверждена"
                                        else -> req.status
                                    }
                                )
                                DetailRow("Город", req.city)
                                req.cost?.let { DetailRow("Стоимость", "$it") }
                                DetailRow("Дата начала", formatDate(req.startDate))
                                DetailRow("Дата окончания", formatDate(req.endDate))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionCard(title = "Специалист") {
                        SpecialistDisplay(
                            request = req,
                            specialist = specialist,
                            isSpecialistCurrentUser = isSpecialistCurrentUser,
                            onUserClick = onUserClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (petPhotoBytes != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(petPhotoBytes)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Фото питомца",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
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
                        when (req.status) {
                            Request.STATUS_PENDING -> "Ожидает специалиста"
                            Request.STATUS_ACCEPTED -> "Принята специалистом"
                            Request.STATUS_CONFIRMED -> "Подтверждена"
                            else -> req.status
                        }
                    )
                    DetailRow("Город", req.city)
                    req.cost?.let { DetailRow("Стоимость", "$it") }
                    DetailRow("Дата начала", formatDate(req.startDate))
                    DetailRow("Дата окончания", formatDate(req.endDate))
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (req.specialistUserId != null && specialist != null) {
                    SectionCard(
                        title = "Специалист",
                        onClick = {
                            if (!isSpecialistCurrentUser) {
                                onUserClick(req.specialistUserId)
                            }
                        }
                    ) {
                        UserRow(
                            user = specialist,
                            onClick = if (isSpecialistCurrentUser) {
                                {}
                            } else {
                                { onUserClick(req.specialistUserId) }
                            },
                            avatarSize = 48.dp,
                            label = specialist.specialist,
                            showArrow = !isSpecialistCurrentUser,
                            extraLabel = if (isSpecialistCurrentUser) "Вы" else null
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (showOwner && !isLandscape) {
                SectionCard(
                    title = "Владелец",
                    onClick = {
                        onUserClick(req.creatorUserId)
                    }
                ) {
                    if (ownerUser == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        UserRow(
                            user = ownerUser,
                            onClick = {
                                onUserClick(req.creatorUserId)
                            },
                            avatarSize = 48.dp,
                            label = null,
                            showArrow = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            uiState.pet?.let { currentPet ->
                SectionCard(title = "Основная информация") {
                    DetailRow("Вид", currentPet.species)
                    currentPet.breed?.let { DetailRow("Порода", it) }
                    currentPet.age?.let { DetailRow("Возраст", "$it ${it.toYearsWord()}") }
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
                            OutlinedAssistChip(text = trait)
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
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.canAccept) {
                Button(
                    onClick = { viewModel.acceptRequest() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Принять заявку")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.canConfirm) {
                Button(
                    onClick = { viewModel.confirmRequest() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Подтвердить заявку")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.canDismiss) {
                OutlinedButton(
                    onClick = { viewModel.dismissRequest() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Отклонить специалиста")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SpecialistDisplay(
    request: Request,
    specialist: User?,
    isSpecialistCurrentUser: Boolean,
    onUserClick: (String) -> Unit
) {
    if (request.specialistUserId != null && specialist != null) {
        UserRow(
            user = specialist,
            onClick = if (isSpecialistCurrentUser) {
                {}
            } else {
                { onUserClick(request.specialistUserId) }
            },
            avatarSize = 40.dp,
            label = specialist.specialist,
            showArrow = !isSpecialistCurrentUser,
            extraLabel = if (isSpecialistCurrentUser) "Вы" else null
        )
    } else if (request.specialistUserId != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Специалист ещё не назначен",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun UserRow(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 48.dp,
    label: String? = null,
    showArrow: Boolean = true,
    extraLabel: String? = null
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(avatarSize),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (user.photoBytes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(user.photoBytes)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Фото пользователя",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(avatarSize * 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifEmpty { user.email },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (user.name.isNotEmpty()) {
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (label != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (extraLabel != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = extraLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Подробнее",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Вы",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}