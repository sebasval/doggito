package com.example.doggitoapp.android.feature.promo

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.domain.model.TransactionType
import com.example.doggitoapp.android.domain.repository.CoinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth

private const val PROMO_PREFS = "doggito_promo_prefs"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimCoinsScreen(
    promoId: String,
    amount: Int,
    onBack: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val coinRepository: CoinRepository = koinInject()
    val supabaseClient: SupabaseClient = koinInject()
    val scope = rememberCoroutineScope()

    val userId = supabaseClient.auth.currentUserOrNull()?.id ?: "local_user"

    // Verificar si ya fue reclamado
    val alreadyClaimed = remember {
        mutableStateOf(isPromoClaimed(context, promoId))
    }
    var claiming by remember { mutableStateOf(false) }
    var justClaimed by remember { mutableStateOf(false) }

    // Animacion del icono de regalo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promocion") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GradientTop, GradientMiddle, GradientBottom, GradientWarm)
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icono principal
                if (justClaimed || alreadyClaimed.value) {
                    // Icono de exito
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn() + fadeIn()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = SuccessGreen
                        )
                    }
                } else {
                    // Icono de regalo con animacion
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        DoggiCoinGold.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = DoggiCoinGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Titulo
                Text(
                    text = if (alreadyClaimed.value && !justClaimed) {
                        "Ya reclamaste esta promocion"
                    } else if (justClaimed) {
                        "DoggiCoins reclamadas!"
                    } else {
                        "Tienes un regalo!"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cantidad de monedas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "+$amount",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DoggiCoinGold
                        )
                        Text(
                            text = "DoggiCoins",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )

                        if (!alreadyClaimed.value && !justClaimed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca el boton para reclamar tus monedas gratis",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (justClaimed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Las monedas se han agregado a tu saldo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SuccessGreen,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Boton de accion
                if (!alreadyClaimed.value && !justClaimed) {
                    Button(
                        onClick = {
                            claiming = true
                            scope.launch {
                                try {
                                    coinRepository.addCoins(
                                        userId = userId,
                                        amount = amount,
                                        type = TransactionType.PROMO,
                                        description = "Promocion: $promoId"
                                    )
                                    markPromoClaimed(context, promoId)
                                    alreadyClaimed.value = true
                                    justClaimed = true
                                } catch (e: Exception) {
                                    // Error silencioso
                                } finally {
                                    claiming = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !claiming,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DoggiCoinGold,
                            contentColor = Color.White
                        )
                    ) {
                        if (claiming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Reclamar $amount DoggiCoins",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Boton para ir al Home
                    Button(
                        onClick = onGoHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DoggitoGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Ir al inicio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- Utilidades para prevenir doble reclamo ---

private fun isPromoClaimed(context: Context, promoId: String): Boolean {
    val prefs = context.getSharedPreferences(PROMO_PREFS, Context.MODE_PRIVATE)
    return prefs.getBoolean(promoId, false)
}

private fun markPromoClaimed(context: Context, promoId: String) {
    val prefs = context.getSharedPreferences(PROMO_PREFS, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(promoId, true).apply()
}
