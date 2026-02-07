package com.example.doggitoapp.android.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DoggitoGreen,
    onPrimary = Color.White,
    primaryContainer = DoggitoGreenLight,
    onPrimaryContainer = DoggitoGreenDark,
    secondary = DoggitoAmber,
    onSecondary = Color.White,
    secondaryContainer = DoggitoAmberLight,
    onSecondaryContainer = DoggitoAmberDark,
    tertiary = DoggiCoinGold,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DoggitoGreenLight,
    onPrimary = DoggitoGreenDeep,
    primaryContainer = DoggitoGreenDark,
    onPrimaryContainer = Color.White,
    secondary = DoggitoAmberLight,
    onSecondary = DoggitoAmberDark,
    secondaryContainer = DoggitoAmber,
    onSecondaryContainer = Color.White,
    tertiary = DoggiCoinGold,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = Color(0xFFE0E8E1),
    surface = SurfaceDark,
    onSurface = Color(0xFFE0E8E1),
    surfaceVariant = Color(0xFF2A3E2C),
    onSurfaceVariant = Color(0xFFBBC8BC),
    error = Color(0xFFFF8A80),
    onError = Color.Black
)

@Composable
fun DoggitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DoggitoTypography,
        content = content
    )
}

// Gradient background composable matching the reference design
@Composable
fun DoggitoGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientTop,
                        GradientMiddle,
                        GradientBottom,
                        GradientWarm
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        content()
    }
}
