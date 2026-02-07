package com.example.doggitoapp.android.feature.redeem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemConfirmScreen(
    productId: String,
    onRedeemSuccess: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: RedeemViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val product = uiState.product
    val store = uiState.nearestStore

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Canje") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (product == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoggitoOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CardGiftcard, null, Modifier.size(64.dp), tint = DoggitoOrange)
                Spacer(Modifier.height(16.dp))
                Text("Confirmar Canje", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(24.dp))

                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Producto", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(product.name, fontWeight = FontWeight.SemiBold)
                        Divider(Modifier.padding(vertical = 12.dp))

                        Text("Costo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, null, tint = DoggiCoinGold, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${product.priceCoins} DoggiCoins", fontWeight = FontWeight.Bold)
                        }
                        Divider(Modifier.padding(vertical = 12.dp))

                        Text("Tu saldo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("${uiState.balance} DoggiCoins", fontWeight = FontWeight.SemiBold)
                        Divider(Modifier.padding(vertical = 12.dp))

                        Text("Saldo después del canje", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            "${uiState.balance - product.priceCoins} DoggiCoins",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.balance >= product.priceCoins) SuccessGreen else ErrorRed
                        )

                        if (store != null) {
                            Divider(Modifier.padding(vertical = 12.dp))
                            Text("Tienda más cercana", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(store.name, fontWeight = FontWeight.SemiBold)
                            Text(store.address, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Vigencia: 30 días para reclamar en tienda",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                if (uiState.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.error!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.confirmRedeem(productId, onRedeemSuccess) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !uiState.isProcessing && uiState.balance >= product.priceCoins,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DoggitoOrange)
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Confirmar Canje", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
