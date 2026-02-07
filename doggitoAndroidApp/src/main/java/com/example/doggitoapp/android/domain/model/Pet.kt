package com.example.doggitoapp.android.domain.model

data class Pet(
    val id: String,
    val userId: String,
    val name: String,
    val breed: String,
    val birthDate: Long,
    val weight: Float,
    val photoUri: String? = null,
    val synced: Boolean = false
)
