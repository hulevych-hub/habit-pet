package com.example.mobile.presentation.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.domain.ExpConfig
import com.example.mobile.presentation.ui.components.CoinIcon
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.util.NotificationPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToRewardsLocked: () -> Unit
) {
    val context = LocalContext.current
    val progressUiState by homeScreenViewModel.uiState.collectAsState()
    var settingsError by remember { mutableStateOf<String?>(null) }

    // Remember states dynamically to guarantee instantaneous toggle UI rendering updates
    var isDailyEnabled by remember { mutableStateOf(NotificationPrefs.isDailyReminderEnabled(context)) }
    var isStreakEnabled by remember { mutableStateOf(NotificationPrefs.isStreakReminderEnabled(context)) }
    var isPetEnabled by remember { mutableStateOf(NotificationPrefs.isPetReminderEnabled(context)) }

    Scaffold(
        containerColor = Color(0xFFFAFAFC),
        topBar = {
            GamifiedFixedHeader(
                streak = progressUiState.globalStreak,
                coins = progressUiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(progressUiState.pet.evolutionStage),
                streakCompletedToday = progressUiState.globalStreakCompletedToday,
                onCoinsClick = onNavigateToRewardsLocked
            )
        }
    ) { padding ->
        if (!settingsError.isNullOrBlank()) {
            ErrorStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                message = settingsError.orEmpty(),
                onRetry = { settingsError = null }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
            item {
                SettingsHero()
            }

            item {
                SettingRow(
                    title = "Dragon Waiting",
                    description = "A soft daily nudge when your dragon is ready for you",
                    icon = Icons.Default.Pets,
                    isChecked = isDailyEnabled,
                    onCheckedChange = { checked ->
                        if (saveNotificationSetting(
                                context,
                                checked,
                                NotificationPrefs::setDailyReminderEnabled,
                                "Daily reminder could not be saved",
                                { settingsError = it }
                            )) {
                            isDailyEnabled = checked
                        }
                    }
                )
            }

            item {
                SettingRow(
                    title = "Streak Encouragement",
                    description = "Supportive streak messages that celebrate your rhythm",
                    icon = Icons.Default.Star,
                    isChecked = isStreakEnabled,
                    onCheckedChange = { checked ->
                        if (saveNotificationSetting(
                                context,
                                checked,
                                NotificationPrefs::setStreakReminderEnabled,
                                "Streak reminder could not be saved",
                                { settingsError = it }
                            )) {
                            isStreakEnabled = checked
                        }
                    }
                )
            }

            item {
                SettingRow(
                    title = "Pet Bond Reminder",
                    description = "Warm reminders that your dragon has a welcome ready",
                    icon = Icons.Default.FavoriteBorder,
                    isChecked = isPetEnabled,
                    onCheckedChange = { checked ->
                        if (saveNotificationSetting(
                                context,
                                checked,
                                NotificationPrefs::setPetReminderEnabled,
                                "Pet reminder could not be saved",
                                { settingsError = it }
                            )) {
                            isPetEnabled = checked
                        }
                    }
                )
            }

            item {
                CopyrightFooter()
            }
            }
        }
    }
}

@Composable
private fun CopyrightFooter() {
    Text(
        text = "Copyright © 2026 Hulevych Enterprises. All rights to cuddle Vanessa Baron reserved.",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        color = Color(0xFF7A7786),
        fontSize = 10.sp,
        lineHeight = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

private fun saveNotificationSetting(
    context: Context,
    checked: Boolean,
    save: (Context, Boolean) -> Unit,
    errorMessage: String,
    onError: (String) -> Unit
): Boolean = try {
    save(context, checked)
    true
} catch (e: Exception) {
    onError(e.message ?: errorMessage)
    false
}

@Composable
private fun GamifiedFixedHeader(
    streak: Int,
    coins: Int,
    stageName: String,
    streakCompletedToday: Boolean,
    onCoinsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
        val streakTint = if (streakCompletedToday) ColorPaletteSettings.Flame else Color(0xFFA9A3B8)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = streakTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$streak d",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteSettings.Ink
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ColorPaletteSettings.Violet.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = ColorPaletteSettings.Violet,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stageName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteSettings.Violet
                    )
                }
            }

            Row(
                modifier = Modifier.clickable(onClick = onCoinsClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CoinIcon(
                    modifier = Modifier.size(22.dp),
                    tint = ColorPaletteSettings.Amber
                )
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteSettings.Ink
                )
            }
        }
    }
}

@Composable
private fun SettingsHero() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteSettings.Ink),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ColorPaletteSettings.Ink, Color(0xFF231D3D))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Gentle Nudges",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Keep reminders warm and supportive. Your companion invites you back without pressure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = ColorPaletteSettings.Violet,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteSettings.Card),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorPaletteSettings.Line.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(ColorPaletteSettings.Violet.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ColorPaletteSettings.Violet,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteSettings.Ink
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorPaletteSettings.Muted,
                    lineHeight = 16.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ColorPaletteSettings.Violet,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = ColorPaletteSettings.Line,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

private object ColorPaletteSettings {
    val Card = Color(0xFFFFFFFF)
    val Violet = Color(0xFF8A76F9)
    val Flame = Color(0xFFFF6B35)
    val Amber = Color(0xFFFFB84D)
    val Line = Color(0xFFEBE9F5)
    val Muted = Color(0xFF8E8A9F)
    val Ink = Color(0xFF1E1A34)
}