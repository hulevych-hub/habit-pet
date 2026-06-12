package com.example.mobile.presentation.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobile.util.NotificationPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Settings", color = ColorPaletteSettings.Ink) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                SettingsHero(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }

            items(settingsItems(context)) { setting ->
                SettingRow(
                    title = setting.title,
                    description = setting.description,
                    icon = setting.icon,
                    isChecked = setting.isChecked,
                    onCheckedChange = setting.onCheckedChange,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsHero(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ColorPaletteSettings.Card),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPaletteSettings.LavenderSoft)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gentle nudges",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ColorPaletteSettings.Ink
                )
                Text(
                    text = "Keep reminders warm, supportive, and never punishing. Your dragon should invite you back, not pressure you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class SettingItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isChecked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

private fun settingsItems(context: Context): List<SettingItem> {
    return listOf(
        SettingItem(
            title = "Dragon Waiting",
            description = "A soft daily nudge when your dragon is ready for you",
            icon = Icons.Default.Pets,
            isChecked = NotificationPrefs.isDailyReminderEnabled(context),
            onCheckedChange = { checked ->
                NotificationPrefs.setDailyReminderEnabled(context, checked)
            }
        ),
        SettingItem(
            title = "Streak Encouragement",
            description = "Supportive streak messages that celebrate your rhythm",
            icon = Icons.Default.Star,
            isChecked = NotificationPrefs.isStreakReminderEnabled(context),
            onCheckedChange = { checked ->
                NotificationPrefs.setStreakReminderEnabled(context, checked)
            }
        ),
        SettingItem(
            title = "Pet Bond Reminder",
            description = "Warm reminders that your dragon has a welcome ready",
            icon = Icons.Default.FavoriteBorder,
            isChecked = NotificationPrefs.isPetReminderEnabled(context),
            onCheckedChange = { checked ->
                NotificationPrefs.setPetReminderEnabled(context, checked)
            }
        )
    )
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
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ColorPaletteSettings.Card),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ColorPaletteSettings.Violet,
                modifier = Modifier.size(30.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorPaletteSettings.Ink
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ColorPaletteSettings.Violet,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = ColorPaletteSettings.Muted
                )
            )
        }
    }
}

private object ColorPaletteSettings {
    val Card = Color(0xFFFFFFFF)
    val LavenderSoft = Color(0xFFF2EEFF)
    val Violet = Color(0xFF8A76F9)
    val Muted = Color(0xFF6F6A8A)
    val Ink = Color(0xFF302B4A)
}
