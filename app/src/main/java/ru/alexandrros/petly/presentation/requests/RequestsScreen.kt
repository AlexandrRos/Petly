package ru.alexandrros.petly.presentation.requests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alexandrros.petly.domain.model.Request

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    requestViewModel: RequestViewModel,
    isSpecialist: Boolean,
    onRequestClick: (requestId: String) -> Unit
) {
    val requests by requestViewModel.requests.collectAsState()
    val showMyRequests by requestViewModel.showMyRequests.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { Text("Заявки") },
                actions = {
                    if (isSpecialist) {
                        TextButton(onClick = { requestViewModel.toggleView() }) {
                            Text(
                                text = if (showMyRequests) "Мои заявки" else "Все заявки",
                                color = MaterialTheme.colorScheme.primary
                            )
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
        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (!isSpecialist || showMyRequests) "У вас пока нет заявок"
                    else "Нет заявок от других пользователей",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests, key = { it.id }) { request ->
                    RequestCard(
                        request = request,
                        onClick = { onRequestClick(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestCard(request: Request, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Питомец: ${request.petName}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Вид: ${request.species}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (request.specialistUserId != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Принята специалистом",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}