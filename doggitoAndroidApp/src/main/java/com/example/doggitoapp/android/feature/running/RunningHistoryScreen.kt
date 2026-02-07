package com.example.doggitoapp.android.feature.running

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.core.util.DateUtils
import com.example.doggitoapp.android.domain.model.RunningMode
import com.example.doggitoapp.android.domain.model.RunningSession
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningHistoryScreen(
    onBack: () -> Unit,
    viewModel: RunningViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Actividad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (uiState.sessions.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DirectionsRun, null, Modifier.size(64.dp), tint = DoggitoGreenLight)
                    Spacer(Modifier.height(16.dp))
                    Text("Sin actividades registradas", color = TextPrimary)
                    Text("Sal a pasear con tu perro!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.sessions) { session ->
                    SessionCard(session)
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: RunningSession) {
    val modeColor = when (session.mode) {
        RunningMode.WALK -> DoggitoGreen
        RunningMode.RUN -> DoggitoGreenDark
        RunningMode.HIKE -> TrainingColor
    }
    val modeIcon = when (session.mode) {
        RunningMode.WALK -> Icons.Default.DirectionsWalk
        RunningMode.RUN -> Icons.Default.DirectionsRun
        RunningMode.HIKE -> Icons.Default.Terrain
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(modeIcon, null, tint = modeColor)
                Spacer(Modifier.width(8.dp))
                Text(session.mode.displayName, fontWeight = FontWeight.SemiBold, color = modeColor)
                Spacer(Modifier.weight(1f))
                Text(DateUtils.formatDate(session.startedAt), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(DateUtils.formatDistance(session.distanceMeters), fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Distancia", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(DateUtils.formatTime(session.durationMillis), fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Tiempo", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format("%.1f km/h", session.avgSpeedKmh), fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Velocidad", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+${session.coinsEarned}", fontWeight = FontWeight.Bold, color = DoggiCoinGoldDark)
                        Icon(Icons.Default.MonetizationOn, null, Modifier.size(14.dp), tint = DoggiCoinGold)
                    }
                    Text("Monedas", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}
