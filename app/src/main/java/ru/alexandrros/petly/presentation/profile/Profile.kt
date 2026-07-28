package ru.alexandrros.petly.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alexandrros.petly.domain.model.User

@Composable
fun Profile(
    user: User?,
    onLogout: () -> Unit,
    onSpecialistToggle: (Boolean) -> Unit
) {
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading user...",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    } else {
        val isSpecialist = user.specialist != null && user.specialist != "None"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Name: ${user.name.ifEmpty { "Not set" }}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Email: ${user.email}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "UID: ${user.uid}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Статус специалиста: ${if (isSpecialist) "Ветеринар" else "Нет"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isSpecialist,
                    onCheckedChange = { checked -> onSpecialistToggle(checked) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Выйти")
            }
        }
    }
}