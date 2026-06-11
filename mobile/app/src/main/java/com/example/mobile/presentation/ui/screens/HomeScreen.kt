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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.DragonMood
import com.example.mobile.presentation.ui.components.AnimatedPet
import com.example.mobile.presentation.ui.components.EvolutionTeaser
import com.example.mobile.presentation.ui.components.ProgressHeader
import com.example.mobile.presentation.ui.components.ProgressHeaderState

@Composable
fun HomeScreen(
    onNavigateToHabits: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val pet = uiState.pet
    val shouldRequestPetName = pet.id == 0L || pet.name.trim().isEmpty()
    var showMandatoryPetNameDialog by remember { mutableStateOf(false) }
    var petNameDraft by remember { mutableStateOf(pet.name) }

    LaunchedEffect(shouldRequestPetName) {
        showMandatoryPetNameDialog = shouldRequestPetName
    }

    LaunchedEffect(pet.name) {
        petNameDraft = pet.name
    }

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
            Text(
                text = "Meet ${pet.name}",
                style = MaterialTheme.typography.titleLarge
            )
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
            ProgressHeader(
                state = ProgressHeaderState(
                    level = pet.level,
                    xp = pet.xp,
                    evolutionStage = pet.evolutionStage,
                    totalCoins = uiState.totalCoins,
                    globalStreak = uiState.globalStreak,
                    lastStreakDate = uiState.lastStreakDate,
                    currentCombo = uiState.currentCombo,
                    lastHabitCompletionTimestamp = uiState.lastHabitCompletionTimestamp
                )
            )
            DailyGoalCard(
                goalXp = uiState.dailyGoalXp.toLong(),
                progressXp = uiState.dailyGoalProgressXp,
                completed = uiState.dailyGoalCompleted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            EvolutionTeaser(
                totalXp = pet.xp,
                currentStage = pet.evolutionStage,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mood: ${DragonMood.from(pet.mood).displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your dragon is ready for its first tiny quest.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(onClick = onNavigateToHabits) {
                        Text("Create your first habit")
                    }
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

        if (showMandatoryPetNameDialog) {
            PetRenameDialog(
                initialName = petNameDraft,
                helperText = "You can rename your dragon later.",
                allowDismiss = false,
                onDismissRequest = { showMandatoryPetNameDialog = false },
                onConfirm = { newName ->
                    homeScreenViewModel.renamePet(newName.trim())
                    showMandatoryPetNameDialog = false
                }
            )
        }
    }
}

@Composable
private fun DailyGoalCard(
    goalXp: Long,
    progressXp: Long,
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = (progressXp.toFloat() / goalXp.toFloat()).coerceIn(0f, 1f)
    val title = if (completed) "Daily goal complete" else "Daily XP goal"
    val message = if (completed) {
        "Your dragon is glowing from today's steady rhythm."
    } else {
        "${progressXp.toInt()} / ${goalXp.toInt()} XP · three checkbox habits usually finish this."
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (completed) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (completed) "Protected" else "Growing",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (completed) Color(0xFF43A047) else MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = if (completed) Color(0xFFFFB74D) else MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
