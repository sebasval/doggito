package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.RunningMode
import com.example.doggitoapp.android.domain.model.RunningSession

@Entity(tableName = "running_sessions")
data class RunSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val petId: String,
    val distanceMeters: Float,
    val durationMillis: Long,
    val avgSpeedKmh: Float,
    val caloriesUser: Float,
    val caloriesDog: Float,
    val mode: String,
    val routeJson: String = "[]",
    val coinsEarned: Int,
    val startedAt: Long,
    val endedAt: Long,
    val synced: Boolean = false
) {
    fun toDomain() = RunningSession(
        id = id,
        userId = userId,
        petId = petId,
        distanceMeters = distanceMeters,
        durationMillis = durationMillis,
        avgSpeedKmh = avgSpeedKmh,
        caloriesUser = caloriesUser,
        caloriesDog = caloriesDog,
        mode = RunningMode.valueOf(mode),
        routeJson = routeJson,
        coinsEarned = coinsEarned,
        startedAt = startedAt,
        endedAt = endedAt,
        synced = synced
    )

    companion object {
        fun fromDomain(session: RunningSession) = RunSessionEntity(
            id = session.id,
            userId = session.userId,
            petId = session.petId,
            distanceMeters = session.distanceMeters,
            durationMillis = session.durationMillis,
            avgSpeedKmh = session.avgSpeedKmh,
            caloriesUser = session.caloriesUser,
            caloriesDog = session.caloriesDog,
            mode = session.mode.name,
            routeJson = session.routeJson,
            coinsEarned = session.coinsEarned,
            startedAt = session.startedAt,
            endedAt = session.endedAt,
            synced = session.synced
        )
    }
}
