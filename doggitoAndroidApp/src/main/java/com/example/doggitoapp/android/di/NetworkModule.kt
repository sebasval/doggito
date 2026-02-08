package com.example.doggitoapp.android.di

import com.example.doggitoapp.android.core.util.NetworkMonitor
import com.example.doggitoapp.android.data.remote.SupabaseClientProvider
import com.example.doggitoapp.android.data.sync.DataPullManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single { SupabaseClientProvider.client }
    single { NetworkMonitor(androidContext()) }
    single { DataPullManager(get(), get(), get(), get(), get(), get(), get(), androidApplication()) }
}
