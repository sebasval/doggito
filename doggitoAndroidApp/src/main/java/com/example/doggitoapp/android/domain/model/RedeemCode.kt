package com.example.doggitoapp.android.domain.model

enum class RedeemStatus(val displayName: String) {
    ACTIVE("Activo"),
    CLAIMED("Reclamado"),
    EXPIRED("Expirado")
}

data class RedeemCode(
    val id: String,
    val userId: String,
    val productId: String,
    val code: String,
    val qrData: String,
    val storeId: String,
    val status: RedeemStatus = RedeemStatus.ACTIVE,
    val createdAt: Long,
    val expiresAt: Long,
    val claimedAt: Long? = null,
    val synced: Boolean = false,
    // Joined fields for display
    val productName: String? = null,
    val productImageUrl: String? = null,
    val storeName: String? = null,
    val storeAddress: String? = null
)
