package ru.alexandrros.petly.presentation.userpets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.presentation.common.components.OutlinedFilterChip
import ru.alexandrros.petly.presentation.common.components.SectionCard
import ru.alexandrros.petly.presentation.common.components.rememberContentInsets
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPetScreen(
    pet: Pet?,
    onSave: (Pet) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(pet?.name ?: "") }
    var species by remember { mutableStateOf(pet?.species ?: "") }
    var breed by remember { mutableStateOf(pet?.breed ?: "") }
    var age by remember { mutableStateOf(pet?.age?.toString() ?: "") }
    var weight by remember { mutableStateOf(pet?.weight?.toString() ?: "") }
    var isMale by remember { mutableStateOf(pet?.isMale ?: true) }
    var sterilizationStatus by remember { mutableStateOf(pet?.sterilizationStatus ?: false) }

    var vaccinationsText by remember { mutableStateOf(pet?.vaccinations?.joinToString(", ") ?: "") }
    var chronicDiseasesText by remember { mutableStateOf(pet?.chronicDiseases?.joinToString(", ") ?: "") }
    var allergiesText by remember { mutableStateOf(pet?.allergies?.joinToString(", ") ?: "") }
    var personalityTraitsText by remember { mutableStateOf(pet?.personalityTraits?.joinToString(", ") ?: "") }
    var medicationsText by remember { mutableStateOf(pet?.medications?.joinToString(", ") ?: "") }

    var feedingSchedule by remember { mutableStateOf(pet?.feedingSchedule ?: "") }
    var walkingSchedule by remember { mutableStateOf(pet?.walkingSchedule ?: "") }

    var photoBytes by remember { mutableStateOf(pet?.photoBytes) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bytes = readAndCompressPhoto(context, it, maxSizeKB = 100)
            photoBytes = bytes
        }
    }

    val contentInsets = rememberContentInsets()

    Scaffold(
        contentWindowInsets = contentInsets,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (pet == null) "Добавить питомца" else "Редактировать",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Отмена")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val newPet = Pet(
                            id = pet?.id ?: "",
                            userId = pet?.userId ?: "",
                            name = name,
                            species = species,
                            breed = breed.ifBlank { null },
                            age = age.toIntOrNull(),
                            weight = weight.toDoubleOrNull(),
                            isMale = isMale,
                            sterilizationStatus = sterilizationStatus,
                            vaccinations = vaccinationsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            chronicDiseases = chronicDiseasesText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            allergies = allergiesText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            personalityTraits = personalityTraitsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            feedingSchedule = feedingSchedule.ifBlank { null },
                            walkingSchedule = walkingSchedule.ifBlank { null },
                            medications = medicationsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            photoBytes = photoBytes
                        )
                        onSave(newPet)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Сохранить")
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
                if (photoBytes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoBytes)
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

            if (photoBytes != null) {
                TextButton(
                    onClick = { photoBytes = null },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Удалить фото")
                }
            }

            SectionCard(title = "Основная информация") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Вид") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Порода (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Возраст (лет)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Вес (кг)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Пол",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedFilterChip(
                        selected = isMale,
                        onClick = { isMale = true },
                        text ="Мужской"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedFilterChip(
                        selected = !isMale,
                        onClick = { isMale = false },
                        text ="Женский"
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
                        checked = sterilizationStatus,
                        onCheckedChange = { sterilizationStatus = it }
                    )
                }
            }

            SectionCard(title = "Здоровье") {
                OutlinedTextField(
                    value = vaccinationsText,
                    onValueChange = { vaccinationsText = it },
                    label = { Text("Вакцинации (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = chronicDiseasesText,
                    onValueChange = { chronicDiseasesText = it },
                    label = { Text("Хронические заболевания (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = allergiesText,
                    onValueChange = { allergiesText = it },
                    label = { Text("Аллергии (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = medicationsText,
                    onValueChange = { medicationsText = it },
                    label = { Text("Лекарства (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(title = "Характер") {
                OutlinedTextField(
                    value = personalityTraitsText,
                    onValueChange = { personalityTraitsText = it },
                    label = { Text("Особенности характера (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(title = "Расписание") {
                OutlinedTextField(
                    value = feedingSchedule,
                    onValueChange = { feedingSchedule = it },
                    label = { Text("Режим кормления") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = walkingSchedule,
                    onValueChange = { walkingSchedule = it },
                    label = { Text("Расписание прогулок") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun readAndCompressPhoto(context: Context, uri: Uri, maxSizeKB: Int): ByteArray? {
    return try {
        val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        rawBytes?.let { compressBytes(it, maxSizeKB * 1024) }
    } catch (e: Exception) {
        Log.e("AddEditPet", "Failed to read/compress image", e)
        null
    }
}

private fun compressBytes(bytes: ByteArray, maxSizeBytes: Int): ByteArray {
    if (bytes.size <= maxSizeBytes) return bytes
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
    var quality = 80
    var output: ByteArray
    do {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        output = baos.toByteArray()
        quality -= 10
    } while (output.size > maxSizeBytes && quality > 10)
    bitmap.recycle()
    return output
}