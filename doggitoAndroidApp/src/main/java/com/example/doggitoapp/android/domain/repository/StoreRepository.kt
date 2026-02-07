package com.example.doggitoapp.android.domain.repository

import com.example.doggitoapp.android.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun getAllStores(): Flow<List<Store>>
    fun getStoreById(storeId: String): Flow<Store?>
    suspend fun refreshStores()
    fun getNearestStore(latitude: Double, longitude: Double): Flow<Store?>
}
