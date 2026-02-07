package com.example.doggitoapp.android.data.repository

import com.example.doggitoapp.android.data.local.dao.CoinTransactionDao
import com.example.doggitoapp.android.data.local.entity.CoinTransactionEntity
import com.example.doggitoapp.android.domain.model.CoinTransaction
import com.example.doggitoapp.android.domain.model.TransactionType
import com.example.doggitoapp.android.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CoinRepositoryImpl(
    private val coinDao: CoinTransactionDao
) : CoinRepository {

    override fun getBalance(userId: String): Flow<Int> =
        coinDao.getBalance(userId)

    override fun getTransactions(userId: String): Flow<List<CoinTransaction>> =
        coinDao.getTransactions(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getRecentTransactions(userId: String, limit: Int): Flow<List<CoinTransaction>> =
        coinDao.getRecentTransactions(userId, limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addCoins(userId: String, amount: Int, type: TransactionType, description: String) {
        coinDao.insertTransaction(
            CoinTransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = amount,
                type = type.name,
                description = description,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun spendCoins(userId: String, amount: Int, description: String) {
        coinDao.insertTransaction(
            CoinTransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = -amount,
                type = TransactionType.REDEMPTION.name,
                description = description,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
