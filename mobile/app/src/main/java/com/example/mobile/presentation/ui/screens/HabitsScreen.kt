package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.repository.HabitRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.navigation.NavHostController

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {
    val habits = habitRepository.getAllHabits()

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Habits") })
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
    navController: NavHostController,
    onDelete: () -> Unit
) {
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
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Complete",
            tint = Color.Gray,
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
            onClick = onDelete
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
