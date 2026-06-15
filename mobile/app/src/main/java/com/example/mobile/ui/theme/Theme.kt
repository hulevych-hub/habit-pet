package com.example.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HabitPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = ColorPalette.CosmicLavender,
            onPrimary = Color.White,
            primaryContainer = ColorPalette.SoftAmethyst.copy(alpha = 0.24f),
            onPrimaryContainer = Color.White,
            secondary = ColorPalette.MintGrass,
            onSecondary = ColorPalette.DeepViolet,
            secondaryContainer = ColorPalette.MintGrass.copy(alpha = 0.18f),
            onSecondaryContainer = ColorPalette.SoftInk,
            tertiary = ColorPalette.HoneyAmber,
            onTertiary = ColorPalette.SoftInk,
            background = Color(0xFF151225),
            onBackground = ColorPalette.CardWhite,
            surface = Color(0xFF1D1A31),
            onSurface = ColorPalette.CardWhite,
            surfaceVariant = Color(0xFF2A2642),
            onSurfaceVariant = Color(0xFFE6E0F7),
            outline = Color(0xFF5A537D),
            error = ColorPalette.Danger
        )
        else -> lightColorScheme(
            primary = ColorPalette.CosmicLavender,
            onPrimary = Color.White,
            primaryContainer = ColorPalette.SoftAmethyst.copy(alpha = 0.20f),
            onPrimaryContainer = ColorPalette.SoftInk,
            secondary = ColorPalette.MintGrass,
            onSecondary = ColorPalette.DeepViolet,
            secondaryContainer = ColorPalette.MintGrass.copy(alpha = 0.18f),
            onSecondaryContainer = ColorPalette.SoftInk,
            tertiary = ColorPalette.HoneyAmber,
            onTertiary = ColorPalette.SoftInk,
            background = ColorPalette.WarmAlabaster,
            onBackground = ColorPalette.SoftInk,
            surface = ColorPalette.CardWhite,
            onSurface = ColorPalette.SoftInk,
            surfaceVariant = Color(0xFFF1EDFF),
            onSurfaceVariant = Color(0xFF5F5A78),
            outline = Color(0xFFC9C2E3),
            error = ColorPalette.Danger
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val Typography = Typography()
