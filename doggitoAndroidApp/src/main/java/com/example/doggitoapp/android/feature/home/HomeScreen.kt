package com.example.doggitoapp.android.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.core.util.DateUtils
import com.example.doggitoapp.android.domain.model.TransactionType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToRunning: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRedeemHistory: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, null, tint = DoggitoOrange)
                        Spacer(Modifier.width(8.dp))
                        Text("Doggito", fontWeight = FontWeight.Bold, color = DoggitoOrange)
                    }
                },
                actions = {
                    // Online/Offline indicator
                    Surface(
                        shape = CircleShape,
                        color = if (uiState.isOnline) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (uiState.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = if (uiState.isOnline) "En línea" else "Sin conexión",
                            modifier = Modifier.padding(6.dp),
                            tint = if (uiState.isOnline) SuccessGreen else ErrorRed
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Ajustes")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Offline banner
            if (!uiState.isOnline) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WifiOff, null, tint = WarningAmber)
                        Spacer(Modifier.width(8.dp))
                        Text("Modo offline - tus datos se sincronizarán al conectarte", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Welcome & Pet info
            uiState.pet?.let { pet ->
                Text(
                    "Hola, dueño de ${pet.name}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            } ?: Text("¡Bienvenido a Doggito!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))

            // DoggiCoins Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(DoggitoOrange, DoggitoOrangeDark)),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, null, tint = DoggiCoinGold, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("DoggiCoins", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${uiState.balance}",
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.streak?.let { streak ->
                            if (streak.currentStreak > 0) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalFireDepartment, null, tint = DoggiCoinGold, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Racha: ${streak.currentStreak} días",
                                        color = Color.White.copy(alpha = 0.9f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Daily Tasks Summary
            Text("Progreso del Día", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Card(
                onClick = onNavigateToTasks,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TaskAlt, null, tint = DoggitoTeal)
                        Spacer(Modifier.width(8.dp))
                        Text("Tareas Diarias", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${uiState.completedTasks}/${uiState.totalTasks}",
                            fontWeight = FontWeight.Bold,
                            color = DoggitoTeal
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    val progress = if (uiState.totalTasks > 0) uiState.completedTasks.toFloat() / uiState.totalTasks else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = DoggitoTeal,
                        trackColor = DoggitoTeal.copy(alpha = 0.2f)
                    )
                    if (uiState.completedTasks == uiState.totalTasks && uiState.totalTasks > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text("¡Todas las tareas completadas!", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Quick Actions Grid
            Text("Acciones Rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DirectionsRun,
                    label = "Running",
                    color = ExerciseColor,
                    onClick = onNavigateToRunning
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ShoppingBag,
                    label = "Tienda",
                    color = DoggitoOrange,
                    onClick = onNavigateToShop
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Pets,
                    label = "Mi Mascota",
                    color = DoggitoTeal,
                    onClick = onNavigateToProfile
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CardGiftcard,
                    label = "Mis Canjes",
                    color = TrainingColor,
                    onClick = onNavigateToRedeemHistory
                )
            }

            Spacer(Modifier.height(20.dp))

            // Recent Transactions
            if (uiState.recentTransactions.isNotEmpty()) {
                Text("Actividad Reciente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                uiState.recentTransactions.forEach { tx ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val isPositive = tx.amount > 0
                            Icon(
                                if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                null,
                                tint = if (isPositive) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(tx.description, style = MaterialTheme.typography.bodyMedium)
                                Text(DateUtils.formatDateTime(tx.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            Text(
                                "${if (isPositive) "+" else ""}${tx.amount}",
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) SuccessGreen else ErrorRed
                            )
                            Spacer(Modifier.width(2.dp))
                            Icon(Icons.Default.MonetizationOn, null, tint = DoggiCoinGold, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Medium, color = color, style = MaterialTheme.typography.labelLarge)
        }
    }
}
