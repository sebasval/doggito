package com.example.doggitoapp.android.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.DoggiCoinGold
import com.example.doggitoapp.android.core.theme.DoggiCoinGoldDark

@Composable
fun DoggiCoinBadge(
    balance: Int,
    modifier: Modifier = Modifier,
    animated: Boolean = false
) {
    val scale = if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "coin_pulse")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatableSpec(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        ).value
    } else 1f

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DoggiCoinGold.copy(alpha = 0.15f),
        modifier = modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MonetizationOn,
                contentDescription = "DoggiCoins",
                tint = DoggiCoinGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "$balance",
                fontWeight = FontWeight.Bold,
                color = DoggiCoinGoldDark,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun <T> infiniteRepeatableSpec(
    animation: DurationBasedAnimationSpec<T>,
    repeatMode: RepeatMode
): InfiniteRepeatableSpec<T> = infiniteRepeatable(animation, repeatMode)
