package com.example.doggitoapp.android.data.local.dao

import androidx.room.*
import com.example.doggitoapp.android.data.local.entity.RedeemCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RedeemCodeDao {
    @Query("SELECT * FROM redeem_codes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRedeemCodesByUser(userId: String): Flow<List<RedeemCodeEntity>>

    @Query("SELECT * FROM redeem_codes WHERE userId = :userId AND status = 'ACTIVE' ORDER BY expiresAt ASC")
    fun getActiveRedeemCodes(userId: String): Flow<List<RedeemCodeEntity>>

    @Query("SELECT * FROM redeem_codes WHERE id = :redeemId")
    fun getRedeemCodeById(redeemId: String): Flow<RedeemCodeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedeemCode(redeemCode: RedeemCodeEntity)

    @Query("UPDATE redeem_codes SET status = :status, claimedAt = :claimedAt, synced = 0 WHERE id = :redeemId")
    suspend fun updateStatus(redeemId: String, status: String, claimedAt: Long? = null)

    @Query("UPDATE redeem_codes SET status = 'EXPIRED' WHERE expiresAt < :now AND status = 'ACTIVE'")
    suspend fun expireOldCodes(now: Long)

    @Query("SELECT * FROM redeem_codes WHERE synced = 0")
    suspend fun getUnsyncedCodes(): List<RedeemCodeEntity>

    @Query("UPDATE redeem_codes SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
