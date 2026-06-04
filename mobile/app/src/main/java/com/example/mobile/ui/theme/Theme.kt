package com.example.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobile.R
import com.example.mobile.ui.theme.ColorPalette

@Composable
fun HabitPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = ColorPalette.Purple500,
            onPrimary = ColorPalette.Purple50,
            secondary = ColorPalette.Teal200,
            onSecondary = ColorPalette.Teal900,
            background = ColorPalette.Background,
            onBackground = ColorPalette.Purple900,
            surface = ColorPalette.Surface,
            onSurface = ColorPalette.Purple900,
            error = ColorPalette.Red700
        )
        else -> lightColorScheme(
            primary = ColorPalette.Purple500,
            onPrimary = ColorPalette.Purple50,
            secondary = ColorPalette.Teal200,
            onSecondary = ColorPalette.Teal900,
            background = ColorPalette.Background,
            onBackground = ColorPalette.Purple900,
            surface = ColorPalette.Surface,
            onSurface = ColorPalette.Purple900,
            error = ColorPalette.Red700
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val Typography = Typography(
    // You can customize typography here if needed
    // For now, using defaults
)