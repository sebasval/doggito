package com.example.doggitoapp.android.domain.model

data class Store(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val email: String,
    val openingHours: String,
    val imageUrl: String? = null
)
