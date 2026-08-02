package ru.alexandrros.petly.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import ru.alexandrros.petly.presentation.common.components.DetailRow
import ru.alexandrros.petly.presentation.common.components.SectionCard
import coil.request.ImageRequest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    profileViewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onSpecialistToggle: (Boolean) -> Unit,
    onEditProfile: () -> Unit = {}
) {
    val user by profileViewModel.currentUser.collectAsState()
    val isUpdatingPhoto by profileViewModel.isUpdatingPhoto.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { profileViewModel.updateProfilePhoto(it, context) }
    }

    LaunchedEffect(Unit) {
        profileViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Профиль", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (user == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка данных…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            val currentUser = user!!
            val isSpecialist = currentUser.specialist != null && currentUser.specialist != "None"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(120.dp)
                ) {
                    if (currentUser.photoBytes != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentUser.photoBytes)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Фото профиля",
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
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        enabled = !isUpdatingPhoto,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        if (isUpdatingPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Сменить фото",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentUser.name.ifEmpty { "Не указано" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentUser.email,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionCard(title = "Основная информация") {
                    DetailRow("Имя", currentUser.name.ifEmpty { "Не указано" })
                    DetailRow("Email", currentUser.email)
                    DetailRow("UID", currentUser.uid)
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionCard(title = "Статус") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Статус специалиста: ${
                                if (isSpecialist) "Ветеринар" else "Нет"
                            }",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isSpecialist,
                            onCheckedChange = onSpecialistToggle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Выйти")
                }
            }
        }
    }
}
