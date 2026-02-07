package com.example.doggitoapp.android.domain.repository

import com.example.doggitoapp.android.domain.model.RedeemCode
import kotlinx.coroutines.flow.Flow

interface RedeemRepository {
    fun getRedeemCodesByUser(userId: String): Flow<List<RedeemCode>>
    fun getActiveRedeemCodes(userId: String): Flow<List<RedeemCode>>
    fun getRedeemCodeById(redeemId: String): Flow<RedeemCode?>
    suspend fun createRedeemCode(userId: String, productId: String, storeId: String): RedeemCode
    suspend fun expireOldCodes()
}
