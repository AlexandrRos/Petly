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

/**
 * Returns the correct Russian word for “years” based on the given age.
 * Example: 1 -> "год", 3 -> "года", 5 -> "лет", 21 -> "год", 11 -> "лет".
 */
fun Int.toYearsWord(): String {
    val lastDigit = this % 10
    val lastTwoDigits = this % 100
    return when {
        lastTwoDigits in 11..14 -> "лет"
        lastDigit == 1 -> "год"
        lastDigit in 2..4 -> "года"
        else -> "лет"
    }
}