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
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.presentation.viewmodel.HabitDetailViewModel
import com.example.mobile.ui.theme.HabitPetTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
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

    HabitPetTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Habit Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            SingleLineSpecifiedPadding(padding) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HabitDetailContent(
                        viewModel = viewModel,
                        habitId = habitId
                    )
                }
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
        EmptyState("Habit not found")
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Habit Header
            HabitHeader(habit = currentHabit)

            // Completion Status
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

            // Divider
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))

            // Completion History
            Text("Completion History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (completions.isEmpty()) {
                Text("No completions yet", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            } else {
                CompletionHistoryList(completions = completions)
            }
        }
    }
}

@Composable
private fun HabitHeader(habit: HabitEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Text(
            text = habit.icon,
            fontSize = 48.sp,
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Type: ${habit.type}",
                style = MaterialTheme.typography.bodyLarge
            )
            if (habit.type == "TIMER") {
                Text(
                    text = "Minimum duration: ${habit.minimumDurationMinutes} minutes",
                    style = MaterialTheme.typography.bodyMedium
                )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Today's Progress",
            style = MaterialTheme.typography.titleMedium
        )

        if (isCompletedToday) {
            // Show completed status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Completed ✓",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Green
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color.Green,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Show habit type specific completion UI
            when (habit.type) {
                "CHECKBOX" -> CheckboxCompletion(
                    onComplete = onCompleteCheckbox
                )
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

@Composable
private fun CheckboxCompletion(
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Not completed ○",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red
        )
        Button(
            onClick = onComplete,
            modifier = Modifier.height(40.dp)
        ) {
            Text("Complete Habit")
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Timer display
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        Text(
            text = timeString,
            style = MaterialTheme.typography.displaySmall,
            color = if (isTimerRunning) Color.Green else Color.Gray
        )

        // Minimum duration info
        Text(
            text = "Minimum: ${habit.minimumDurationMinutes} min",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        // Button controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isTimerRunning) {
                Button(
                    onClick = onStartTimer,
                    enabled = true,
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("Start")
                }
            } else {
                Button(
                    onClick = onStopTimer,
                    enabled = true,
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("Stop")
                }
                Button(
                    onClick = onResetTimer,
                    enabled = true,
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("Reset")
                }
            }
        }

        // Show completion button if timer has reached minimum duration
        if (isTimerRunning && (elapsedSeconds / 60) >= habit.minimumDurationMinutes) {
            Button(
                onClick = onStopTimer,
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Text("Complete", color = Color.White)
            }
        }
    }
}

@Composable
private fun CompletionHistoryList(completions: List<HabitCompletionEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(completions) { completion ->
            CompletionHistoryItem(completion = completion)
        }
    }
}

@Composable
private fun CompletionHistoryItem(completion: HabitCompletionEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date
        Column {
            Text(
                text = formatDate(completion.date),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "+${completion.xpEarned} XP",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Green
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Icon
        Text(
            text = "✓",
            fontSize = 24.sp,
            color = Color.Green,
            modifier = Modifier.size(32.dp)
                .background(
                    Color.Green.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(date)
}

@Composable
private fun CenteredProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SingleLineSpecifiedPadding(
    innerPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        content()
    }
}
