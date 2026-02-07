package com.example.doggitoapp.android.domain.model

enum class RunningMode(val displayName: String) {
    WALK("Caminar"),
    RUN("Correr"),
    HIKE("Senderismo")
}

data class RunningSession(
    val id: String,
    val userId: String,
    val petId: String,
    val distanceMeters: Float,
    val durationMillis: Long,
    val avgSpeedKmh: Float,
    val caloriesUser: Float,
    val caloriesDog: Float,
    val mode: RunningMode,
    val routeJson: String = "[]",
    val coinsEarned: Int,
    val startedAt: Long,
    val endedAt: Long,
    val synced: Boolean = false
)
