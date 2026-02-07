package com.example.doggitoapp.android.domain.repository

import com.example.doggitoapp.android.domain.model.RunningSession
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    fun getSessionsByUser(userId: String): Flow<List<RunningSession>>
    fun getSessionById(sessionId: String): Flow<RunningSession?>
    fun getTotalDistance(userId: String): Flow<Float>
    fun getSessionCount(userId: String): Flow<Int>
    suspend fun saveSession(session: RunningSession)
}
