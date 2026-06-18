package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ChallengeCard
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.ui.components.LoadingStateCard
import com.example.mobile.domain.repository.ChallengeUiState
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.presentation.viewmodel.HabitsViewModel

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
    val challengeUiState by homeScreenViewModel.challengeUiState.collectAsState()
    val sortedHabits = habits.sortedWith(
        compareBy<HabitEntity> { completedToday[it.id] == true }
            .thenBy { it.name.lowercase() }
    )

    Scaffold(
        containerColor = AppTheme.current.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("habitCreation") },
                containerColor = AppTheme.current.violet,
                contentColor = AppTheme.current.onPrimary,
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
                ChallengeCard(
                    state = challengeUiState,
                    onClaim = homeScreenViewModel::claimChallenge,
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
                    onEdit = { navController.navigate("habitEdit/${habit.id}") },
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HabitItem(
    habit: HabitEntity,
    completed: Boolean,
    isCompleting: Boolean,
    navController: NavHostController,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionsDialog by remember { mutableStateOf(false) }
    var skippedToday by remember { mutableStateOf(false) }
    var swipeOffset by remember { mutableStateOf(0f) }
    var showSwipeActions by remember { mutableStateOf(false) }
    val category = habitCategory(habit)
    val itemBackground = if (completed) AppTheme.current.mintSurfaceActive else AppTheme.current.card

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = { navController.navigate("habitDetail/${habit.id}") },
                onLongClick = { showActionsDialog = true }
            )
            .pointerInput(habit.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val nextOffset = (swipeOffset + dragAmount.x).coerceIn(-120f, 80f)
                        swipeOffset = nextOffset
                        showSwipeActions = nextOffset < -48f
                    },
                    onDragEnd = {
                        showSwipeActions = swipeOffset < -48f
                        swipeOffset = 0f
                    },
                    onDragCancel = {
                        showSwipeActions = false
                        swipeOffset = 0f
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        color = itemBackground,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AppTheme.current.muted.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (skippedToday) {
                        Text(
                            text = "Skipped for this session",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.current.danger
                        )
                    }
                }

                StreakBadge(streak = habit.currentStreak, activeToday = completed)
            }

            if (showSwipeActions) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    SwipeActionsOverlay(
                        onEdit = {
                            showSwipeActions = false
                            onEdit()
                        },
                        onDelete = {
                            showSwipeActions = false
                            showDeleteDialog = true
                        }
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
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.current.danger)
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
private fun SwipeActionsOverlay(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(top = 6.dp, end = 20.dp),
        shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        color = AppTheme.current.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            SwipeActionButton(
                icon = Icons.Default.Edit,
                color = AppTheme.current.violet,
                onClick = onEdit
            )
            SwipeActionButton(
                icon = Icons.Default.Delete,
                color = AppTheme.current.danger,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun SwipeActionButton(
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StreakBadge(streak: Int, activeToday: Boolean) {
    val badgeTint = if (activeToday) AppTheme.current.amber else AppTheme.current.muted
    val badgeText = if (activeToday) AppTheme.current.amberDark else AppTheme.current.muted.copy(alpha = 0.78f)
    val badgeSurface = if (activeToday) {
        AppTheme.current.amberSoft.copy(alpha = 0.6f)
    } else {
        AppTheme.current.card.copy(alpha = 0.55f)
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = badgeSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = badgeTint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "${streak}d streak",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = badgeText
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
    val tintColor = if (completed) AppTheme.current.success else AppTheme.current.violet
    val ambientBg = if (completed) AppTheme.current.success.copy(alpha = 0.12f) else AppTheme.current.violet.copy(alpha = 0.08f)

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
                color = AppTheme.current.violet
            )
        } else {
            Icon(
                imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (completed) AppTheme.current.success else AppTheme.current.violet.copy(alpha = 0.6f),
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
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppTheme.current.danger)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = AppTheme.current.danger)
                }
                TextButton(onClick = onSkip) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppTheme.current.muted)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Skip", color = AppTheme.current.muted)
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
