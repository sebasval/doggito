package com.example.doggitoapp.android.data.repository

import com.example.doggitoapp.android.data.local.dao.RedeemCodeDao
import com.example.doggitoapp.android.data.local.entity.RedeemCodeEntity
import com.example.doggitoapp.android.domain.model.RedeemCode
import com.example.doggitoapp.android.domain.model.RedeemStatus
import com.example.doggitoapp.android.domain.repository.RedeemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RedeemRepositoryImpl(
    private val redeemDao: RedeemCodeDao
) : RedeemRepository {

    override fun getRedeemCodesByUser(userId: String): Flow<List<RedeemCode>> =
        redeemDao.getRedeemCodesByUser(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getActiveRedeemCodes(userId: String): Flow<List<RedeemCode>> =
        redeemDao.getActiveRedeemCodes(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getRedeemCodeById(redeemId: String): Flow<RedeemCode?> =
        redeemDao.getRedeemCodeById(redeemId).map { it?.toDomain() }

    override suspend fun createRedeemCode(userId: String, productId: String, storeId: String): RedeemCode {
        val id = UUID.randomUUID().toString()
        val alphaCode = generateAlphaCode()
        val now = System.currentTimeMillis()
        val expiresAt = now + (30L * 24 * 60 * 60 * 1000) // 30 days

        val entity = RedeemCodeEntity(
            id = id,
            userId = userId,
            productId = productId,
            code = alphaCode,
            qrData = "DOGGITO:$id:$alphaCode",
            storeId = storeId,
            status = RedeemStatus.ACTIVE.name,
            createdAt = now,
            expiresAt = expiresAt
        )
        redeemDao.insertRedeemCode(entity)
        return entity.toDomain()
    }

    override suspend fun expireOldCodes() {
        redeemDao.expireOldCodes(System.currentTimeMillis())
    }

    private fun generateAlphaCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
}
