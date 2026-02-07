package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.CoinTransaction
import com.example.doggitoapp.android.domain.model.TransactionType

@Entity(tableName = "coin_transactions")
data class CoinTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Int,
    val type: String,
    val description: String,
    val createdAt: Long,
    val synced: Boolean = false
) {
    fun toDomain() = CoinTransaction(
        id = id,
        userId = userId,
        amount = amount,
        type = TransactionType.valueOf(type),
        description = description,
        createdAt = createdAt,
        synced = synced
    )

    companion object {
        fun fromDomain(tx: CoinTransaction) = CoinTransactionEntity(
            id = tx.id,
            userId = tx.userId,
            amount = tx.amount,
            type = tx.type.name,
            description = tx.description,
            createdAt = tx.createdAt,
            synced = tx.synced
        )
    }
}
