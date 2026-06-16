package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.DragonMood
import com.example.mobile.domain.ExpConfig
import com.example.mobile.presentation.ui.components.AnimatedPet
import com.example.mobile.presentation.ui.components.CoinIcon
import com.example.mobile.presentation.ui.components.LoadingStateCard

@Composable
fun HomeScreen(
    onNavigateToHabits: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit,
    onNavigateToRewardsLocked: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val isLoading by homeScreenViewModel.isLoading.collectAsState()
    val pet = uiState.pet
    val shouldRequestPetName = pet.id == 0L || pet.name.trim().isEmpty()
    var showMandatoryPetNameDialog by remember { mutableStateOf(false) }
    var showResetGameDialog by remember { mutableStateOf(false) }
    var petNameDraft by remember { mutableStateOf(pet.name) }

    LaunchedEffect(shouldRequestPetName) { showMandatoryPetNameDialog = shouldRequestPetName }
    LaunchedEffect(pet.name) { petNameDraft = pet.name }

    Scaffold(
        containerColor = Color(0xFFFAFAFC),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GamifiedFixedHeader(
                streak = uiState.globalStreak,
                coins = uiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(pet.evolutionStage),
                streakCompletedToday = uiState.globalStreakCompletedToday,
                onCoinsClick = onNavigateToRewardsLocked
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                LoadingStateCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    message = "Waking up your dragon..."
                )
            } else {
                PetSummary(pet = pet)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    NextUnlockText()
                    ResetGameButton(onResetClick = { showResetGameDialog = true })
                    TodayNourishmentSection(
                        habits = uiState.habits,
                        completedTodayXp = uiState.completedTodayXp,
                        onNavigateToHabits = onNavigateToHabits,
                        onNavigateToHabitDetail = onNavigateToHabitDetail
                    )
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

        if (showResetGameDialog) {
            AlertDialog(
                onDismissRequest = { showResetGameDialog = false },
                title = { Text("Reset Game Data?") },
                text = {
                    Text("This clears habits, completions, rewards, pet progress, achievements, and statistics. This cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetGameDialog = false
                            homeScreenViewModel.resetAllGameData()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetGameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun PetSummary(pet: PetEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ColorPaletteHome.Card,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ColorPaletteHome.LavenderSoft.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedPet(
                    pet = pet,
                    modifier = Modifier.fillMaxSize(),
                    showNameOverlay = false
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pet.name.ifBlank { "Baby Dragon" },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteHome.Ink,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ColorPaletteHome.Amber
                    ) {
                        Text(
                            text = "Lv. ${pet.level}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ColorPaletteHome.Mint.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = DragonMood.from(pet.mood).displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorPaletteHome.Green,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GamifiedFixedHeader(
    streak: Int,
    coins: Int,
    stageName: String,
    streakCompletedToday: Boolean,
    onCoinsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
        val streakTint = if (streakCompletedToday) ColorPaletteHome.Honey else Color(0xFFA9A3B8)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = streakTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$streak d",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteHome.Ink
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ColorPaletteHome.Violet.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = ColorPaletteHome.Violet,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stageName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteHome.Violet
                    )
                }
            }

            Row(
                modifier = Modifier.clickable(onClick = onCoinsClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CoinIcon(
                    modifier = Modifier.size(22.dp),
                    tint = ColorPaletteHome.Amber
                )
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteHome.Ink
                )
            }
        }
    }
}


@Composable
private fun ResetGameButton(
    onResetClick: () -> Unit
) {
    Button(
        onClick = onResetClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF6B6B),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = "Reset Game",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun TodayNourishmentSection(
    habits: List<HabitEntity>,
    completedTodayXp: Map<Long, Long>,
    onNavigateToHabits: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit
) {
    val sortedHabits = habits.sortedWith(
        compareBy<HabitEntity> { completedTodayXp.containsKey(it.id) }
            .thenBy { it.name.lowercase() }
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Quest",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ColorPaletteHome.Ink
            )
            Text(
                text = "Manage",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = ColorPaletteHome.Violet,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { onNavigateToHabits() }
            )
        }

        if (sortedHabits.isEmpty()) {
            EmptyHomeQuest(onNavigateToHabits)
        } else {
            sortedHabits.forEach { habit ->
                HomeHabitItem(
                    habit = habit,
                    completed = completedTodayXp.containsKey(habit.id),
                    completedXp = completedTodayXp[habit.id],
                    onClick = { onNavigateToHabitDetail(habit.id) }
                )
            }
        }

    }
}

@Composable
private fun NextUnlockText() {
    Text(
        text = "✨ Next Unlock: Dragon Nest Background at Lv. 3",
        style = MaterialTheme.typography.bodySmall,
        color = ColorPaletteHome.Ink.copy(alpha = 0.5f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EmptyHomeQuest(onNavigateToHabits: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorPaletteHome.Card, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = ColorPaletteHome.Amber,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Your dragon is ready for its first tiny quest.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = ColorPaletteHome.Ink,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onNavigateToHabits,
            colors = ButtonDefaults.buttonColors(containerColor = ColorPaletteHome.Violet),
            shape = RoundedCornerShape(999.dp)
        ) {
            Text("Create first habit", modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}

@Composable
private fun HomeHabitItem(
    habit: HabitEntity,
    completed: Boolean,
    completedXp: Long?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (completed) ColorPaletteHome.Mint.copy(alpha = 0.15f) else ColorPaletteHome.Card,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = if (completed) Icons.Default.CheckCircle else habitIcon(habit),
                contentDescription = null,
                tint = if (completed) ColorPaletteHome.Green else ColorPaletteHome.Violet,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ColorPaletteHome.Ink
                )
                Text(
                    text = if (completed) "Nourished for today" else "${habit.type} • ${habit.currentStreak}d streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorPaletteHome.Ink.copy(alpha = 0.6f)
                )
            }
            if (completed) {
                Text(
                    text = "+${completedXp ?: 0L} XP",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteHome.Green
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = ColorPaletteHome.Violet.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun habitIcon(habit: HabitEntity) = when {
    habit.type == "TIMER" -> Icons.Default.FavoriteBorder
    habit.icon.contains("star", ignoreCase = true) -> Icons.Default.Star
    habit.icon.contains("pet", ignoreCase = true) || habit.icon.contains("dragon", ignoreCase = true) -> Icons.Default.Pets
    else -> Icons.Default.RadioButtonUnchecked
}

private object ColorPaletteHome {
    val Card = Color(0xFFFFFFFF)
    val LavenderSoft = Color(0xFFF2EEFF)
    val AmethystSoft = Color(0xFFE5DDFF)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Honey = Color(0xFFFF9F1C)
    val Mint = Color(0xFF4EDB95)
    val Green = Color(0xFF27A86B)
    val Line = Color(0xFFEBE9F5)
    val Ink = Color(0xFF1C1930)
}