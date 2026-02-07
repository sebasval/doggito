package com.example.doggitoapp.android.domain.repository

import com.example.doggitoapp.android.domain.model.DailyTask
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksByDate(userId: String, date: Long): Flow<List<DailyTask>>
    fun getCompletedCountByDate(userId: String, date: Long): Flow<Int>
    fun getTotalCountByDate(userId: String, date: Long): Flow<Int>
    suspend fun hasTasksForDate(userId: String, date: Long): Boolean
    suspend fun generateDailyTasks(userId: String, date: Long)
    suspend fun completeTask(taskId: String)
}
