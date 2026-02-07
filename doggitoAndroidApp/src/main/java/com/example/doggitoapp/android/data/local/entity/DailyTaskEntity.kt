package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.DailyTask
import com.example.doggitoapp.android.domain.model.TaskCategory

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: String,
    val rewardCoins: Int,
    val isCompleted: Boolean = false,
    val assignedDate: Long,
    val completedAt: Long? = null,
    val synced: Boolean = false
) {
    fun toDomain() = DailyTask(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = TaskCategory.valueOf(category),
        rewardCoins = rewardCoins,
        isCompleted = isCompleted,
        assignedDate = assignedDate,
        completedAt = completedAt,
        synced = synced
    )

    companion object {
        fun fromDomain(task: DailyTask) = DailyTaskEntity(
            id = task.id,
            userId = task.userId,
            title = task.title,
            description = task.description,
            category = task.category.name,
            rewardCoins = task.rewardCoins,
            isCompleted = task.isCompleted,
            assignedDate = task.assignedDate,
            completedAt = task.completedAt,
            synced = task.synced
        )
    }
}
