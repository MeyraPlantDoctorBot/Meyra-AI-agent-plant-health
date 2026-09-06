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
    primary = MeyraLeafMint,
    secondary = MeyraHarvestAmber,
    tertiary = MeyraSoilGold,
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF9FAFB),
    onSurface = Color(0xFFF9FAFB)
)

private val LightColorScheme = lightColorScheme(
    primary = MeyraGreenPrimary,
    secondary = MeyraSoilGold,
    tertiary = MeyraHarvestAmber,
    background = MeyraBgLight,
    surface = MeyraSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = MeyraTextDark,
    onSurface = MeyraTextDark,
    onSurfaceVariant = MeyraTextMuted,
    primaryContainer = MeyraGreenLight,
    onPrimaryContainer = MeyraGreenDark,
    surfaceVariant = Color(0xFFF0F2E8),
    outline = MeyraCardBorder
)

@Composable
fun MeyraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
