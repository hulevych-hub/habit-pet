package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.ui.components.LoadingStateCard
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
    val error by habitsViewModel.error.collectAsState(initial = null)
    val isLoading by homeScreenViewModel.isLoading.collectAsState()
    val progressUiState by homeScreenViewModel.uiState.collectAsState()
    val sortedHabits = habits.sortedWith(
        compareBy<HabitEntity> { completedToday[it.id] == true }
            .thenBy { it.name.lowercase() }
    )

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
        containerColor = Color(0xFFFAFAFC), // Alabaster Premium Background matching home
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFAFAFC)
                ),
                title = {
                    Text(
                        text = "Habits",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
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
                contentColor = Color.White,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add habit", modifier = Modifier.size(26.dp))
            }
        }
    ) { padding ->
        if (!error.isNullOrBlank()) {
            ErrorStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                message = error.orEmpty(),
                onRetry = habitsViewModel::clearError
            )
        } else if (isLoading) {
            LoadingStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                message = "Gathering today's quests..."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
            item {
                HabitsHeader(
                    streak = progressUiState.globalStreak,
                    dailyGoalProgress = dailyGoalProgress,
                    dailyGoalSegments = dailyGoalSegments,
                    dailyGoalProgressXp = progressUiState.dailyGoalProgressXp,
                    dailyGoalXp = progressUiState.dailyGoalXp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            if (sortedHabits.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Your dragon is ready for its first tiny quest.",
                        message = "Create one small habit and give your dragon a simple win to celebrate.",
                        hint = "Start with something easy enough to finish today.",
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            items(
                items = sortedHabits,
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
                Spacer(modifier = Modifier.height(120.dp))
            }
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
        colors = CardDefaults.cardColors(containerColor = ColorPaletteHabits.HeaderCardBackground),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Modern flame container without rough boundaries
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ColorPaletteHabits.AmberSoft, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = ColorPaletteHabits.Amber,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Current Streak: $streak Days",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorPaletteHabits.AmberDark
                        )
                        Text(
                            text = "Rhythm is visibility.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ColorPaletteHabits.Muted
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Daily Goal: $dailyGoalProgressXp / $dailyGoalXp XP to Bonus Chest",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ColorPaletteHabits.Ink
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { dailyGoalProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp),
                        color = ColorPaletteHabits.Violet,
                        trackColor = ColorPaletteHabits.ProgressTrack
                    )

                    // Chest container asset showcase right next to progress indicator track
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Bonus Chest",
                        tint = ColorPaletteHabits.Amber,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "$dailyGoalSegments / 3 Tasks Done",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = ColorPaletteHabits.Muted,
                    textAlign = TextAlign.Start,
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

    // Dynamic matching of the card colors based on checked states
    val itemBackground = if (completed) ColorPaletteHabits.MintSurfaceActive else ColorPaletteHabits.DefaultItemCardBackground

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = { navController.navigate("habitDetail/${habit.id}") },
                onLongClick = { showActionsDialog = true }
            ),
        shape = RoundedCornerShape(24.dp),
        color = itemBackground,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CompletionButton(
                completed = completed,
                skippedToday = skippedToday,
                isCompleting = isCompleting,
                onComplete = onComplete
            )

            IconBadge(
                icon = habit.icon,
                completed = completed,
                fallbackIcon = habitIcon(habit)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteHabits.Ink
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = ColorPaletteHabits.Muted.copy(alpha = 0.7f)
                )
                if (skippedToday) {
                    Text(
                        text = "Skipped for this session",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorPaletteHabits.Danger
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StreakBadge(streak = habit.currentStreak)

                IconButton(
                    onClick = { navController.navigate("habitEdit/${habit.id}") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit habit",
                        tint = ColorPaletteHabits.Violet.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete habit",
                        tint = ColorPaletteHabits.Danger.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
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
        color = ColorPaletteHabits.AmberSoft.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = ColorPaletteHabits.Amber,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "${streak}d streak",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = ColorPaletteHabits.AmberDark
            )
        }
    }
}

@Composable
private fun IconBadge(
    icon: String,
    completed: Boolean,
    fallbackIcon: ImageVector
) {
    val tintColor = if (completed) ColorPaletteHabits.Success else ColorPaletteHabits.Violet
    val ambientBg = if (completed) ColorPaletteHabits.Success.copy(alpha = 0.12f) else ColorPaletteHabits.Violet.copy(alpha = 0.08f)

    Surface(
        modifier = Modifier.size(46.dp),
        shape = RoundedCornerShape(14.dp),
        color = ambientBg
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (icon.isNotBlank()) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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
        modifier = Modifier.size(40.dp)
    ) {
        if (isCompleting) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = ColorPaletteHabits.Violet
            )
        } else {
            Icon(
                imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (completed) ColorPaletteHabits.Success else ColorPaletteHabits.Violet.copy(alpha = 0.6f),
                modifier = Modifier.size(30.dp)
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

private object ColorPaletteHabits {
    val HeaderCardBackground = Color(0xFFEBE5FC) // Creamy lavender header block
    val DefaultItemCardBackground = Color(0xFFFFFFFF) // Clean pristine card base
    val MintSurfaceActive = Color(0xFFD7F5E6) // Smooth Mint Green active feedback
    val ProgressTrack = Color(0xFFDDD5F3)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val AmberSoft = Color(0xFFFFF2DC)
    val AmberDark = Color(0xFFB57416)
    val Success = Color(0xFF2EA366)
    val Danger = Color(0xFFFF6B6B)
    val Muted = Color(0xFF635E7A)
    val Ink = Color(0xFF1B172E)
}