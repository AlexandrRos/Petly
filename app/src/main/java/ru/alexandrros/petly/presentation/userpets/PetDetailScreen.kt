package ru.alexandrros.petly.presentation.userpets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.alexandrros.petly.domain.model.Pet


@Composable
fun PetDetailScreen(pet: Pet) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pet.photoRes != null) {
            Image(
                painter = painterResource(id = pet.photoRes),
                contentDescription = "Фото питомца",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = pet.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Основные данные
        DetailRow("Вид", pet.species)
        pet.breed?.let { DetailRow("Порода", it) }
        pet.age?.let { DetailRow("Возраст", "$it лет") }
        pet.weight?.let { DetailRow("Вес", "$it кг") }
        DetailRow("Пол", if (pet.isMale) "Мужской" else "Женский")
        pet.sterilizationStatus?.let {
            DetailRow("Стерилизация", if (it) "Да" else "Нет")
        }

        // Списки
        if (pet.vaccinations.isNotEmpty()) {
            DetailList("Вакцинации", pet.vaccinations)
        }
        if (pet.chronicDiseases.isNotEmpty()) {
            DetailList("Хронические заболевания", pet.chronicDiseases)
        }
        if (pet.allergies.isNotEmpty()) {
            DetailList("Аллергии", pet.allergies)
        }
        if (pet.personalityTraits.isNotEmpty()) {
            DetailList("Особенности характера", pet.personalityTraits)
        }
        pet.feedingSchedule?.let { DetailRow("Режим кормления", it) }
        pet.walkingSchedule?.let { DetailRow("Расписание прогулок", it) }
        if (pet.medications.isNotEmpty()) {
            DetailList("Принимаемые лекарства", pet.medications)
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
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DetailList(label: String, items: List<String>) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        items.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}