package com.example.doggitoapp.android.data.local.dao

import androidx.room.*
import com.example.doggitoapp.android.data.local.entity.RunSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunSessionDao {
    @Query("SELECT * FROM running_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getSessionsByUser(userId: String): Flow<List<RunSessionEntity>>

    @Query("SELECT * FROM running_sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: String): Flow<RunSessionEntity?>

    @Query("SELECT COALESCE(SUM(distanceMeters), 0) FROM running_sessions WHERE userId = :userId")
    fun getTotalDistance(userId: String): Flow<Float>

    @Query("SELECT COUNT(*) FROM running_sessions WHERE userId = :userId")
    fun getSessionCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: RunSessionEntity)

    @Update
    suspend fun updateSession(session: RunSessionEntity)

    @Query("SELECT * FROM running_sessions WHERE synced = 0")
    suspend fun getUnsyncedSessions(): List<RunSessionEntity>

    @Query("UPDATE running_sessions SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
