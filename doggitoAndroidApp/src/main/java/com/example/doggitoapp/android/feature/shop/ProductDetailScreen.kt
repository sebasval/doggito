package com.example.doggitoapp.android.feature.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.doggitoapp.android.core.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onRedeem: () -> Unit,
    onBack: () -> Unit,
    viewModel: ShopViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val product = uiState.selectedProduct
    val canAfford = product != null && uiState.balance >= product.priceCoins

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Producto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
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
        if (product == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoggitoGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Column(Modifier.padding(20.dp)) {
                    Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = DoggitoGreenLight.copy(alpha = 0.2f)) {
                        Text(
                            product.category.displayName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = DoggitoGreenDark
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(product.description, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)

                    Spacer(Modifier.height(24.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DoggiCoinGold.copy(alpha = 0.08f))
                    ) {
                        Column(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Precio", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, null, tint = DoggiCoinGold, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("${product.priceCoins}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DoggiCoinGoldDark)
                                Text(" DoggiCoins", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tu saldo: ${uiState.balance} DoggiCoins",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (canAfford) SuccessGreen else ErrorRed
                            )
                            if (!canAfford) {
                                Text(
                                    "Te faltan ${product.priceCoins - uiState.balance} monedas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ErrorRed
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onRedeem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = canAfford,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
                    ) {
                        Icon(Icons.Default.ShoppingCart, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Canjear Producto", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
