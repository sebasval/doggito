package com.example.doggitoapp.android.domain.model

enum class ProductCategory(val displayName: String) {
    SNACKS("Snacks"),
    COLLARS("Collares"),
    BEDS("Camas"),
    TOYS("Juguetes"),
    ACCESSORIES("Accesorios"),
    OTHER("Otros")
}

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val priceCoins: Int,
    val category: ProductCategory,
    val isAvailable: Boolean = true
)
