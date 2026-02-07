package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.RedeemCode
import com.example.doggitoapp.android.domain.model.RedeemStatus

@Entity(tableName = "redeem_codes")
data class RedeemCodeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val code: String,
    val qrData: String,
    val storeId: String,
    val status: String = RedeemStatus.ACTIVE.name,
    val createdAt: Long,
    val expiresAt: Long,
    val claimedAt: Long? = null,
    val synced: Boolean = false
) {
    fun toDomain() = RedeemCode(
        id = id,
        userId = userId,
        productId = productId,
        code = code,
        qrData = qrData,
        storeId = storeId,
        status = RedeemStatus.valueOf(status),
        createdAt = createdAt,
        expiresAt = expiresAt,
        claimedAt = claimedAt,
        synced = synced
    )

    companion object {
        fun fromDomain(redeem: RedeemCode) = RedeemCodeEntity(
            id = redeem.id,
            userId = redeem.userId,
            productId = redeem.productId,
            code = redeem.code,
            qrData = redeem.qrData,
            storeId = redeem.storeId,
            status = redeem.status.name,
            createdAt = redeem.createdAt,
            expiresAt = redeem.expiresAt,
            claimedAt = redeem.claimedAt,
            synced = redeem.synced
        )
    }
}
