package com.example.doggitoapp.android.feature.redeem

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
import com.example.doggitoapp.android.domain.model.RedeemCode
import com.example.doggitoapp.android.domain.model.RedeemStatus
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemHistoryScreen(
    onRedeemClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: RedeemViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Canjes", fontWeight = FontWeight.Bold) },
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
        if (uiState.redeemHistory.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CardGiftcard, null, Modifier.size(64.dp), tint = DoggitoGreenLight)
                    Spacer(Modifier.height(16.dp))
                    Text("Sin canjes aun", color = TextPrimary)
                    Text("Acumula DoggiCoins y canjea premios!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
                items(uiState.redeemHistory) { code ->
                    RedeemHistoryCard(
                        redeemCode = code,
                        onClick = { onRedeemClick(code.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RedeemHistoryCard(redeemCode: RedeemCode, onClick: () -> Unit) {
    val statusColor = when (redeemCode.status) {
        RedeemStatus.ACTIVE -> SuccessGreen
        RedeemStatus.CLAIMED -> DoggitoGreen
        RedeemStatus.EXPIRED -> ErrorRed
    }
    val statusIcon = when (redeemCode.status) {
        RedeemStatus.ACTIVE -> Icons.Default.ConfirmationNumber
        RedeemStatus.CLAIMED -> Icons.Default.CheckCircle
        RedeemStatus.EXPIRED -> Icons.Default.Cancel
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Codigo: ${redeemCode.code}", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Creado: ${DateUtils.formatDate(redeemCode.createdAt)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (redeemCode.status == RedeemStatus.ACTIVE) {
                    val daysLeft = DateUtils.daysUntil(redeemCode.expiresAt)
                    Text("Expira en $daysLeft dias", style = MaterialTheme.typography.bodySmall, color = if (daysLeft > 7) SuccessGreen else WarningAmber)
                }
            }
            Surface(shape = RoundedCornerShape(10.dp), color = statusColor.copy(alpha = 0.12f)) {
                Text(
                    redeemCode.status.displayName,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
