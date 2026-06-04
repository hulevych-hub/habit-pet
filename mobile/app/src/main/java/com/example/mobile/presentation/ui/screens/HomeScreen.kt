package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(homeScreenViewModel: HomeScreenViewModel = hiltViewModel()) {
    val uiState by homeScreenViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Pet Scene (placeholder)
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.LightGray, CircleShape)
        ) {
            Text("Pet Scene", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Streak
        Text("🔥 ${uiState.globalStreak} Day Streak", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // Pet Info
        Text("Level 0 Egg", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(4.dp))

        // XP Progress
        Text("XP: 0 / 100", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Today's Habits
        Text("Today's Habits:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // For now, showing static habits - in a real implementation, we'd iterate through uiState.habits
            HabitItem("Skincare", completed = true)
            HabitItem("Reading", completed = true)
            HabitItem("Workout", completed = false)
        }
    }
}

@Composable
private fun HabitItem(name: String, completed: Boolean) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(
                if (completed) Color.Green.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (completed) "Completed" else "Not completed",
            tint = if (completed) Color.Green else Color.Red,
            modifier = Modifier.size(24.dp)
        )
        Text(name)
    }
}
