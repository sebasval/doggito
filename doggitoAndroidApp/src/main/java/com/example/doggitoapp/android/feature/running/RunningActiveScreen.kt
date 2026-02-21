package com.example.doggitoapp.android.feature.running

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    var hasStarted by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    val isTracking by LocationTrackingService.isTracking.collectAsState()
    val distance by LocationTrackingService.distanceMeters.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineGranted && !hasStarted) {
            viewModel.startSession(RunningMode.RUN)
            context.startService(Intent(context, LocationTrackingService::class.java))
            hasStarted = true
        }
    }

    LaunchedEffect(hasStarted) {
        if (hasStarted) {
            while (true) {
                delay(1000)
                elapsedTime = System.currentTimeMillis() - uiState.startTime
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Actividad",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasStarted) {
                            context.stopService(Intent(context, LocationTrackingService::class.java))
                            viewModel.finishSession()
                        }
                        onFinish()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = onViewHistory) {
                        Text("Historial", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Animacion de perrito
            DogAnimation(isRunning = hasStarted)

            // Metricas en card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Distancia principal
                    Text(
                        DateUtils.formatDistance(distance),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = DoggitoGreenDark
                    )
                    Text(
                        "distancia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(24.dp))

                    Divider(color = DoggitoGreenLight.copy(alpha = 0.3f))

                    Spacer(Modifier.height(24.dp))

                    // Metricas secundarias
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricColumn(
                            label = "Tiempo",
                            value = DateUtils.formatTime(if (hasStarted) elapsedTime else 0L),
                            color = TextPrimary
                        )
                        MetricColumn(
                            label = "Velocidad",
                            value = if (elapsedTime > 0) {
                                val speedKmh = (distance / 1000f) / (elapsedTime / 3600000f)
                                String.format("%.1f km/h", speedKmh)
                            } else "0.0 km/h",
                            color = TextPrimary
                        )
                        MetricColumn(
                            label = "DoggiCoins",
                            value = "+${(distance / 100f).toInt()}",
                            color = DoggiCoinGold
                        )
                    }

                    if (hasStarted) {
                        Spacer(Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isTracking)
                                SuccessGreen.copy(alpha = 0.1f)
                            else
                                WarningAmber.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isTracking) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                    null,
                                    tint = if (isTracking) SuccessGreen else WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isTracking) "GPS activo" else "Buscando GPS...",
                                    color = if (isTracking) SuccessGreen else WarningAmber,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Boton de accion
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
                    colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Iniciar carrera",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            } else {
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DogAnimation(isRunning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "dog")

    if (isRunning) {
        // Perrito corriendo: movimiento horizontal + rebote vertical
        val offsetX by infiniteTransition.animateFloat(
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "runX"
        )
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -12f,
            animationSpec = infiniteRepeatable(
                animation = tween(300, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "runY"
        )
        val tilt by infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tilt"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        translationX = offsetX
                        translationY = offsetY
                    }
                    .rotate(tilt),
                tint = DoggitoGreen
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Corriendo...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DoggitoGreenDark
            )
        }
    } else {
        // Perrito en reposo: respiracion suave
        val breathScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breath"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        scaleX = breathScale
                        scaleY = breathScale
                    },
                tint = DoggitoGreen.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Listo para correr",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
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
            color = TextSecondary
        )
    }
}
