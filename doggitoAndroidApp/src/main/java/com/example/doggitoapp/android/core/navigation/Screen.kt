package com.example.doggitoapp.android.core.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Onboarding : Screen("onboarding")

    // Main
    data object Home : Screen("home")
    data object Tasks : Screen("tasks")
    data object TaskDetail : Screen("tasks/{taskId}") {
        fun createRoute(taskId: String) = "tasks/$taskId"
    }

    // Running
    data object Running : Screen("running")
    data object RunningActive : Screen("running/active?mode={mode}") {
        fun createRoute(mode: String = "RUN") = "running/active?mode=$mode"
    }
    data object RunningHistory : Screen("running/history")

    // Profile
    data object PetProfile : Screen("profile")
    data object PetEdit : Screen("profile/edit?petId={petId}") {
        fun createRoute(petId: String? = null) =
            if (petId != null) "profile/edit?petId=$petId" else "profile/edit"
    }
    data object MedicalHistory : Screen("profile/medical/{petId}") {
        fun createRoute(petId: String) = "profile/medical/$petId"
    }

    // Shop
    data object Shop : Screen("shop")
    data object ProductDetail : Screen("shop/{productId}") {
        fun createRoute(productId: String) = "shop/$productId"
    }

    // Redeem
    data object RedeemConfirm : Screen("redeem/confirm/{productId}") {
        fun createRoute(productId: String) = "redeem/confirm/$productId"
    }
    data object RedeemCode : Screen("redeem/code/{redeemId}") {
        fun createRoute(redeemId: String) = "redeem/code/$redeemId"
    }
    data object RedeemHistory : Screen("redeem/history")

    // Stores
    data object Stores : Screen("stores")
    data object StoreDetail : Screen("stores/{storeId}") {
        fun createRoute(storeId: String) = "stores/$storeId"
    }

    // Settings
    data object Settings : Screen("settings")

    companion object {
        // Routes where the bottom navigation bar should be visible
        val mainRoutes = setOf(
            Home.route,
            Tasks.route,
            Shop.route,
            PetProfile.route
        )
    }
}
