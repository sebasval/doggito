package com.example.doggitoapp.android.feature.redeem

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.core.util.DateUtils
import com.example.doggitoapp.android.core.util.QRGenerator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemCodeScreen(
    redeemId: String,
    onNavigateToStore: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: RedeemViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(redeemId) {
        viewModel.loadRedeemCode(redeemId)
    }

    val redeemCode = uiState.redeemCode
    val store = uiState.allStores.firstOrNull { it.id == redeemCode?.storeId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Codigo de Canje", fontWeight = FontWeight.Bold) },
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
        if (redeemCode == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoggitoGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(48.dp), tint = SuccessGreen)
                Spacer(Modifier.height(8.dp))
                Text("Canje Exitoso", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SuccessGreen)

                Spacer(Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val qrBitmap: Bitmap = remember(redeemCode.qrData) {
                            QRGenerator.generateQRBitmap(redeemCode.qrData, 300)
                        }
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Codigo QR",
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(Modifier.height(12.dp))
                        Text("Codigo:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            redeemCode.code,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Vigencia", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text("Hasta: ${DateUtils.formatDate(redeemCode.expiresAt)}", fontWeight = FontWeight.Medium, color = TextPrimary)
                        val daysLeft = DateUtils.daysUntil(redeemCode.expiresAt)
                        Text(
                            "$daysLeft dias restantes",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (daysLeft > 7) SuccessGreen else WarningAmber
                        )
                    }
                }

                if (store != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Store, null, tint = DoggitoGreen)
                                Spacer(Modifier.width(8.dp))
                                Text(store.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(store.address, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("Horario: ${store.openingHours}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { onNavigateToStore(store.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Navigation, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ver en Mapa / Navegar")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Presenta este codigo QR o el codigo alfanumerico en la tienda para reclamar tu producto.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = TextSecondary
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
                ) {
                    Text("Volver al Inicio", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
