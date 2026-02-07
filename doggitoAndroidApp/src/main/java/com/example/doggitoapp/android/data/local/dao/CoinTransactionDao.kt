package com.example.doggitoapp.android.data.local.dao

import androidx.room.*
import com.example.doggitoapp.android.data.local.entity.CoinTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinTransactionDao {
    @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_transactions WHERE userId = :userId")
    fun getBalance(userId: String): Flow<Int>

    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactions(userId: String): Flow<List<CoinTransactionEntity>>

    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTransactions(userId: String, limit: Int): Flow<List<CoinTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CoinTransactionEntity)

    @Query("SELECT * FROM coin_transactions WHERE synced = 0")
    suspend fun getUnsyncedTransactions(): List<CoinTransactionEntity>

    @Query("UPDATE coin_transactions SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
