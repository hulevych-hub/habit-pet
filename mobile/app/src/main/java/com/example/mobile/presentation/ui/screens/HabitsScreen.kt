package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrunchDining
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.viewmodel.HabitsViewModel
import kotlin.math.ceil

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HabitsScreen(
    navController: NavHostController,
    habitsViewModel: HabitsViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val habits by habitsViewModel.habits.collectAsState(initial = emptyList())
    val completedToday by habitsViewModel.completedToday.collectAsState(initial = emptyMap())
    val completingHabitIds by habitsViewModel.completingHabitIds.collectAsState(initial = emptySet())
    val progressUiState by homeScreenViewModel.uiState.collectAsState()
    val dailyGoalProgress = if (progressUiState.dailyGoalXp > 0) {
        (progressUiState.dailyGoalProgressXp.toFloat() / progressUiState.dailyGoalXp.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val dailyGoalSegments = if (dailyGoalProgress <= 0f) {
        0
    } else {
        ceil(dailyGoalProgress * 3f).toInt().coerceIn(1, 3)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HabitsHeader(
                    streak = progressUiState.globalStreak,
                    dailyGoalProgress = dailyGoalProgress,
                    dailyGoalSegments = dailyGoalSegments,
                    dailyGoalProgressXp = progressUiState.dailyGoalProgressXp,
                    dailyGoalXp = progressUiState.dailyGoalXp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
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
                Spacer(modifier = Modifier.height(112.dp))
            }
        }
    }
}

@Composable
private fun HabitsHeader(
    streak: Int,
    dailyGoalProgress: Float,
    dailyGoalSegments: Int,
    dailyGoalProgressXp: Long,
    dailyGoalXp: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ColorPaletteHabits.HeaderStart,
                            ColorPaletteHabits.HeaderEnd
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = ColorPaletteHabits.Amber,
                        modifier = Modifier.size(34.dp)
                    )
                    Column {
                        Text(
                            text = "Current Streak: $streak Days",
                            style = MaterialTheme.typography.titleLarge,
                            color = ColorPaletteHabits.Amber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rhythm is visibility.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ColorPaletteHabits.Muted
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = ColorPaletteHabits.Amber,
                    modifier = Modifier.size(42.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Goal: $dailyGoalProgressXp / $dailyGoalXp XP to Bonus Chest",
                        style = MaterialTheme.typography.titleMedium,
                        color = ColorPaletteHabits.Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = ColorPaletteHabits.Amber,
                        modifier = Modifier.size(30.dp)
                    )
                }
                LinearProgressIndicator(
                    progress = { dailyGoalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = ColorPaletteHabits.Amber,
                    trackColor = ColorPaletteHabits.ProgressTrack
                )
                Text(
                    text = "$dailyGoalSegments/3",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorPaletteHabits.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
    val category = habitCategory(habit)
    val cardColors = habitCardColors(habit, completed)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = { navController.navigate("habitDetail/${habit.id}") },
                onLongClick = { showActionsDialog = true }
            ),
        shape = RoundedCornerShape(28.dp),
        color = cardColors.background,
        shadowElevation = 2.dp
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

            IconBadge(
                icon = habitIcon(habit),
                tint = cardColors.iconTint,
                backgroundColor = cardColors.iconBackground
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = habitTitle(habit, category),
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorPaletteHabits.Ink,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = cardColors.categoryTint
                )
                if (skippedToday) {
                    Text(
                        text = "Skipped for this session",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorPaletteHabits.Muted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StreakBadge(streak = habit.currentStreak)
                IconButton(
                    onClick = { navController.navigate("habitEdit/${habit.id}") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit habit",
                        tint = ColorPaletteHabits.Violet,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = {
                        showActionsDialog = false
                        showDeleteDialog = true
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete habit",
                        tint = ColorPaletteHabits.Danger,
                        modifier = Modifier.size(22.dp)
                    )
                }
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
private fun StreakBadge(streak: Int) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = ColorPaletteHabits.AmberSoft
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = ColorPaletteHabits.Amber,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "${streak}d streak",
                style = MaterialTheme.typography.labelMedium,
                color = ColorPaletteHabits.AmberDark
            )
        }
    }
}

@Composable
private fun IconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    backgroundColor: Color
) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = 0.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(28.dp)
                .padding(10.dp)
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
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp,
                color = ColorPaletteHabits.Violet
            )
        } else {
            Icon(
                imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (completed) "Completed" else "Complete habit",
                tint = if (completed) ColorPaletteHabits.Success else ColorPaletteHabits.Violet,
                modifier = Modifier.size(34.dp)
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

private fun habitTitle(habit: HabitEntity, category: String): String {
    return if (category == "CATEGORY") habit.name else "${habit.name} | $category"
}

private fun habitCategory(habit: HabitEntity): String {
    val name = habit.name.lowercase()
    val icon = habit.icon.lowercase()

    return when {
        icon.contains("\uD83D\uDCAA") || icon.contains("\uD83D\uDEB4") || name.contains("exercise") || name.contains("workout") || name.contains("gym") || name.contains("run") -> "EXERCISE"
        icon.contains("\uD83D\uDCDA") || name.contains("read") || name.contains("book") || name.contains("chapter") -> "READ"
        icon.contains("\uD83C\uDF4E") || name.contains("breakfast") || name.contains("food") || name.contains("health") -> "HEALTH"
        icon.contains("\uD83E\uDDD8") || name.contains("meditation") || name.contains("mindful") -> "MINDFULNESS"
        icon.contains("\uD83D\uDCDD") || name.contains("journal") || name.contains("write") || name.contains("note") -> "JOURNAL"
        icon.contains("\uD83C\uDFAF") || name.contains("focus") || name.contains("target") -> "FOCUS"
        icon.contains("\u2764") || name.contains("care") || name.contains("love") -> "CARE"
        else -> "CATEGORY"
    }
}

private fun habitIcon(habit: HabitEntity) = when (habitCategory(habit)) {
    "EXERCISE" -> Icons.Default.FitnessCenter
    "READ" -> Icons.AutoMirrored.Filled.MenuBook
    "HEALTH" -> Icons.Default.BrunchDining
    "MINDFULNESS" -> Icons.Default.FavoriteBorder
    "JOURNAL" -> Icons.Default.Star
    "FOCUS" -> Icons.Default.RadioButtonUnchecked
    "CARE" -> Icons.Default.Pets
    else -> Icons.Default.Pets
}

private fun habitCardColors(habit: HabitEntity, completed: Boolean): HabitCardColors {
    if (completed) {
        return HabitCardColors(
            background = ColorPaletteHabits.MintSoft,
            iconBackground = ColorPaletteHabits.SuccessSoft,
            iconTint = ColorPaletteHabits.Success,
            categoryTint = ColorPaletteHabits.SuccessDark
        )
    }

    return when (habitCategory(habit)) {
        "EXERCISE" -> HabitCardColors(
            background = ColorPaletteHabits.MintSoft,
            iconBackground = ColorPaletteHabits.SuccessSoft,
            iconTint = ColorPaletteHabits.Success,
            categoryTint = ColorPaletteHabits.SuccessDark
        )
        "READ" -> HabitCardColors(
            background = ColorPaletteHabits.BlueSoft,
            iconBackground = ColorPaletteHabits.BlueIcon,
            iconTint = ColorPaletteHabits.Blue,
            categoryTint = ColorPaletteHabits.BlueDark
        )
        "HEALTH" -> HabitCardColors(
            background = ColorPaletteHabits.AmberSoft,
            iconBackground = ColorPaletteHabits.AmberIcon,
            iconTint = ColorPaletteHabits.Amber,
            categoryTint = ColorPaletteHabits.AmberDark
        )
        else -> HabitCardColors(
            background = ColorPaletteHabits.PurpleSoft,
            iconBackground = ColorPaletteHabits.PurpleIcon,
            iconTint = ColorPaletteHabits.Violet,
            categoryTint = ColorPaletteHabits.Muted
        )
    }
}

private data class HabitCardColors(
    val background: Color,
    val iconBackground: Color,
    val iconTint: Color,
    val categoryTint: Color
)

private object ColorPaletteHabits {
    val HeaderStart = Color(0xFFF2EAFF)
    val HeaderEnd = Color(0xFFF8F0FF)
    val ProgressTrack = Color(0xFFE6D9FF)
    val PurpleSoft = Color(0xFFF6F0FF)
    val PurpleIcon = Color(0xFFEDE4FF)
    val MintSoft = Color(0xFFE8FBF2)
    val SuccessSoft = Color(0xFFDFF8EA)
    val BlueSoft = Color(0xFFEAF4FF)
    val BlueIcon = Color(0xFFE0ECFF)
    val AmberSoft = Color(0xFFFFF4E4)
    val AmberIcon = Color(0xFFFFEAC2)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Success = Color(0xFF4EDB95)
    val SuccessDark = Color(0xFF2F9D68)
    val Blue = Color(0xFF4BA3FF)
    val BlueDark = Color(0xFF2F6FAE)
    val AmberDark = Color(0xFFB97820)
    val Danger = Color(0xFFFF6B6B)
    val Muted = Color(0xFF6F6A8A)
    val Ink = Color(0xFF302B4A)
}
