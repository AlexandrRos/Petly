package ru.alexandrros.petly.presentation.common.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OutlinedFilterChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean,
    labelModifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    enabled: Boolean = true
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = text,
                textAlign = textAlign,
                modifier = labelModifier
            )
        },
        modifier = modifier.padding(vertical = 2.dp),
        border = FilterChipDefaults.filterChipBorder(
            selected = selected,
            enabled = true,
            borderColor = MaterialTheme.colorScheme.outline
        ),
        selected = selected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = enabled
    )
}