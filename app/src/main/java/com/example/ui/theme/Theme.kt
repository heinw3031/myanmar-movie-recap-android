package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CinemaDarkColorScheme = darkColorScheme(
    primary = CinemaCrimson,
    onPrimary = Color.White,
    primaryContainer = CinemaCrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = CinemaGold,
    onSecondary = Color(0xFF1A1200),
    secondaryContainer = Color(0xFF332608),
    onSecondaryContainer = CinemaGold,
    tertiary = CinemaEmerald,
    onTertiary = Color(0xFF00210B),
    background = CinemaObsidian,
    onBackground = CinemaTextPrimary,
    surface = CinemaSurface,
    onSurface = CinemaTextPrimary,
    surfaceVariant = CinemaCard,
    onSurfaceVariant = CinemaTextSecondary,
    surfaceContainerLow = CinemaSurface,
    surfaceContainer = CinemaCard,
    surfaceContainerHigh = CinemaElevated,
    outline = CinemaBorder,
    outlineVariant = Color(0xFF242432),
    error = CinemaError,
    onError = Color.White
)

private val CinemaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyanmarMovieRecapTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinemaDarkColorScheme,
        typography = Typography,
        shapes = CinemaShapes,
        content = content
    )
}
