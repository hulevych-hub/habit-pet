package com.example.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun HabitPetTheme(
    appTheme: AppThemeOption = AppThemePrefs.currentTheme(),
    content: @Composable () -> Unit
) {
    val palette = appTheme.colors
    val colorScheme = if (appTheme.isDark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            primaryContainer = palette.primaryContainer,
            onPrimaryContainer = palette.onPrimaryContainer,
            secondary = palette.secondary,
            onSecondary = palette.onSecondary,
            secondaryContainer = palette.secondaryContainer,
            onSecondaryContainer = palette.onSecondaryContainer,
            tertiary = palette.tertiary,
            onTertiary = palette.onTertiary,
            tertiaryContainer = palette.tertiaryContainer,
            onTertiaryContainer = palette.onTertiaryContainer,
            background = palette.background,
            onBackground = palette.onBackground,
            surface = palette.surface,
            onSurface = palette.onSurface,
            error = palette.danger,
            onError = palette.onSurface,
            outline = palette.outline,
            outlineVariant = palette.outlineVariant,
            surfaceVariant = palette.surfaceVariant,
            onSurfaceVariant = palette.onSurfaceVariant,
            inverseSurface = palette.onSurface,
            inverseOnSurface = palette.surface,
            inversePrimary = palette.primary
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            primaryContainer = palette.primaryContainer,
            onPrimaryContainer = palette.onPrimaryContainer,
            secondary = palette.secondary,
            onSecondary = palette.onSecondary,
            secondaryContainer = palette.secondaryContainer,
            onSecondaryContainer = palette.onSecondaryContainer,
            tertiary = palette.tertiary,
            onTertiary = palette.onTertiary,
            tertiaryContainer = palette.tertiaryContainer,
            onTertiaryContainer = palette.onTertiaryContainer,
            background = palette.background,
            onBackground = palette.onBackground,
            surface = palette.surface,
            onSurface = palette.onSurface,
            error = palette.danger,
            onError = palette.onSurface,
            outline = palette.outline,
            outlineVariant = palette.outlineVariant,
            surfaceVariant = palette.surfaceVariant,
            onSurfaceVariant = palette.onSurfaceVariant,
            inverseSurface = palette.onSurface,
            inverseOnSurface = palette.surface,
            inversePrimary = palette.primary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = palette.background.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.navigationBarDividerColor = Color.Transparent.toArgb()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !appTheme.isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !appTheme.isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val Typography = Typography()
