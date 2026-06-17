package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.viewmodel.HabitDetailViewModel
import com.example.mobile.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
fun HabitDetailScreen(
    habitId: Long,
    viewModel: HabitDetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    LaunchedEffect(habitId) {
        viewModel.initialize(habitId)
    }
    LaunchedEffect(viewModel) {
        viewModel.navigateBack.collect {
            onNavigateUp()
        }
    }

    Scaffold(
        containerColor = AppTheme.current.background
    ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    InlineScreenHeader(
                        title = "Quest Chronicles",
                        onNavigateUp = onNavigateUp,
                        accentColor = AppTheme.current.primary
                    )
                    HabitDetailContent(
                        viewModel = viewModel,
                        habitId = habitId
                    )
                }
            }
        }
    }

@Composable
private fun HabitDetailContent(
    viewModel: HabitDetailViewModel,
    habitId: Long
) {
    val habit by viewModel.habit.collectAsState(initial = null)
    val completions by viewModel.completions.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val error by viewModel.error.collectAsState(initial = null)
    val isCompletedToday by viewModel.isCompletedToday(habitId).collectAsState(initial = false)
    val isTimerRunning by viewModel.isTimerRunning.collectAsState(initial = false)
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState(initial = 0)

    val currentError = error
    val currentHabit = habit

    if (isLoading) {
        CenteredProgressIndicator()
    } else if (currentError != null) {
        ErrorMessage(currentError)
    } else if (currentHabit == null) {
        Box(modifier = Modifier.padding(20.dp)) {
            EmptyStateCard(
                title = "This habit has gone quiet",
                message = "This path is no longer available, but your dragon is ready for the next small win.",
                hint = "Return to your habit list and choose a quest that still belongs to you.",
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            item {
                HabitHeader(habit = currentHabit)
            }

            item {
                CompletionStatus(
                    habit = currentHabit,
                    isCompletedToday = isCompletedToday,
                    isTimerRunning = isTimerRunning,
                    elapsedSeconds = elapsedSeconds,
                    onStartTimer = { viewModel.startTimerHabit(habitId) },
                    onStopTimer = { viewModel.stopTimerHabit(habitId) },
                    onCompleteCheckbox = { viewModel.completeCheckboxHabit(habitId) },
                    onResetTimer = { viewModel.resetTimer() }
                )
            }

            item {
                Text(
                    text = "Chronicle Ledger History",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = AppTheme.current.muted,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (completions.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "First chapter is unwritten",
                        message = "Complete it once and the story of your consistency will begin here.",
                        hint = "One completion today is enough to start the memory trail.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(
                    items = completions,
                    key = { it.id }
                ) { completion ->
                    CompletionHistoryItem(completion = completion)
                }
            }
        }
    }
}

@Composable
private fun HabitHeader(habit: HabitEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.primary),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AppTheme.current.primary, AppTheme.current.primaryContainer)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(AppTheme.current.onPrimary.copy(alpha = 0.12f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = habit.icon,
                        fontSize = 32.sp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.onPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppTheme.current.primaryContainer
                        ) {
                            Text(
                                text = habit.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                                color = AppTheme.current.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (habit.type == "TIMER") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AppTheme.current.surfaceVariant
                            ) {
                                Text(
                                    text = "🎯 Target: ${habit.minimumDurationMinutes}m",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = AppTheme.current.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionStatus(
    habit: HabitEntity,
    isCompletedToday: Boolean,
    isTimerRunning: Boolean,
    elapsedSeconds: Int,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onCompleteCheckbox: () -> Unit,
    onResetTimer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompletedToday) AppTheme.current.success.copy(alpha = 0.03f) else AppTheme.current.card
        ),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isCompletedToday) AppTheme.current.success.copy(alpha = 0.2f) else AppTheme.current.outline.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Today's Quest Objective",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.current.ink
            )

            if (isCompletedToday) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.current.success.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = AppTheme.current.success,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Objective Secured!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.current.success
                        )
                        Text(
                            text = "Your companion has stored this core memory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.current.muted
                        )
                    }
                }
            } else {
                when (habit.type) {
                    "CHECKBOX" -> CheckboxCompletion(onComplete = onCompleteCheckbox)
                    "TIMER" -> TimerCompletion(
                        habit = habit,
                        isTimerRunning = isTimerRunning,
                        elapsedSeconds = elapsedSeconds,
                        onStartTimer = onStartTimer,
                        onStopTimer = onStopTimer,
                        onResetTimer = onResetTimer
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckboxCompletion(onComplete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Pending actions remain...",
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.current.muted
        )
        Button(
            onClick = onComplete,
            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.current.primary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.height(44.dp)
        ) {
            Text("Complete Quest", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun TimerCompletion(
    habit: HabitEntity,
    isTimerRunning: Boolean,
    elapsedSeconds: Int,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onResetTimer: () -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    val hasMetMinimum = (elapsedSeconds / 60) >= habit.minimumDurationMinutes

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.current.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = if (isTimerRunning) AppTheme.current.success else AppTheme.current.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = AppTheme.current.muted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Required: ${habit.minimumDurationMinutes}:00",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.current.muted
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isTimerRunning) {
                Button(
                    onClick = onStartTimer,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.current.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Focus Run", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                Button(
                    onClick = onStopTimer,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.current.ink),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pause", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onResetTimer,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.current.muted),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.current.outline),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
            }
        }

        if (isTimerRunning && hasMetMinimum) {
            Button(
                onClick = onStopTimer,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.current.success),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Secure & Lock Rewards", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun CompletionHistoryItem(completion: HabitCompletionEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(AppTheme.current.success.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(8.dp).background(AppTheme.current.success, CircleShape))
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(42.dp)
                    .background(AppTheme.current.outline)
            )
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.current.outline.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = formatDate(completion.date),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.ink
                    )
                    Text(
                        text = "Synchronized Journey Event",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.current.muted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AppTheme.current.success.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "+${completion.xpEarned} XP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.success,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    return formatter.format(date)
}

@Composable
private fun CenteredProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.current.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AppTheme.current.primary)
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = AppTheme.current.danger, textAlign = TextAlign.Center)
    }
}

@Composable
private fun InlineScreenHeader(
    title: String,
    onNavigateUp: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateUp) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = accentColor
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.current.ink
        )
    }
}
