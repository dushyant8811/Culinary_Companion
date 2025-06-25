package com.example.culinarycompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val RecipeBookLightColors = lightColorScheme(
    primary = Color(0xFF006D3B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF95F6BB),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF4A6357),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8D8),
    onSecondaryContainer = Color(0xFF062019),
    tertiary = Color(0xFF3A5C4F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCE0D3),
    onTertiaryContainer = Color(0xFF002019),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DB),
    onSurfaceVariant = Color(0xFF404943),
    outline = Color(0xFF707973),
    inverseOnSurface = Color(0xFFEFF1ED),
    inverseSurface = Color(0xFF2E312E),
    inversePrimary = Color(0xFF79D9A0),
    surfaceTint = Color(0xFF006D3B),
)

private val RecipeBookDarkColors = darkColorScheme(
    primary = Color(0xFF79D9A0),
    onPrimary = Color(0xFF00391D),
    primaryContainer = Color(0xFF00522C),
    onPrimaryContainer = Color(0xFF95F6BB),
    secondary = Color(0xFFB1C9BB),
    onSecondary = Color(0xFF1D352A),
    secondaryContainer = Color(0xFF334B40),
    onSecondaryContainer = Color(0xFFCCE8D8),
    tertiary = Color(0xFFA3CCC1),
    onTertiary = Color(0xFF03372C),
    tertiaryContainer = Color(0xFF214E43),
    onTertiaryContainer = Color(0xFFBCE0D3),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFC0C9C1),
    outline = Color(0xFF8A938C),
    inverseOnSurface = Color(0xFF191C1A),
    inverseSurface = Color(0xFFE1E3DF),
    inversePrimary = Color(0xFF006D3B),
    surfaceTint = Color(0xFF79D9A0),
)

private val RecipeBookTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun RecipeBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RecipeBookDarkColors else RecipeBookLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RecipeBookTypography,
        content = content
    )
}