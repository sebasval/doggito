package com.example.doggitoapp.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.doggitoapp.android.data.local.dao.*
import com.example.doggitoapp.android.data.local.entity.*

@Database(
    entities = [
        PetEntity::class,
        DailyTaskEntity::class,
        CoinTransactionEntity::class,
        RunSessionEntity::class,
        ProductEntity::class,
        RedeemCodeEntity::class,
        StoreEntity::class,
        VaccineEntity::class,
        StreakEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DoggitoDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun coinTransactionDao(): CoinTransactionDao
    abstract fun runSessionDao(): RunSessionDao
    abstract fun productDao(): ProductDao
    abstract fun redeemCodeDao(): RedeemCodeDao
    abstract fun storeDao(): StoreDao
    abstract fun vaccineDao(): VaccineDao
    abstract fun streakDao(): StreakDao
}
