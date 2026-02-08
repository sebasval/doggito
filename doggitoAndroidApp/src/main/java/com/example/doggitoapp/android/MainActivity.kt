package com.example.doggitoapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.doggitoapp.android.core.components.BottomNavItem
import com.example.doggitoapp.android.core.components.DoggitoBottomBar
import com.example.doggitoapp.android.core.navigation.DoggitoNavGraph
import com.example.doggitoapp.android.core.navigation.Screen
import com.example.doggitoapp.android.core.theme.DoggitoGradientBackground
import com.example.doggitoapp.android.core.theme.DoggitoTheme
import com.example.doggitoapp.android.feature.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DoggitoTheme {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsState()

    // Mostrar splash mientras se verifica la sesion (sin montar NavGraph)
    if (authState.isCheckingSession) {
        DoggitoGradientBackground {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "doggito.",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.White.copy(alpha = 0.7f),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
        return
    }

    // Decidir startDestination segun sesion activa
    val startDestination = if (authState.isLoggedIn) Screen.Home.route else Screen.Login.route
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in Screen.mainRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        DoggitoNavGraph(
            navController = navController,
            startDestination = startDestination
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
