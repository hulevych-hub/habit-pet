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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ExpConfig
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.ui.components.GamifiedFixedHeader
import com.example.mobile.presentation.ui.components.StreakCalendarOverlay
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.AppThemeOption
import com.example.mobile.ui.theme.AppThemePrefs
import com.example.mobile.ui.theme.HabitPetTheme
import com.example.mobile.ui.theme.DesignTokens
import com.example.mobile.util.NotificationPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    homeScreenViewModel: HomeScreenViewModel,
    onNavigateToRewardsLocked: () -> Unit
) {
    val context = LocalContext.current
    val progressUiState by homeScreenViewModel.uiState.collectAsState()
    val streakCalendarState by homeScreenViewModel.streakCalendarState.collectAsState()
    var settingsError by remember { mutableStateOf<String?>(null) }

    NotificationSettingsContent(
        progressUiState = progressUiState,
        onNavigateToRewardsLocked = onNavigateToRewardsLocked,
        onError = { settingsError = it },
        onStreakClick = homeScreenViewModel::openGlobalStreakCalendar,
        onStreakCalendarDismiss = homeScreenViewModel::closeStreakCalendar,
        onPreviousStreakMonth = homeScreenViewModel::showPreviousStreakMonth,
        onNextStreakMonth = homeScreenViewModel::showNextStreakMonth,
        streakCalendarState = streakCalendarState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsContent(
    progressUiState: HomeScreenViewModel.UiState,
    onNavigateToRewardsLocked: () -> Unit,
    onError: (String) -> Unit,
    onStreakClick: () -> Unit,
    onStreakCalendarDismiss: () -> Unit,
    onPreviousStreakMonth: () -> Unit,
    onNextStreakMonth: () -> Unit,
    streakCalendarState: StreakCalendarUiState?
) {
    val context = LocalContext.current
    var settingsError by remember { mutableStateOf<String?>(null) }

    // Remember states dynamically to guarantee instantaneous toggle UI rendering updates
    var isDailyEnabled by remember { mutableStateOf(NotificationPrefs.isDailyReminderEnabled(context)) }
    var isStreakEnabled by remember { mutableStateOf(NotificationPrefs.isStreakReminderEnabled(context)) }
    var isPetEnabled by remember { mutableStateOf(NotificationPrefs.isPetReminderEnabled(context)) }
    var selectedTheme by remember { mutableStateOf(AppThemePrefs.currentTheme()) }

    Scaffold(
        containerColor = AppTheme.current.background,
        topBar = {
            GamifiedFixedHeader(
                streak = progressUiState.globalStreak,
                coins = progressUiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(progressUiState.pet.evolutionStage),
                streakCompletedToday = progressUiState.globalStreakCompletedToday,
                streakPartialToday = progressUiState.globalStreakPartialToday,
                onCoinsClick = onNavigateToRewardsLocked,
                onStreakClick = onStreakClick
            )
        }
    ) { padding ->
        if (!settingsError.isNullOrBlank()) {
            ErrorStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(DesignTokens.Section.horizontalPadding),
                message = settingsError.orEmpty(),
                onRetry = { settingsError = null }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = DesignTokens.Section.horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignTokens.space12),
                contentPadding = PaddingValues(top = DesignTokens.Section.topPadding, bottom = DesignTokens.Section.bottomPadding)
            ) {
            item {
                SettingsHero()
            }

            item {
                ThemeSelectionSection(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { option ->
                        AppThemePrefs.setTheme(context, option)
                        selectedTheme = option
                    }
                )
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
                                onError
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
                                onError
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
                                onError
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

        StreakCalendarOverlay(
            state = streakCalendarState,
            onDismiss = onStreakCalendarDismiss,
            onPreviousMonth = onPreviousStreakMonth,
            onNextMonth = onNextStreakMonth
        )
    }
}

@Composable
private fun ThemeSelectionSection(
    selectedTheme: AppThemeOption,
    onThemeSelected: (AppThemeOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
        shape = DesignTokens.cardCorner,
        border = androidx.compose.foundation.BorderStroke(DesignTokens.strokeThin, AppTheme.current.outline.copy(alpha = DesignTokens.alpha40)),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.elevationXs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Card.padding),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.space12)
        ) {
            Text(
                text = "App Color Palette",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.current.ink
            )

            AppThemeOption.values().forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeSelected(option) }
                        .padding(vertical = DesignTokens.space6),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.space10)
                ) {
                    RadioButton(
                        selected = selectedTheme == option,
                        onClick = { onThemeSelected(option) }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.space2)) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.current.ink
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.current.muted
                        )
                    }
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
            .padding(horizontal = DesignTokens.space8),
        color = AppTheme.current.muted,
        fontSize = 10.sp,
        lineHeight = DesignTokens.space12.value.sp,
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
private fun SettingsHero() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.primary),
        shape = DesignTokens.cardCornerRounded
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AppTheme.current.primary, AppTheme.current.primaryContainer)
                    )
                )
                .padding(DesignTokens.Section.horizontalPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.space4)
                ) {
                    Text(
                        text = "Gentle Nudges",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.onPrimary
                    )
                    Text(
                        text = "Keep reminders warm and supportive. Your companion invites you back without pressure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.current.onPrimary.copy(alpha = DesignTokens.alpha65)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(DesignTokens.space56)
                        .background(AppTheme.current.onPrimary.copy(alpha = DesignTokens.alpha6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AppTheme.current.violet,
                        modifier = Modifier.size(DesignTokens.Icon.size2xl)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390px,height=844px,dpi=420")
@Composable
private fun NotificationSettingsScreenPreview() {
    val pet = PetEntity(
        id = 1,
        name = "Luna",
        level = 3,
        xp = 180,
        evolutionStage = 1,
        equippedOutfit = "classic_blue_outfit",
        equippedBackground = "misty_meadow_background",
        equippedAura = null,
        mood = "Calm"
    )
    HabitPetTheme {
        NotificationSettingsContent(
            progressUiState = HomeScreenViewModel.UiState(
                globalStreak = 4,
                habits = emptyList(),
                pet = pet,
                completedTodayXp = emptyMap(),
                totalCoins = 128,
                lastStreakDate = 0L,
                currentCombo = 0,
                lastHabitCompletionTimestamp = 0L,
                globalStreakCompletedToday = false
            ),
            onNavigateToRewardsLocked = {},
            onError = {},
            onStreakClick = {},
            onStreakCalendarDismiss = {},
            onPreviousStreakMonth = {},
            onNextStreakMonth = {},
            streakCalendarState = null
        )
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
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
        shape = DesignTokens.cardCorner,
        border = androidx.compose.foundation.BorderStroke(DesignTokens.strokeThin, AppTheme.current.outline.copy(alpha = DesignTokens.alpha40)),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.elevationXs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Card.padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.space14)
        ) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.Card.iconSize)
                    .background(AppTheme.current.violet.copy(alpha = DesignTokens.alpha8), RoundedCornerShape(DesignTokens.Card.iconBackgroundRadius)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.current.violet,
                    modifier = Modifier.size(DesignTokens.Icon.sizeLg)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.space2)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.current.ink
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.current.muted,
                    lineHeight = DesignTokens.space16.value.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppTheme.current.onPrimary,
                    checkedTrackColor = AppTheme.current.violet,
                    uncheckedThumbColor = AppTheme.current.onPrimary,
                    uncheckedTrackColor = AppTheme.current.outline,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}
