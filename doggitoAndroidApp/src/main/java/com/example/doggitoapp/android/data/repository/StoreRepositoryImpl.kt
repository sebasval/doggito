package com.example.doggitoapp.android.data.repository

import com.example.doggitoapp.android.data.local.dao.StoreDao
import com.example.doggitoapp.android.data.local.entity.StoreEntity
import com.example.doggitoapp.android.domain.model.Store
import com.example.doggitoapp.android.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.math.*

class StoreRepositoryImpl(
    private val storeDao: StoreDao
) : StoreRepository {

    override fun getAllStores(): Flow<List<Store>> =
        storeDao.getAllStores().map { entities -> entities.map { it.toDomain() } }

    override fun getStoreById(storeId: String): Flow<Store?> =
        storeDao.getStoreById(storeId).map { it?.toDomain() }

    override fun getNearestStore(latitude: Double, longitude: Double): Flow<Store?> =
        storeDao.getAllStores().map { stores ->
            stores.minByOrNull { store ->
                haversineDistance(latitude, longitude, store.latitude, store.longitude)
            }?.toDomain()
        }

    override suspend fun refreshStores() {
        // TODO: Fetch from Supabase when online
        // Seed sample stores
        val sampleStores = listOf(
            StoreEntity(
                id = UUID.randomUUID().toString(),
                name = "Doggito Store Centro",
                address = "Av. Principal 123, Centro",
                latitude = 19.4326,
                longitude = -99.1332,
                phone = "+52 55 1234 5678",
                email = "centro@doggito.com",
                openingHours = "Lun-Sáb 9:00-20:00, Dom 10:00-15:00"
            ),
            StoreEntity(
                id = UUID.randomUUID().toString(),
                name = "Doggito Store Norte",
                address = "Blvd. Norte 456, Col. Reforma",
                latitude = 19.4500,
                longitude = -99.1500,
                phone = "+52 55 8765 4321",
                email = "norte@doggito.com",
                openingHours = "Lun-Sáb 10:00-21:00, Dom 10:00-16:00"
            )
        )
        storeDao.insertStores(sampleStores)
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }
}
