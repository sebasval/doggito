package com.example.doggitoapp.android.domain.model

data class Streak(
    val userId: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: Long = 0L
)
