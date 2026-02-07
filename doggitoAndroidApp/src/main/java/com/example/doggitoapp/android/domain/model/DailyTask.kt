package com.example.doggitoapp.android.domain.model

enum class TaskCategory(val displayName: String) {
    BASIC_CARE("Cuidado básico"),
    HEALTH("Salud"),
    EXERCISE("Ejercicio y actividad"),
    TRAINING("Entrenamiento y socialización"),
    WELLNESS("Bienestar emocional")
}

data class DailyTask(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: TaskCategory,
    val rewardCoins: Int,
    val isCompleted: Boolean = false,
    val assignedDate: Long,
    val completedAt: Long? = null,
    val synced: Boolean = false
)
