package com.example.doggitoapp.android.domain.model

enum class TransactionType(val displayName: String) {
    TASK("Tarea completada"),
    RUNNING("Actividad física"),
    STREAK_BONUS("Bonificación de racha"),
    REDEMPTION("Canje de producto"),
    PROMO("Promoción")
}

data class CoinTransaction(
    val id: String,
    val userId: String,
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val createdAt: Long,
    val synced: Boolean = false
)
