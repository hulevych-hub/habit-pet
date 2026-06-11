package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val streakEngine: StreakEngine
) : ViewModel() {
    val habits = habitRepository.getAllHabits()

    val completedToday: StateFlow<Map<Long, Boolean>> = habitRepository.getAllHabits()
        .flatMapLatest { habits ->
            val today = getDayStart(System.currentTimeMillis())

            if (habits.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    habits.map { habit ->
                        habitCompletionRepository.getCompletionForHabitOnDate(habit.id, today)
                            .map { completion -> habit.id to (completion != null) }
                    }
                ) { pairs ->
                    pairs.toList().toMap()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
            streakEngine.recalculateTodayStreak(System.currentTimeMillis())
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HabitsScreen(
    navController: NavHostController,
    habitsViewModel: HabitsViewModel = hiltViewModel()
) {
    val habits by habitsViewModel.habits.collectAsState(initial = emptyList())
    val completedToday by habitsViewModel.completedToday.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Habits") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("habitCreation") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(habits) { habit ->
                HabitItem(
                    habit = habit,
                    completed = completedToday[habit.id] == true,
                    navController = navController,
                    onDelete = { habitsViewModel.deleteHabit(habit) }
                )
            }
        }
    }
}

@Composable
private fun HabitItem(
    habit: HabitEntity,
    completed: Boolean,
    navController: NavHostController,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Make the entire row clickable for habit details
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("habitDetail/${habit.id}")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (completed) "Completed" else "Not completed",
            tint = if (completed) Color.Green else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(habit.name)
            Text("Type: ${habit.type}", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                navController.navigate("habitEdit/${habit.id}")
            }
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(
            onClick = {
                showDeleteDialog = true
            }
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Delete Habit")
            },
            text = {
                Text("Are you sure you want to delete '${habit.name}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getDayStart(time: Long): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = time
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
