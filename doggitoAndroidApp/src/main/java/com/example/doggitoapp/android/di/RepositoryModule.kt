package com.example.doggitoapp.android.di

import com.example.doggitoapp.android.data.repository.*
import com.example.doggitoapp.android.domain.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<PetRepository> { PetRepositoryImpl(get()) }
    single<TaskRepository> { TaskRepositoryImpl(get()) }
    single<CoinRepository> { CoinRepositoryImpl(get()) }
    single<RunRepository> { RunRepositoryImpl(get()) }
    single<ShopRepository> { ShopRepositoryImpl(get()) }
    single<RedeemRepository> { RedeemRepositoryImpl(get()) }
    single<StoreRepository> { StoreRepositoryImpl(get()) }
    single<VaccineRepository> { VaccineRepositoryImpl(get()) }
}
