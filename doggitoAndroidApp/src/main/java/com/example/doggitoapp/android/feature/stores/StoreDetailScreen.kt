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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.*
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
                title = { Text("Detalle de Tienda", fontWeight = FontWeight.Bold) },
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
        if (store == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoggitoGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Icon(Icons.Default.Store, null, Modifier.size(64.dp), tint = DoggitoGreen)
                Spacer(Modifier.height(12.dp))
                Text(store.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(store.address, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)

                Spacer(Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow(Icons.Default.Schedule, "Horario", store.openingHours)
                        Divider(Modifier.padding(vertical = 8.dp))
                        InfoRow(Icons.Default.Phone, "Telefono", store.phone)
                        Divider(Modifier.padding(vertical = 8.dp))
                        InfoRow(Icons.Default.Email, "Email", store.email)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=${store.latitude},${store.longitude}")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${store.latitude},${store.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
                ) {
                    Icon(Icons.Default.Navigation, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ir a la Tienda (Google Maps)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Phone, null, tint = DoggitoGreen)
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
        Icon(icon, null, tint = DoggitoGreen, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
    }
}
