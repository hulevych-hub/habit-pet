package com.example.mobile.presentation.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobile.presentation.viewmodel.NotificationSettingsViewModel
import com.example.mobile.util.NotificationPrefs

/**
 * Screen for configuring notification settings
 */
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Gentle Notification Nudges",
            style = MaterialTheme.typography.headlineMedium
        )

        // Settings list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            items(settingsItems(context)) { setting ->
                SettingRow(
                    title = setting.title,
                    description = setting.description,
                    isChecked = setting.isChecked,
                    onCheckedChange = { checked ->
                        setting.onCheckedChange(checked)
                    }
                )
            }
        }
    }
}

private data class SettingItem(
    val title: String,
    val description: String,
    val isChecked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

private fun settingsItems(context: Context): List<SettingItem> {
    return listOf(
        SettingItem(
            title = "Dragon Waiting",
            description = "A soft daily nudge when your dragon is ready for you",
            isChecked = NotificationPrefs.isDailyReminderEnabled(context),
            onCheckedChange = { checked ->
                NotificationPrefs.setDailyReminderEnabled(context, checked)
            }
        ),
        SettingItem(
            title = "Streak Encouragement",
            description = "Supportive streak messages that celebrate your rhythm",
            isChecked = NotificationPrefs.isStreakReminderEnabled(context),
            onCheckedChange = { checked ->
                NotificationPrefs.setStreakReminderEnabled(context, checked)
            }
        ),
        SettingItem(
            title = "Pet Bond Reminder",
            description = "Warm reminders that your dragon has a welcome ready",
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
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
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
            // Use default colors for simplicity
        )
    }
}