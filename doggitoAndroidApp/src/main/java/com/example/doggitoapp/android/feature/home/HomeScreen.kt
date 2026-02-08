package com.example.doggitoapp.android.feature.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.domain.model.Product
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToRunning: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRedeemHistory: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DoggitoGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 100.dp)
        ) {
            // Top bar: plan badge + settings + pet avatar
            TopBarRow(
                petPhotoUri = uiState.pet?.photoUri,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToProfile = onNavigateToProfile
            )

            // Combined header: DoggiCoins (left) + Task Progress (right)
            HomeHeader(
                balance = uiState.balance,
                petName = uiState.pet?.name,
                streakDays = uiState.streak?.currentStreak ?: 0,
                completedTasks = uiState.completedTasks,
                totalTasks = uiState.totalTasks,
                onViewHistory = onNavigateToRedeemHistory,
                onNavigateToTasks = onNavigateToTasks
            )

            Spacer(Modifier.height(12.dp))

            // Product Card Stack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (uiState.products.isEmpty()) {
                    // Shimmer loading placeholder
                    ShimmerProductCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                    )
                } else {
                    ProductCardStack(
                        products = uiState.products,
                        balance = uiState.balance,
                        onProductClick = onNavigateToProductDetail
                    )
                }
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────

@Composable
private fun TopBarRow(
    petPhotoUri: String?,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Freemium plan badge
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = DoggitoAmberLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Freemium",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Settings
        IconButton(onClick = onNavigateToSettings) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }

        // Pet avatar
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onNavigateToProfile),
            shape = CircleShape,
            color = DoggitoGreenDark.copy(alpha = 0.5f)
        ) {
            if (petPhotoUri != null) {
                AsyncImage(
                    model = petPhotoUri,
                    contentDescription = "Mascota",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = "Mascota",
                    modifier = Modifier.padding(8.dp),
                    tint = Color.White
                )
            }
        }
    }
}

// ─── Combined Header: Coins (left) + Tasks (right) ────────────────────

@Composable
private fun HomeHeader(
    balance: Int,
    petName: String?,
    streakDays: Int,
    completedTasks: Int,
    totalTasks: Int,
    onViewHistory: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left column: DoggiCoins
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Balance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onViewHistory)
            ) {
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = DoggitoAmberLight,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$balance",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "DoggiCoins",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 38.dp)
            )

            Spacer(Modifier.height(4.dp))

            // Pet name + streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                petName?.let {
                    Text(
                        text = "con $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                if (streakDays > 0) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                null,
                                tint = DoggitoAmberLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "$streakDays",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // History link
            TextButton(
                onClick = onViewHistory,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Ver historial",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Right column: Task Progress card
        Surface(
            modifier = Modifier
                .clickable(onClick = onNavigateToTasks),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.12f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 5.dp,
                        color = DoggitoAmberLight,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                    Text(
                        "$completedTasks/$totalTasks",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tareas",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ─── Product Card Stack ────────────────────────────────────────────────

@Composable
private fun ProductCardStack(
    products: List<Product>,
    balance: Int,
    onProductClick: (String) -> Unit
) {
    val cardOrder = remember { mutableStateListOf(*products.indices.toList().toTypedArray()) }
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val visibleCount = minOf(cardOrder.size, 3)

    Box(modifier = Modifier.fillMaxSize()) {
        for (i in (visibleCount - 1) downTo 0) {
            val productIndex = cardOrder[i]
            val product = products.getOrNull(productIndex) ?: continue

            val depthOffsetY = i * 14f
            val depthOffsetX = i * 5f
            val depthRotation = i * -1.8f

            val modifier = if (i == 0) {
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .offset {
                        IntOffset(
                            offsetX.value.roundToInt(),
                            offsetY.value.roundToInt()
                        )
                    }
                    .graphicsLayer {
                        val drag = (offsetX.value.absoluteValue + offsetY.value.absoluteValue)
                        rotationZ = offsetX.value * 0.03f
                        alpha = 1f - (drag / 2000f).coerceIn(0f, 0.3f)
                    }
                    .pointerInput(cardOrder.toList()) {
                        detectDragGestures(
                            onDragEnd = {
                                val totalDrag =
                                    offsetX.value.absoluteValue + offsetY.value.absoluteValue
                                coroutineScope.launch {
                                    if (totalDrag > 120f) {
                                        launch { offsetX.animateTo(offsetX.value * 4f, tween(250)) }
                                        launch { offsetY.animateTo(offsetY.value * 4f, tween(250)) }
                                        kotlinx.coroutines.delay(200)
                                        val removed = cardOrder.removeAt(0)
                                        cardOrder.add(removed)
                                        offsetX.snapTo(0f)
                                        offsetY.snapTo(0f)
                                    } else {
                                        launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
                                        launch { offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    launch { offsetX.animateTo(0f, spring()) }
                                    launch { offsetY.animateTo(0f, spring()) }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                                    launch { offsetY.snapTo(offsetY.value + dragAmount.y) }
                                }
                            }
                        )
                    }
            } else {
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .graphicsLayer {
                        translationY = depthOffsetY
                        translationX = depthOffsetX
                        rotationZ = depthRotation
                        alpha = 1f - (i * 0.1f)
                    }
            }

            ProductCard(
                product = product,
                balance = balance,
                modifier = modifier,
                onClick = { onProductClick(product.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCard(
    product: Product,
    balance: Int,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val canAfford = balance >= product.priceCoins

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Product image (top half)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
                // Category badge
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        product.category.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            // Product info (bottom)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(16.dp))

                // Price row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Price
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DoggiCoinGold.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = DoggiCoinGold
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${product.priceCoins}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DoggiCoinGoldDark
                            )
                        }
                    }

                    // Affordability indicator
                    if (canAfford) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SuccessGreen.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    modifier = Modifier.size(16.dp),
                                    tint = SuccessGreen
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Disponible",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    } else {
                        Text(
                            "Faltan ${product.priceCoins - balance}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Tap hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Toca para ver detalles",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextSecondary.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ─── Shimmer Loading Placeholder ─────────────────────────────────────────

@Composable
private fun ShimmerProductCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.08f),
        Color.White.copy(alpha = 0.18f),
        Color.White.copy(alpha = 0.08f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 200f, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(brush)
            )

            // Info placeholder
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(10.dp))
                // Description shimmer lines
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(20.dp))
                // Price shimmer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}
