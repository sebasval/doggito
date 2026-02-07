package com.example.doggitoapp.android.di

import com.example.doggitoapp.android.core.util.NetworkMonitor
import com.example.doggitoapp.android.data.remote.SupabaseClientProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single { SupabaseClientProvider.client }
    single { NetworkMonitor(androidContext()) }
}
