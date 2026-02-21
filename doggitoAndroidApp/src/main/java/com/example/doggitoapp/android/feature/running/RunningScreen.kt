package com.example.doggitoapp.android.feature.running

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.core.util.DateUtils
import com.example.doggitoapp.android.domain.model.RunningMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningScreen(
    onStartRun: (String) -> Unit,
    onViewHistory: () -> Unit,
    onBack: () -> Unit,
    viewModel: RunningViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividad Física") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = onViewHistory) {
                        Text("Historial")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats summary
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCard("Distancia Total", DateUtils.formatDistance(uiState.totalDistance), ExerciseColor)
                StatCard("Sesiones", "${uiState.sessionCount}", DoggitoTeal)
            }

            Spacer(Modifier.height(32.dp))

            Text("Elige tu modo de actividad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            // Mode selection
            RunningMode.entries.forEach { mode ->
                val (icon, color) = when (mode) {
                    RunningMode.WALK -> Icons.Default.DirectionsWalk to DoggitoTeal
                    RunningMode.RUN -> Icons.Default.DirectionsRun to ExerciseColor
                    RunningMode.HIKE -> Icons.Default.Terrain to TrainingColor
                }

                Card(
                    onClick = { onStartRun(mode.name) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(mode.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                            Text(
                                when (mode) {
                                    RunningMode.WALK -> "Paseo tranquilo con tu perro"
                                    RunningMode.RUN -> "Corre junto a tu mejor amigo"
                                    RunningMode.HIKE -> "Aventura al aire libre"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(Icons.Default.PlayArrow, null, tint = color)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DoggiCoinGold.copy(alpha = 0.1f))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, null, tint = DoggiCoinGold)
                    Spacer(Modifier.width(8.dp))
                    Text("Gana 10 DoggiCoins por cada kilómetro recorrido", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
