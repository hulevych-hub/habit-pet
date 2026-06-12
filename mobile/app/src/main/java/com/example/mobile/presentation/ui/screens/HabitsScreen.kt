package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.collectAsState
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ProgressHeader
import com.example.mobile.presentation.ui.components.ProgressHeaderState
import com.example.mobile.presentation.viewmodel.HabitsViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HabitsScreen(
    navController: NavHostController,
    habitsViewModel: HabitsViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val habits by habitsViewModel.habits.collectAsState(initial = emptyList())
    val completedToday by habitsViewModel.completedToday.collectAsState()
    val completingHabitIds by habitsViewModel.completingHabitIds.collectAsState()
    val progressUiState by homeScreenViewModel.uiState.collectAsState()
    val dailyGoalProgress = if (progressUiState.dailyGoalXp > 0) {
        (progressUiState.dailyGoalProgressXp.toFloat() / progressUiState.dailyGoalXp.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Habits",
                        color = ColorPaletteHabits.Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ColorPaletteHabits.Violet
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("habitCreation") },
                containerColor = ColorPaletteHabits.Violet,
                shape = RoundedCornerShape(22.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                HabitsHeader(
                    streak = progressUiState.globalStreak,
                    dailyGoalProgress = dailyGoalProgress,
                    dailyGoalProgressXp = progressUiState.dailyGoalProgressXp,
                    dailyGoalXp = progressUiState.dailyGoalXp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                ProgressHeader(
                    state = ProgressHeaderState(
                        level = progressUiState.pet.level,
                        xp = progressUiState.pet.xp,
                        evolutionStage = progressUiState.pet.evolutionStage,
                        totalCoins = progressUiState.totalCoins,
                        globalStreak = progressUiState.globalStreak,
                        currentCombo = progressUiState.currentCombo,
                        lastHabitCompletionTimestamp = progressUiState.lastHabitCompletionTimestamp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (habits.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Your dragon is ready for its first tiny quest.",
                        message = "Create one small habit and give your dragon a simple win to celebrate.",
                        hint = "Start with something easy enough to finish today.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(
                items = habits,
                key = { habit -> habit.id }
            ) { habit ->
                HabitItem(
                    habit = habit,
                    completed = completedToday[habit.id] == true,
                    isCompleting = habit.id in completingHabitIds,
                    navController = navController,
                    onComplete = { habitsViewModel.completeCheckboxHabit(habit) },
                    onDelete = { habitsViewModel.deleteHabit(habit) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun HabitsHeader(
    streak: Int,
    dailyGoalProgress: Float,
    dailyGoalProgressXp: Long,
    dailyGoalXp: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tiny quests, big dragon growth.",
            style = MaterialTheme.typography.headlineSmall,
            color = ColorPaletteHabits.Ink,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Complete today's habits to feed the flame and keep your rhythm alive.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiniMetricCard(
                title = "Streak",
                value = "$streak days",
                accent = ColorPaletteHabits.Amber,
                modifier = Modifier.weight(1f)
            )
            MiniMetricCard(
                title = "Daily XP",
                value = "$dailyGoalProgressXp / $dailyGoalXp",
                accent = ColorPaletteHabits.Mint,
                modifier = Modifier.weight(1f)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorPaletteHabits.Card),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily goal", style = MaterialTheme.typography.titleMedium, color = ColorPaletteHabits.Ink)
                    Text(
                        text = "${(dailyGoalProgress * 100f).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorPaletteHabits.Violet
                    )
                }
                LinearProgressIndicator(
                    progress = dailyGoalProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    color = ColorPaletteHabits.Violet
                )
            }
        }
    }
}

@Composable
private fun MiniMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = ColorPaletteHabits.Card,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, color = accent)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitItem(
    habit: HabitEntity,
    completed: Boolean,
    isCompleting: Boolean,
    navController: NavHostController,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionsDialog by remember { mutableStateOf(false) }
    var skippedToday by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .combinedClickable(
                onClick = { navController.navigate("habitDetail/${habit.id}") },
                onLongClick = { showActionsDialog = true }
            ),
        shape = RoundedCornerShape(28.dp),
        color = if (completed) ColorPaletteHabits.MintSoft else ColorPaletteHabits.Card,
        shadowElevation = if (completed) 0.dp else 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompletionButton(
                completed = completed,
                skippedToday = skippedToday,
                isCompleting = isCompleting,
                onComplete = onComplete
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorPaletteHabits.Ink
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = habitIcon(habit),
                        contentDescription = null,
                        tint = ColorPaletteHabits.Violet,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${habit.type} • ${habit.currentStreak} day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (skippedToday) {
                    Text(
                        text = "Skipped for this session",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorPaletteHabits.Muted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            IconButton(
                onClick = { showActionsDialog = true },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Habit actions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    HabitActionsDialog(
        habit = habit,
        show = showActionsDialog,
        onDismiss = { showActionsDialog = false },
        onEdit = {
            showActionsDialog = false
            navController.navigate("habitEdit/${habit.id}")
        },
        onDelete = {
            showActionsDialog = false
            showDeleteDialog = true
        },
        onSkip = {
            skippedToday = true
            showActionsDialog = false
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to delete '${habit.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPaletteHabits.Danger)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CompletionButton(
    completed: Boolean,
    skippedToday: Boolean,
    isCompleting: Boolean,
    onComplete: () -> Unit
) {
    IconButton(
        onClick = onComplete,
        enabled = !completed && !skippedToday && !isCompleting,
        modifier = Modifier.size(44.dp)
    ) {
        if (isCompleting) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = ColorPaletteHabits.Violet
            )
        } else {
            Icon(
                imageVector = if (completed) Icons.Default.Star else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (completed) "Completed" else "Complete habit",
                tint = if (completed) ColorPaletteHabits.Amber else ColorPaletteHabits.Violet,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun HabitActionsDialog(
    habit: HabitEntity,
    show: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSkip: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(habit.name) },
        text = { Text("Choose what to do with this quest.") },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = ColorPaletteHabits.Danger)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = ColorPaletteHabits.Danger)
                }
                TextButton(onClick = onSkip) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp), tint = ColorPaletteHabits.Muted)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Skip", color = ColorPaletteHabits.Muted)
                }
            }
        }
    )
}

private fun habitIcon(habit: HabitEntity) = when {
    habit.type == "TIMER" -> Icons.Default.FavoriteBorder
    habit.icon.contains("star", ignoreCase = true) -> Icons.Default.Star
    habit.icon.contains("pet", ignoreCase = true) || habit.icon.contains("dragon", ignoreCase = true) -> Icons.Default.Pets
    else -> Icons.Default.RadioButtonUnchecked
}

private object ColorPaletteHabits {
    val Card = Color(0xFFFFFFFF)
    val MintSoft = Color(0xFFE8FBF2)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Mint = Color(0xFF4EDB95)
    val Danger = Color(0xFFFF6B6B)
    val Muted = Color(0xFF6F6A8A)
    val Ink = Color(0xFF302B4A)
}
