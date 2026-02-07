package com.example.doggitoapp.android.feature.running

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doggitoapp.android.core.service.LocationTrackingService
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.core.util.DateUtils
import com.example.doggitoapp.android.domain.model.RunningMode
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun RunningActiveScreen(
    mode: String,
    onFinish: () -> Unit,
    viewModel: RunningViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val runningMode = RunningMode.valueOf(mode)
    var hasStarted by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    val isTracking by LocationTrackingService.isTracking.collectAsState()
    val distance by LocationTrackingService.distanceMeters.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineGranted && !hasStarted) {
            viewModel.startSession(runningMode)
            context.startService(Intent(context, LocationTrackingService::class.java))
            hasStarted = true
        }
    }

    // Timer
    LaunchedEffect(hasStarted) {
        if (hasStarted) {
            while (true) {
                delay(1000)
                elapsedTime = System.currentTimeMillis() - uiState.startTime
            }
        }
    }

    // Request permissions on enter
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val modeColor = when (runningMode) {
        RunningMode.WALK -> DoggitoTeal
        RunningMode.RUN -> ExerciseColor
        RunningMode.HIKE -> TrainingColor
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                runningMode.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = modeColor
            )
            if (isTracking) {
                Text("Actividad en curso...", style = MaterialTheme.typography.bodyMedium, color = modeColor)
            } else {
                Text("Esperando ubicación...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Metrics
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Distance
            Text(
                DateUtils.formatDistance(distance),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = modeColor
            )
            Text("Distancia", style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricColumn(
                    label = "Tiempo",
                    value = DateUtils.formatTime(if (hasStarted) elapsedTime else 0L)
                )
                MetricColumn(
                    label = "Velocidad",
                    value = if (elapsedTime > 0) {
                        val speedKmh = (distance / 1000f) / (elapsedTime / 3600000f)
                        String.format("%.1f km/h", speedKmh)
                    } else "0.0 km/h"
                )
                MetricColumn(
                    label = "DoggiCoins",
                    value = "+${(distance / 1000f).toInt() * 10}"
                )
            }
        }

        // Stop button
        Button(
            onClick = {
                context.stopService(Intent(context, LocationTrackingService::class.java))
                viewModel.finishSession()
                onFinish()
            },
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
            enabled = hasStarted
        ) {
            Icon(Icons.Default.Stop, "Detener", modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    }
}
