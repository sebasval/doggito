package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.Streak

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey val userId: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: Long = 0L
) {
    fun toDomain() = Streak(
        userId = userId,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastCompletedDate = lastCompletedDate
    )

    companion object {
        fun fromDomain(streak: Streak) = StreakEntity(
            userId = streak.userId,
            currentStreak = streak.currentStreak,
            longestStreak = streak.longestStreak,
            lastCompletedDate = streak.lastCompletedDate
        )
    }
}
