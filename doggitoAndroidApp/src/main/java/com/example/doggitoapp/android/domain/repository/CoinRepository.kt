package com.example.doggitoapp.android.domain.repository

import com.example.doggitoapp.android.domain.model.CoinTransaction
import com.example.doggitoapp.android.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun getBalance(userId: String): Flow<Int>
    fun getTransactions(userId: String): Flow<List<CoinTransaction>>
    fun getRecentTransactions(userId: String, limit: Int = 10): Flow<List<CoinTransaction>>
    suspend fun addCoins(userId: String, amount: Int, type: TransactionType, description: String)
    suspend fun spendCoins(userId: String, amount: Int, description: String)
}
