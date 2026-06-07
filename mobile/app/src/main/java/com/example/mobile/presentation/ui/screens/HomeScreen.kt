package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.presentation.ui.components.AnimatedPet

@Composable
fun HomeScreen(
    onNavigateToHabits: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val pet = uiState.pet
    val nextLevelXp = xpRequiredForNextLevel(pet.level)
    val currentLevelXp = xpIntoCurrentLevel(pet.xp)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Animated pet display
            AnimatedPet(
                pet = pet,
                modifier = Modifier.size(280.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Coin balance display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { homeScreenViewModel.resetAllGameData() }
                ) {
                    Text("Reset Game Data")
                }
                Text(
                    text = "${uiState.totalCoins} Coins",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Coin balance",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${uiState.globalStreak} Day Streak", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Level ${pet.level} ${getEvolutionStageName(pet.evolutionStage)}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("XP: $currentLevelXp / $nextLevelXp", style = MaterialTheme.typography.bodyLarge)
            LinearProgressIndicator(
                progress = { (currentLevelXp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's Habits", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onNavigateToHabits) {
                    Text("Manage")
                }
            }

            if (uiState.habits.isEmpty()) {
                Button(onClick = onNavigateToHabits) {
                    Text("Create your first habit")
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    uiState.habits.forEach { habit ->
                        HomeHabitItem(
                            habit = habit,
                            completed = uiState.completedToday[habit.id] == true,
                            onClick = { onNavigateToHabitDetail(habit.id) }
                        )
                    }
                }
            }
        }

        // Reward overlay that appears above content
        /*RewardOverlay(
            onDismiss = {} // Empty lambda as dismissal is handled internally
        )*/
    }
}

@Composable
private fun HomeHabitItem(
    habit: HabitEntity,
    completed: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                if (completed) Color.Green.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (completed) "Completed" else "Not completed",
            tint = if (completed) Color.Green else Color.Red,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(habit.name)
            Text(
                text = "${habit.type} | Streak ${habit.currentStreak}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


private fun getEvolutionStageName(stage: Int): String = when (stage) {
    0 -> "Egg"
    1 -> "Hatchling"
    2 -> "Young Dragon"
    3 -> "Adult Dragon"
    4 -> "Ancient Dragon"
    else -> "Unknown"
}

private fun xpRequiredForNextLevel(level: Int): Long = 100L + (level * 50L)

private fun xpIntoCurrentLevel(totalXp: Long): Long {
    var level = 0
    var remainingXp = totalXp
    while (remainingXp >= xpRequiredForNextLevel(level)) {
        remainingXp -= xpRequiredForNextLevel(level)
        level++
    }
    return remainingXp
}
