package ru.alexandrros.petly.presentation.common

/**
 * Returns true if the string represents a valid specialist value.
 * Treats null, blank, or the literal "None" as not a specialist.
 */
fun String?.isSpecialistValue(): Boolean =
    !this.isNullOrBlank() && !this.equals("None", ignoreCase = true)