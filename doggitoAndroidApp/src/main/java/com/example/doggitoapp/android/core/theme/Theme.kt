package com.example.doggitoapp.android.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DoggitoOrange,
    onPrimary = Color.White,
    primaryContainer = DoggitoOrangeLight,
    onPrimaryContainer = DoggitoOrangeDark,
    secondary = DoggitoTeal,
    onSecondary = Color.White,
    secondaryContainer = DoggitoTealLight,
    onSecondaryContainer = DoggitoTealDark,
    tertiary = DoggiCoinGold,
    onTertiary = Color.Black,
    background = BackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DoggitoOrangeLight,
    onPrimary = DoggitoOrangeDark,
    primaryContainer = DoggitoOrange,
    onPrimaryContainer = Color.White,
    secondary = DoggitoTealLight,
    onSecondary = DoggitoTealDark,
    secondaryContainer = DoggitoTeal,
    onSecondaryContainer = Color.White,
    tertiary = DoggiCoinGold,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = Color(0xFFE6E1E5),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFFF6659),
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
