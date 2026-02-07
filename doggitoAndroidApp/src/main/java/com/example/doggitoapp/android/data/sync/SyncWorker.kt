package com.example.doggitoapp.android.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.doggitoapp.android.data.local.dao.*
import com.example.doggitoapp.android.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val petDao: PetDao by inject()
    private val taskDao: DailyTaskDao by inject()
    private val coinDao: CoinTransactionDao by inject()
    private val runDao: RunSessionDao by inject()
    private val redeemDao: RedeemCodeDao by inject()
    private val vaccineDao: VaccineDao by inject()

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting sync...")

            syncPets()
            syncTasks()
            syncCoinTransactions()
            syncRunningSessions()
            syncRedeemCodes()
            syncVaccines()

            Log.d(TAG, "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncPets() {
        val unsynced = petDao.getUnsyncedPets()
        if (unsynced.isEmpty()) return

        try {
            val client = SupabaseClientProvider.client
            unsynced.forEach { pet ->
                client.postgrest["pets"].upsert(
                    PetDto(
                        id = pet.id,
                        user_id = pet.userId,
                        name = pet.name,
                        breed = pet.breed,
                        birth_date = pet.birthDate,
                        weight = pet.weight,
                        photo_uri = pet.photoUri
                    )
                )
            }
            petDao.markAsSynced(unsynced.map { it.id })
            Log.d(TAG, "Synced ${unsynced.size} pets")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync pets: ${e.message}")
        }
    }

    private suspend fun syncTasks() {
        val unsynced = taskDao.getUnsyncedTasks()
        if (unsynced.isEmpty()) return

        try {
            val client = SupabaseClientProvider.client
            unsynced.forEach { task ->
                client.postgrest["daily_tasks"].upsert(
                    TaskDto(
                        id = task.id,
                        user_id = task.userId,
                        title = task.title,
                        description = task.description,
                        category = task.category,
                        reward_coins = task.rewardCoins,
                        is_completed = task.isCompleted,
                        assigned_date = task.assignedDate,
                        completed_at = task.completedAt
                    )
                )
            }
            taskDao.markAsSynced(unsynced.map { it.id })
            Log.d(TAG, "Synced ${unsynced.size} tasks")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync tasks: ${e.message}")
        }
    }

    private suspend fun syncCoinTransactions() {
        val unsynced = coinDao.getUnsyncedTransactions()
        if (unsynced.isEmpty()) return

        try {
            val client = SupabaseClientProvider.client
            unsynced.forEach { tx ->
                client.postgrest["coin_transactions"].upsert(
                    CoinDto(
                        id = tx.id,
                        user_id = tx.userId,
                        amount = tx.amount,
                        type = tx.type,
                        description = tx.description,
                        created_at = tx.createdAt
                    )
                )
            }
            coinDao.markAsSynced(unsynced.map { it.id })
            Log.d(TAG, "Synced ${unsynced.size} coin transactions")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync coin transactions: ${e.message}")
        }
    }

    private suspend fun syncRunningSessions() {
        val unsynced = runDao.getUnsyncedSessions()
        if (unsynced.isEmpty()) return

        try {
            val client = SupabaseClientProvider.client
            unsynced.forEach { session ->
                client.postgrest["running_sessions"].upsert(
                    RunDto(
                        id = session.id,
                        user_id = session.userId,
                        pet_id = session.petId,
                        distance_meters = session.distanceMeters,
                        duration_millis = session.durationMillis,
                        avg_speed_kmh = session.avgSpeedKmh,
                        calories_user = session.caloriesUser,
                        calories_dog = session.caloriesDog,
                        mode = session.mode,
                        route_json = session.routeJson,
                        coins_earned = session.coinsEarned,
                        started_at = session.startedAt,
                        ended_at = session.endedAt
                    )
                )
            }
            runDao.markAsSynced(unsynced.map { it.id })
            Log.d(TAG, "Synced ${unsynced.size} running sessions")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync running sessions: ${e.message}")
        }
    }

    private suspend fun syncRedeemCodes() {
        val unsynced = redeemDao.getUnsyncedCodes()
        if (unsynced.isEmpty()) return

        try {
            val client = SupabaseClientProvider.client
            unsynced.forEach { code ->
                client.postgrest["redeem_codes"].upsert(
                    RedeemDto(
                        id = code.id,
                        user_id = code.userId,
                        product_id = code.productId,
                        code = code.code,
                        qr_data = code.qrData,
                        store_id = code.storeId,
                        status = code.status,
                        created_at = code.createdAt,
                        expires_at = code.expiresAt,
                        claimed_at = code.claimedAt
                    )
                )
            }
            redeemDao.markAsSynced(unsynced.map { it.id })
            Log.d(TAG, "Synced ${unsynced.size} redeem codes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync redeem codes: ${e.message}")
        }
    }

    private suspend fun syncVaccines() {
        val unsynced = vaccineDao.getUnsyncedVaccines()
        if (unsynced.isEmpty()) return

        try {
            val client = SupabaseClientProvider.client
            unsynced.forEach { vaccine ->
                client.postgrest["vaccine_records"].upsert(
                    VaccineDto(
                        id = vaccine.id,
                        pet_id = vaccine.petId,
                        vaccine_name = vaccine.vaccineName,
                        date_administered = vaccine.dateAdministered,
                        next_due_date = vaccine.nextDueDate,
                        vet_name = vaccine.vetName,
                        notes = vaccine.notes
                    )
                )
            }
            vaccineDao.markAsSynced(unsynced.map { it.id })
            Log.d(TAG, "Synced ${unsynced.size} vaccines")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync vaccines: ${e.message}")
        }
    }
}

// DTOs for Supabase (snake_case to match Postgres conventions)
@Serializable data class PetDto(val id: String, val user_id: String, val name: String, val breed: String, val birth_date: Long, val weight: Float, val photo_uri: String?)
@Serializable data class TaskDto(val id: String, val user_id: String, val title: String, val description: String, val category: String, val reward_coins: Int, val is_completed: Boolean, val assigned_date: Long, val completed_at: Long?)
@Serializable data class CoinDto(val id: String, val user_id: String, val amount: Int, val type: String, val description: String, val created_at: Long)
@Serializable data class RunDto(val id: String, val user_id: String, val pet_id: String, val distance_meters: Float, val duration_millis: Long, val avg_speed_kmh: Float, val calories_user: Float, val calories_dog: Float, val mode: String, val route_json: String, val coins_earned: Int, val started_at: Long, val ended_at: Long)
@Serializable data class RedeemDto(val id: String, val user_id: String, val product_id: String, val code: String, val qr_data: String, val store_id: String, val status: String, val created_at: Long, val expires_at: Long, val claimed_at: Long?)
@Serializable data class VaccineDto(val id: String, val pet_id: String, val vaccine_name: String, val date_administered: Long, val next_due_date: Long?, val vet_name: String?, val notes: String?)
