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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.core.util.DateUtils
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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

    // Card stack state - 4 cards (COINS removed, shown in header)
    val cards = remember {
        mutableStateListOf(
            HomeCard.TASKS,
            HomeCard.RUNNING,
            HomeCard.SHOP,
            HomeCard.PET
        )
    }

    DoggitoGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 100.dp) // space for bottom nav
        ) {
            // Top bar: brand + settings + avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "doggito.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.weight(1f))

                // Settings
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Avatar circle
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = DoggitoGreenDark.copy(alpha = 0.5f)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil",
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
            }

            // DoggiCoins header - central element of the app
            DoggiCoinsHeader(
                balance = uiState.balance,
                petName = uiState.pet?.name,
                streakDays = uiState.streak?.currentStreak ?: 0,
                onViewHistory = onNavigateToRedeemHistory
            )

            Spacer(Modifier.height(16.dp))

            // Card Stack - no dots, back cards peek out as visual indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CardStack(
                    cards = cards,
                    uiState = uiState,
                    onNavigateToTasks = onNavigateToTasks,
                    onNavigateToRunning = onNavigateToRunning,
                    onNavigateToShop = onNavigateToShop,
                    onNavigateToProfile = onNavigateToProfile
                )
            }
        }
    }
}

@Composable
private fun DoggiCoinsHeader(
    balance: Int,
    petName: String?,
    streakDays: Int,
    onViewHistory: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // Balance row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onViewHistory)
        ) {
            Icon(
                Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = DoggitoAmberLight,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "$balance",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "DoggiCoins",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 48.dp) // align under the number
        )

        Spacer(Modifier.height(6.dp))

        // Pet name + streak row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            petName?.let {
                Text(
                    text = "con $it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.weight(1f))
            if (streakDays > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            null,
                            tint = DoggitoAmberLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$streakDays dias",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
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
                "Ver historial de canjes",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CardStack(
    cards: MutableList<HomeCard>,
    uiState: HomeUiState,
    onNavigateToTasks: () -> Unit,
    onNavigateToRunning: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Show up to 3 stacked cards for depth effect
    val visibleCount = minOf(cards.size, 3)

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw back cards first (bottom of stack)
        for (i in (visibleCount - 1) downTo 0) {
            val card = cards[i]

            // Stacked card effect: offset + subtle rotation so edges peek out
            val depthOffsetY = i * 14f
            val depthOffsetX = i * 5f
            val depthRotation = i * -1.8f

            val modifier = if (i == 0) {
                // Top card - draggable
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
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
                    .pointerInput(cards.toList()) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val totalDrag =
                                    offsetX.value.absoluteValue + offsetY.value.absoluteValue
                                coroutineScope.launch {
                                    if (totalDrag > 120f) {
                                        // Animate out
                                        launch { offsetX.animateTo(offsetX.value * 4f, tween(250)) }
                                        launch { offsetY.animateTo(offsetY.value * 4f, tween(250)) }
                                        kotlinx.coroutines.delay(200)
                                        // Move card to back
                                        val removed = cards.removeAt(0)
                                        cards.add(removed)
                                        // Reset position
                                        offsetX.snapTo(0f)
                                        offsetY.snapTo(0f)
                                    } else {
                                        // Snap back
                                        launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
                                        launch { offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
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
                // Background cards - offset and rotated so edges peek out
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
                    .graphicsLayer {
                        translationY = depthOffsetY
                        translationX = depthOffsetX
                        rotationZ = depthRotation
                        alpha = 1f - (i * 0.1f)
                    }
            }

            HomeCardContent(
                card = card,
                uiState = uiState,
                modifier = modifier,
                onAction = {
                    when (card) {
                        HomeCard.TASKS -> onNavigateToTasks()
                        HomeCard.RUNNING -> onNavigateToRunning()
                        HomeCard.SHOP -> onNavigateToShop()
                        HomeCard.PET -> onNavigateToProfile()
                    }
                }
            )
        }
    }
}

enum class HomeCard {
    TASKS, RUNNING, SHOP, PET
}

@Composable
private fun HomeCardContent(
    card: HomeCard,
    uiState: HomeUiState,
    modifier: Modifier,
    onAction: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        when (card) {
            HomeCard.TASKS -> TasksCardContent(uiState, onAction)
            HomeCard.RUNNING -> RunningCardContent(uiState, onAction)
            HomeCard.SHOP -> ShopCardContent(onAction)
            HomeCard.PET -> PetCardContent(uiState, onAction)
        }
    }
}

@Composable
private fun TasksCardContent(uiState: HomeUiState, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            CardLabel(icon = Icons.Default.TaskAlt, label = "Tareas Diarias", color = DoggitoGreen)
            Spacer(Modifier.height(20.dp))

            // Progress circle
            val progress = if (uiState.totalTasks > 0) uiState.completedTasks.toFloat() / uiState.totalTasks else 0f
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(140.dp),
                    strokeWidth = 10.dp,
                    color = DoggitoGreen,
                    trackColor = DoggitoGreenLight.copy(alpha = 0.3f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${uiState.completedTasks}/${uiState.totalTasks}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "completadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.completedTasks == uiState.totalTasks && uiState.totalTasks > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Todas las tareas completadas!",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom action
        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
        ) {
            Text("Ver Todas las Tareas", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RunningCardContent(uiState: HomeUiState, onAction: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DoggitoGreenDark.copy(alpha = 0.05f),
                            DoggitoGreen.copy(alpha = 0.08f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                CardLabel(icon = Icons.Default.DirectionsRun, label = "Actividad Fisica", color = DoggitoGreenDark)
                Spacer(Modifier.height(24.dp))

                // Big icon
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = DoggitoGreen.copy(alpha = 0.12f)
                    ) {
                        Icon(
                            Icons.Default.DirectionsRun,
                            contentDescription = null,
                            modifier = Modifier.padding(28.dp),
                            tint = DoggitoGreenDark
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Sal a correr con tu perro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Gana 10 DoggiCoins por cada km recorrido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Start button - large and thumb-friendly
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreenDark)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Iniciar Actividad", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ShopCardContent(onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            CardLabel(icon = Icons.Default.ShoppingBag, label = "Tienda", color = DoggitoGreen)
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = DoggitoGreenLight.copy(alpha = 0.2f)
                ) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.padding(28.dp),
                        tint = DoggitoGreen
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Canjea tus DoggiCoins",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Descubre premios exclusivos para ti y tu mascota. Desde juguetes hasta consultas veterinarias.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
        ) {
            Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Explorar Tienda", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PetCardContent(uiState: HomeUiState, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            CardLabel(icon = Icons.Default.Pets, label = "Mi Mascota", color = DoggitoGreenDark)
            Spacer(Modifier.height(24.dp))

            uiState.pet?.let { pet ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = DoggitoGreenLight.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.padding(24.dp),
                            tint = DoggitoGreenDark
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    pet.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    pet.breed,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoChip(label = "${pet.weight} kg", icon = Icons.Default.Scale)
                    InfoChip(label = DateUtils.formatDate(pet.birthDate), icon = Icons.Default.Cake)
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = DoggitoGreenLight
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Registra a tu mascota",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreenDark)
        ) {
            Text("Ver Perfil", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CardLabel(icon: ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp).size(20.dp),
                tint = color
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun InfoChip(label: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DoggitoGreenLight.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = DoggitoGreenDark)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        }
    }
}
