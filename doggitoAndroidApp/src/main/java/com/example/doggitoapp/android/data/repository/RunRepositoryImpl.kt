package com.example.doggitoapp.android.data.repository

import com.example.doggitoapp.android.data.local.dao.RunSessionDao
import com.example.doggitoapp.android.data.local.entity.RunSessionEntity
import com.example.doggitoapp.android.domain.model.RunningSession
import com.example.doggitoapp.android.domain.repository.RunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RunRepositoryImpl(
    private val runDao: RunSessionDao
) : RunRepository {

    override fun getSessionsByUser(userId: String): Flow<List<RunningSession>> =
        runDao.getSessionsByUser(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getSessionById(sessionId: String): Flow<RunningSession?> =
        runDao.getSessionById(sessionId).map { it?.toDomain() }

    override fun getTotalDistance(userId: String): Flow<Float> =
        runDao.getTotalDistance(userId)

    override fun getSessionCount(userId: String): Flow<Int> =
        runDao.getSessionCount(userId)

    override suspend fun saveSession(session: RunningSession) {
        runDao.insertSession(RunSessionEntity.fromDomain(session))
    }
}
