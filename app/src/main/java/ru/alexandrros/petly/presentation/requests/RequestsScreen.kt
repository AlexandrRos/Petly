package ru.alexandrros.petly.presentation.requests

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koin.androidx.compose.koinViewModel
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.model.UserRating
import ru.alexandrros.petly.presentation.common.components.rememberContentInsets
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    requestsViewModel: RequestsViewModel = koinViewModel(),
    onRequestClick: (requestId: String) -> Unit
) {
    val uiState by requestsViewModel.uiState.collectAsState()
    val contentInsets = rememberContentInsets()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заявки", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    if (uiState.isSpecialist) {
                        TextButton(onClick = { requestsViewModel.cycleViewMode() }) {
                            Text(
                                text = when (uiState.viewMode) {
                                    RequestListViewMode.MY_REQUESTS -> "Мои заявки"
                                    RequestListViewMode.AVAILABLE -> "Доступные"
                                    RequestListViewMode.ACCEPTED_BY_ME -> "Принятые мной"
                                },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { requestsViewModel.showFilterDialog() },
                            enabled = uiState.viewMode == RequestListViewMode.AVAILABLE,
                            modifier = Modifier.alpha(
                                if (uiState.viewMode == RequestListViewMode.AVAILABLE) 1f else 0.4f
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Фильтры",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        contentWindowInsets = contentInsets
    ) { innerPadding ->
        if (uiState.requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        !uiState.isSpecialist -> "У вас пока нет заявок"
                        uiState.viewMode == RequestListViewMode.MY_REQUESTS -> "У вас пока нет заявок"
                        uiState.viewMode == RequestListViewMode.ACCEPTED_BY_ME -> "Нет заявок, принятых вами"
                        else -> {
                            val hasActiveFilters = uiState.cityFilter.isNotBlank() ||
                                    uiState.minCost.isNotBlank() ||
                                    uiState.maxCost.isNotBlank() ||
                                    uiState.minRating > 0f
                            if (hasActiveFilters) {
                                "Нет заявок, соответствующих фильтрам. Попробуйте изменить фильтры."
                            } else {
                                "Нет доступных заявок"
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isLandscape) 2 else 1),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.requests, key = { it.id }) { request ->
                    RequestCard(
                        request = request,
                        photoBytes = uiState.photoCache[request.id],
                        ownerRating = uiState.ratings[request.creatorUserId],
                        showOwnerRating = uiState.viewMode != RequestListViewMode.MY_REQUESTS,
                        onClick = { onRequestClick(request.id) }
                    )
                }
            }
        }
    }

    if (uiState.isFilterDialogVisible && uiState.viewMode == RequestListViewMode.AVAILABLE) {
        AlertDialog(
            onDismissRequest = { requestsViewModel.hideFilterDialog() },
            title = { Text("Фильтры") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = uiState.cityFilter,
                        onValueChange = { requestsViewModel.updateCityFilter(it) },
                        label = { Text("Город") },
                        placeholder = { Text("Введите город") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.minCost,
                        onValueChange = { requestsViewModel.updateMinCost(it) },
                        label = { Text("Мин. стоимость") },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.maxCost,
                        onValueChange = { requestsViewModel.updateMaxCost(it) },
                        label = { Text("Макс. стоимость") },
                        placeholder = { Text("Любая") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Мин. рейтинг: ${uiState.minRating}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = uiState.minRating,
                            onValueChange = { requestsViewModel.updateMinRating(it) },
                            valueRange = 0f..5f,
                            steps = 9,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { requestsViewModel.hideFilterDialog() }) {
                    Text("Применить")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { requestsViewModel.resetFilters() }) {
                        Text("Сбросить")
                    }
                    TextButton(onClick = { requestsViewModel.hideFilterDialog() }) {
                        Text("Отмена")
                    }
                }
            }
        )
    }
}

@Composable
private fun RequestCard(
    request: Request,
    photoBytes: ByteArray?,
    ownerRating: UserRating?,
    showOwnerRating: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (photoBytes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoBytes)
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
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.petName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = request.species,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (request.status) {
                        Request.STATUS_PENDING -> "Ожидает специалиста"
                        Request.STATUS_ACCEPTED -> "Принята специалистом"
                        Request.STATUS_CONFIRMED -> "Подтверждена"
                        else -> request.status
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (request.status) {
                        Request.STATUS_PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                        Request.STATUS_ACCEPTED -> MaterialTheme.colorScheme.secondary
                        Request.STATUS_CONFIRMED -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${request.city.ifEmpty { "Город не указан" }} • ${request.cost?.let { "%.2f".format(it) } ?: "Нет оплаты"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showOwnerRating) {
                    if (ownerRating != null && ownerRating.totalCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < ownerRating.average.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (index < ownerRating.average.toInt()) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", ownerRating.average),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Нет оценок",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Подробнее",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}