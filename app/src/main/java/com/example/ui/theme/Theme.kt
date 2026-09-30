package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1E1404),
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = WeddingChampagne,
    onSecondary = Color(0xFF2E2416),
    tertiary = MaroonLight,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFEDE5DF),
    surface = DarkSurface,
    onSurface = Color(0xFFEDE5DF),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCEC2B8),
    outline = Color(0xFF5A4F46),
    outlineVariant = Color(0xFF38312B)
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = GoldLight,
    onPrimaryContainer = Color(0xFF2D2005),
    secondary = MaroonAccent,
    onSecondary = Color.White,
    tertiary = GoldPrimary,
    onTertiary = Color.Black,
    background = LightBackground,
    onBackground = Color(0xFF1F1B18),
    surface = LightSurface,
    onSurface = Color(0xFF1F1B18),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF564D45),
    outline = Color(0xFFC4B8AD),
    outlineVariant = Color(0xFFE2D6CB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to luxurious dark photo studio aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
