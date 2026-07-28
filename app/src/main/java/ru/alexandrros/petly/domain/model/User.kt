package ru.alexandrros.petly.domain.model

data class User(
    val uid: String,
    val email: String,
    val name: String,
    val specialist: String? = null      // "None" for non‑specialists
)