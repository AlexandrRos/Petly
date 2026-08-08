package ru.alexandrros.petly.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OutlinedAssistChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier.padding(vertical = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    )
}