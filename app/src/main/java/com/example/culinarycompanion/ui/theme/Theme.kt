package com.example.culinarycompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006D3B),
    secondary = Color(0xFF4A6357),
    tertiary = Color(0xFF3A5C4F),
    surface = Color(0xFFFFFBFE),
    surfaceVariant = Color(0xFFF1F1F1),
    background = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF69F0AE),
    secondary = Color(0xFFB1C9BB),
    tertiary = Color(0xFFA3CCC1),
    surface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFF49454F),
    background = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun RecipeBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        // Fixed: Use your custom typography instance
        typography = AppTypography,
        content = content
    )
}