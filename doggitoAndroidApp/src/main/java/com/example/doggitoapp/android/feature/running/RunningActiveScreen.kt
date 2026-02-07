package com.example.doggitoapp.android.feature.running

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningActiveScreen(
    mode: String,
    onFinish: () -> Unit,
    onViewHistory: () -> Unit = {},
    viewModel: RunningViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedMode by remember { mutableStateOf(RunningMode.valueOf(mode)) }
    var hasStarted by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    val isTracking by LocationTrackingService.isTracking.collectAsState()
    val distance by LocationTrackingService.distanceMeters.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineGranted && !hasStarted) {
            viewModel.startSession(selectedMode)
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

    DoggitoGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (hasStarted) {
                        context.stopService(Intent(context, LocationTrackingService::class.java))
                        viewModel.finishSession()
                    }
                    onFinish()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Text(
                    "Actividad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(onClick = onViewHistory) {
                    Text("Historial", color = Color.White.copy(alpha = 0.8f))
                }
            }

            // Mode selector chips (only before starting)
            if (!hasStarted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    RunningMode.entries.forEach { runMode ->
                        FilterChip(
                            selected = selectedMode == runMode,
                            onClick = { selectedMode = runMode },
                            label = { Text(runMode.displayName, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    when (runMode) {
                                        RunningMode.WALK -> Icons.Default.DirectionsWalk
                                        RunningMode.RUN -> Icons.Default.DirectionsRun
                                        RunningMode.HIKE -> Icons.Default.Terrain
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White.copy(alpha = 0.25f),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                iconColor = Color.White.copy(alpha = 0.7f)
                            ),
                            border = null
                        )
                    }
                }
            } else {
                // Active mode indicator
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (selectedMode) {
                                RunningMode.WALK -> Icons.Default.DirectionsWalk
                                RunningMode.RUN -> Icons.Default.DirectionsRun
                                RunningMode.HIKE -> Icons.Default.Terrain
                            },
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isTracking) "En curso..." else "Esperando GPS...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Main metrics
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    DateUtils.formatDistance(distance),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "distancia",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(32.dp))

                // Secondary metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricColumn(
                        label = "Tiempo",
                        value = DateUtils.formatTime(if (hasStarted) elapsedTime else 0L),
                        color = Color.White
                    )
                    MetricColumn(
                        label = "Velocidad",
                        value = if (elapsedTime > 0) {
                            val speedKmh = (distance / 1000f) / (elapsedTime / 3600000f)
                            String.format("%.1f km/h", speedKmh)
                        } else "0.0 km/h",
                        color = Color.White
                    )
                    MetricColumn(
                        label = "DoggiCoins",
                        value = "+${(distance / 1000f).toInt() * 10}",
                        color = DoggitoAmberLight
                    )
                }
            }

            // Bottom action - large and thumb-friendly
            if (!hasStarted) {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = DoggitoGreenDark, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Iniciar ${selectedMode.displayName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DoggitoGreenDark
                    )
                }
            } else {
                // Stop button - large circular, easy to reach with thumb
                Button(
                    onClick = {
                        context.stopService(Intent(context, LocationTrackingService::class.java))
                        viewModel.finishSession()
                        onFinish()
                    },
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Icon(Icons.Default.Stop, "Detener", modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}
