package com.example.pocketcalculator.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF365B32), onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E7A3), onPrimaryContainer = Color(0xFF23351C),
    secondaryContainer = Color(0xFFE0E8D2), onSecondaryContainer = Color(0xFF34442B),
    background = Color(0xFFF7F8F2), onBackground = Color(0xFF1E2822),
    surface = Color(0xFFF7F8F2), onSurface = Color(0xFF1E2822),
    surfaceContainer = Color(0xFFEFF2E9), surfaceContainerHigh = Color(0xFFE7EBDF),
    surfaceVariant = Color(0xFFE3E8D9), onSurfaceVariant = Color(0xFF4C594B),
    outline = Color(0xFF73806D), outlineVariant = Color(0xFFCCD4C4),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFC8DF97), onPrimary = Color(0xFF243A1C),
    primaryContainer = Color(0xFF3D5130), onPrimaryContainer = Color(0xFFE0EEC9),
    secondaryContainer = Color(0xFF343F2E), onSecondaryContainer = Color(0xFFD9E5CE),
    background = Color(0xFF141C17), onBackground = Color(0xFFE4EBDF),
    surface = Color(0xFF141C17), onSurface = Color(0xFFE4EBDF),
    surfaceContainer = Color(0xFF1D2820), surfaceContainerHigh = Color(0xFF263229),
    surfaceVariant = Color(0xFF354236), onSurfaceVariant = Color(0xFFC3CFBD),
    outline = Color(0xFF8D9B85), outlineVariant = Color(0xFF45533E),
)
@Composable
fun CalculatorTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
