package com.example.doggitoapp.android.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.doggitoapp.android.feature.auth.LoginScreen
import com.example.doggitoapp.android.feature.auth.RegisterScreen
import com.example.doggitoapp.android.feature.home.HomeScreen
import com.example.doggitoapp.android.feature.onboarding.OnboardingScreen
import com.example.doggitoapp.android.feature.profile.MedicalHistoryScreen
import com.example.doggitoapp.android.feature.profile.PetEditScreen
import com.example.doggitoapp.android.feature.profile.PetProfileScreen
import com.example.doggitoapp.android.feature.redeem.RedeemCodeScreen
import com.example.doggitoapp.android.feature.redeem.RedeemConfirmScreen
import com.example.doggitoapp.android.feature.redeem.RedeemHistoryScreen
import com.example.doggitoapp.android.feature.running.RunningActiveScreen
import com.example.doggitoapp.android.feature.running.RunningHistoryScreen
import com.example.doggitoapp.android.feature.running.RunningScreen
import com.example.doggitoapp.android.feature.settings.SettingsScreen
import com.example.doggitoapp.android.feature.shop.ProductDetailScreen
import com.example.doggitoapp.android.feature.shop.ShopScreen
import com.example.doggitoapp.android.feature.stores.StoreDetailScreen
import com.example.doggitoapp.android.feature.stores.StoresScreen
import com.example.doggitoapp.android.feature.tasks.TasksScreen

@Composable
fun DoggitoNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.PetEdit.createRoute()) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                onNavigateToRunning = {
                    navController.navigate(Screen.RunningActive.createRoute("RUN"))
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToProfile = { navController.navigate(Screen.PetProfile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToRedeemHistory = { navController.navigate(Screen.RedeemHistory.route) }
            )
        }

        // Tasks
        composable(Screen.Tasks.route) {
            TasksScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Running - keep legacy screen accessible from history
        composable(Screen.Running.route) {
            RunningScreen(
                onStartRun = { mode -> navController.navigate(Screen.RunningActive.createRoute(mode)) },
                onViewHistory = { navController.navigate(Screen.RunningHistory.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.RunningActive.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "RUN" })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "RUN"
            RunningActiveScreen(
                mode = mode,
                onFinish = { navController.popBackStack() },
                onViewHistory = { navController.navigate(Screen.RunningHistory.route) }
            )
        }

        composable(Screen.RunningHistory.route) {
            RunningHistoryScreen(onBack = { navController.popBackStack() })
        }

        // Profile
        composable(Screen.PetProfile.route) {
            PetProfileScreen(
                onEdit = { petId -> navController.navigate(Screen.PetEdit.createRoute(petId)) },
                onMedicalHistory = { petId -> navController.navigate(Screen.MedicalHistory.createRoute(petId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.PetEdit.route,
            arguments = listOf(navArgument("petId") { type = NavType.StringType; defaultValue = "" })
        ) {
            PetEditScreen(
                onSaved = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.MedicalHistory.route,
            arguments = listOf(navArgument("petId") { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            MedicalHistoryScreen(petId = petId, onBack = { navController.popBackStack() })
        }

        // Shop
        composable(Screen.Shop.route) {
            ShopScreen(
                onProductClick = { productId -> navController.navigate(Screen.ProductDetail.createRoute(productId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onRedeem = { navController.navigate(Screen.RedeemConfirm.createRoute(productId)) },
                onBack = { navController.popBackStack() }
            )
        }

        // Redeem
        composable(
            Screen.RedeemConfirm.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            RedeemConfirmScreen(
                productId = productId,
                onRedeemSuccess = { redeemId ->
                    navController.navigate(Screen.RedeemCode.createRoute(redeemId)) {
                        popUpTo(Screen.Shop.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.RedeemCode.route,
            arguments = listOf(navArgument("redeemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val redeemId = backStackEntry.arguments?.getString("redeemId") ?: ""
            RedeemCodeScreen(
                redeemId = redeemId,
                onBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RedeemHistory.route) {
            RedeemHistoryScreen(
                onRedeemClick = { redeemId ->
                    navController.navigate(Screen.RedeemCode.createRoute(redeemId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Stores
        composable(Screen.Stores.route) {
            StoresScreen(
                onStoreClick = { storeId -> navController.navigate(Screen.StoreDetail.createRoute(storeId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.StoreDetail.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            StoreDetailScreen(storeId = storeId, onBack = { navController.popBackStack() })
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
