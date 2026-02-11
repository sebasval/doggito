package com.example.doggitoapp.android.di

import com.example.doggitoapp.android.feature.auth.AuthViewModel
import com.example.doggitoapp.android.feature.home.HomeViewModel
import com.example.doggitoapp.android.feature.profile.ProfileViewModel
import com.example.doggitoapp.android.feature.redeem.RedeemViewModel
import com.example.doggitoapp.android.feature.running.RunningViewModel
import com.example.doggitoapp.android.feature.settings.SettingsViewModel
import com.example.doggitoapp.android.feature.shop.ShopViewModel
import com.example.doggitoapp.android.feature.stores.StoresViewModel
import com.example.doggitoapp.android.feature.tasks.TasksViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { TasksViewModel(get(), get(), get(), get(), get()) }
    viewModel { RunningViewModel(get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), androidApplication()) }
    viewModel { ShopViewModel(get(), get(), get()) }
    viewModel { RedeemViewModel(get(), get(), get(), get(), get()) }
    viewModel { StoresViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}
