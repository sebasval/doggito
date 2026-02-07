package com.example.doggitoapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.doggitoapp.android.core.components.BottomNavItem
import com.example.doggitoapp.android.core.components.DoggitoBottomBar
import com.example.doggitoapp.android.core.navigation.DoggitoNavGraph
import com.example.doggitoapp.android.core.navigation.Screen
import com.example.doggitoapp.android.core.theme.DoggitoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DoggitoTheme {
                val navController = rememberNavController()
                MainScreen(navController = navController)
            }
        }
    }
}

@Composable
private fun MainScreen(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in Screen.mainRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        DoggitoNavGraph(
            navController = navController,
            startDestination = Screen.Login.route
        )

        // Pill-shaped floating bottom nav
        AnimatedVisibility(
            visible = showBottomBar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            DoggitoBottomBar(
                currentRoute = currentRoute,
                onItemSelected = { item ->
                    val targetRoute = when (item) {
                        BottomNavItem.HOME -> Screen.Home.route
                        BottomNavItem.TASKS -> Screen.Tasks.route
                        BottomNavItem.RUNNING -> Screen.RunningActive.createRoute("RUN")
                        BottomNavItem.SHOP -> Screen.Shop.route
                        BottomNavItem.PROFILE -> Screen.PetProfile.route
                    }
                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
