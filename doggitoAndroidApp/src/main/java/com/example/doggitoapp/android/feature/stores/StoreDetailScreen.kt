package com.example.doggitoapp.android.feature.stores

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.DoggitoOrange
import com.example.doggitoapp.android.core.theme.DoggitoTeal
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    storeId: String,
    onBack: () -> Unit,
    viewModel: StoresViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(storeId) {
        viewModel.loadStore(storeId)
    }

    val store = uiState.selectedStore

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Tienda") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (store == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoggitoOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Icon(Icons.Default.Store, null, Modifier.size(64.dp), tint = DoggitoOrange)
                Spacer(Modifier.height(12.dp))
                Text(store.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(store.address, style = MaterialTheme.typography.bodyLarge)

                Spacer(Modifier.height(24.dp))

                // Info cards
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow(Icons.Default.Schedule, "Horario", store.openingHours)
                        Divider(Modifier.padding(vertical = 8.dp))
                        InfoRow(Icons.Default.Phone, "Teléfono", store.phone)
                        Divider(Modifier.padding(vertical = 8.dp))
                        InfoRow(Icons.Default.Email, "Email", store.email)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Navigation button
                Button(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=${store.latitude},${store.longitude}")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Fallback to browser
                            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${store.latitude},${store.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DoggitoOrange)
                ) {
                    Icon(Icons.Default.Navigation, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ir a la Tienda (Google Maps)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))

                // Call button
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Phone, null, tint = DoggitoTeal)
                    Spacer(Modifier.width(8.dp))
                    Text("Llamar")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = DoggitoOrange, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
