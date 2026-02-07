package com.example.doggitoapp.android.di

import androidx.room.Room
import com.example.doggitoapp.android.data.local.DoggitoDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            DoggitoDatabase::class.java,
            "doggito_database"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<DoggitoDatabase>().petDao() }
    single { get<DoggitoDatabase>().dailyTaskDao() }
    single { get<DoggitoDatabase>().coinTransactionDao() }
    single { get<DoggitoDatabase>().runSessionDao() }
    single { get<DoggitoDatabase>().productDao() }
    single { get<DoggitoDatabase>().redeemCodeDao() }
    single { get<DoggitoDatabase>().storeDao() }
    single { get<DoggitoDatabase>().vaccineDao() }
    single { get<DoggitoDatabase>().streakDao() }
}
