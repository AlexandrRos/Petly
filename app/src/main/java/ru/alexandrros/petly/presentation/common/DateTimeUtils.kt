package ru.alexandrros.petly.presentation.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        millis.toString()
    }
}