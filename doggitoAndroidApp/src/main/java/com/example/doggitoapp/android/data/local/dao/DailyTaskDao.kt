package com.example.doggitoapp.android.data.local.dao

import androidx.room.*
import com.example.doggitoapp.android.data.local.entity.DailyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE userId = :userId AND assignedDate = :date ORDER BY category")
    fun getTasksByDate(userId: String, date: Long): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): Flow<DailyTaskEntity?>

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE userId = :userId AND assignedDate = :date AND isCompleted = 1")
    fun getCompletedCountByDate(userId: String, date: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE userId = :userId AND assignedDate = :date")
    fun getTotalCountByDate(userId: String, date: Long): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM daily_tasks WHERE userId = :userId AND assignedDate = :date)")
    suspend fun hasTasksForDate(userId: String, date: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTaskEntity>)

    @Query("UPDATE daily_tasks SET isCompleted = 1, completedAt = :completedAt, synced = 0 WHERE id = :taskId")
    suspend fun completeTask(taskId: String, completedAt: Long)

    @Query("SELECT * FROM daily_tasks WHERE synced = 0")
    suspend fun getUnsyncedTasks(): List<DailyTaskEntity>

    @Query("UPDATE daily_tasks SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
