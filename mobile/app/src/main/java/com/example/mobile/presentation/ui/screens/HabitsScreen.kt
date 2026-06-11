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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.navigation.NavHostController
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
            item {
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
                    modifier = Modifier.padding(8.dp)
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

            items(habits) { habit ->
                HabitItem(
                    habit = habit,
                    completed = completedToday[habit.id] == true,
                    isCompleting = habit.id in completingHabitIds,
                    navController = navController,
                    onComplete = { habitsViewModel.completeCheckboxHabit(habit) },
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
    isCompleting: Boolean,
    navController: NavHostController,
    onComplete: () -> Unit,
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
        IconButton(
            onClick = onComplete,
            enabled = !completed && habit.type == "CHECKBOX" && !isCompleting,
            modifier = Modifier.size(40.dp)
        ) {
            if (isCompleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                    contentDescription = if (completed) "Completed" else "Complete habit",
                    tint = if (completed) Color.Green else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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
