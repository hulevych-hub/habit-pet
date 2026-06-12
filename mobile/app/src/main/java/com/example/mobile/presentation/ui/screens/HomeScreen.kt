package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.DragonMood
import com.example.mobile.domain.ExpConfig
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeTopBar(
                streak = uiState.globalStreak,
                coins = uiState.totalCoins,
                onHabitsClick = onNavigateToHabits
            )

            DragonHero(
                pet = pet,
                modifier = Modifier.fillMaxWidth()
            )

            ProgressModule(
                state = ProgressHeaderState(
                    level = pet.level,
                    xp = pet.xp,
                    evolutionStage = pet.evolutionStage,
                    totalCoins = uiState.totalCoins,
                    globalStreak = uiState.globalStreak,
                    lastStreakDate = uiState.lastStreakDate,
                    currentCombo = uiState.currentCombo,
                    lastHabitCompletionTimestamp = uiState.lastHabitCompletionTimestamp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            DailyGoalCard(
                goalXp = uiState.dailyGoalXp.toLong(),
                progressXp = uiState.dailyGoalProgressXp,
                completed = uiState.dailyGoalCompleted,
                modifier = Modifier.fillMaxWidth()
            )

            TodayNourishmentSection(
                habits = uiState.habits,
                completedToday = uiState.completedToday,
                onNavigateToHabits = onNavigateToHabits,
                onNavigateToHabitDetail = onNavigateToHabitDetail
            )
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
private fun HomeTopBar(
    streak: Int,
    coins: Int,
    onHabitsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PremiumPill(
            icon = Icons.Default.FavoriteBorder,
            title = "Streak",
            value = "$streak days",
            accentColor = ColorPaletteHome.Honey,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        PremiumPill(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Wallet",
            value = coins.toString(),
            accentColor = ColorPaletteHome.Amber,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DragonHero(
    pet: com.example.mobile.data.local.entities.PetEntity,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val heroHeight = if (maxHeight < 680.dp) 270.dp else 330.dp
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ColorPaletteHome.LavenderSoft,
                                ColorPaletteHome.AmethystSoft,
                                ColorPaletteHome.Card
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .background(ColorPaletteHome.Amber.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val petSize = if (LocalConfiguration.current.screenWidthDp < 360) 205.dp else 235.dp
                    AnimatedPet(
                        pet = pet,
                        modifier = Modifier.size(petSize),
                        showNameOverlay = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pet.name.ifBlank { "Dragon" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = ColorPaletteHome.Ink,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LevelBadge(pet.level)
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.72f)
                        ) {
                            Text(
                                text = ExpConfig.evolutionStageName(pet.evolutionStage),
                                style = MaterialTheme.typography.labelMedium,
                                color = ColorPaletteHome.Violet,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = ColorPaletteHome.Mint.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = DragonMood.from(pet.mood).displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = ColorPaletteHome.Green,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressModule(
    state: ProgressHeaderState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ColorPaletteHome.Card),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Growth track",
                    style = MaterialTheme.typography.titleLarge,
                    color = ColorPaletteHome.Ink
                )
                Text(
                    text = "Lv. ${state.level}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorPaletteHome.Violet
                )
            }
            ProgressHeader(state = state)
            EvolutionTeaser(
                totalXp = state.xp,
                currentStage = state.evolutionStage
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
    val title = if (completed) "Daily goal complete" else "Today's nourishment"
    val message = if (completed) {
        "Your dragon is glowing from today's steady rhythm."
    } else {
        "${progressXp.toInt()} / ${goalXp.toInt()} XP · three small wins light the path."
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (completed) ColorPaletteHome.Mint.copy(alpha = 0.16f) else ColorPaletteHome.Card
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorPaletteHome.Ink
                )
                Text(
                    text = if (completed) "Complete" else "Growing",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (completed) ColorPaletteHome.Green else ColorPaletteHome.Violet,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = if (completed) ColorPaletteHome.Amber else ColorPaletteHome.Violet
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodayNourishmentSection(
    habits: List<HabitEntity>,
    completedToday: Map<Long, Boolean>,
    onNavigateToHabits: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteHome.Card),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's nourishment",
                    style = MaterialTheme.typography.titleLarge,
                    color = ColorPaletteHome.Ink
                )
                Button(
                    onClick = onNavigateToHabits,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPaletteHome.Violet),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Manage")
                }
            }

            if (habits.isEmpty()) {
                EmptyHomeQuest(onNavigateToHabits)
            } else {
                habits.forEach { habit ->
                    HomeHabitItem(
                        habit = habit,
                        completed = completedToday[habit.id] == true,
                        onClick = { onNavigateToHabitDetail(habit.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHomeQuest(onNavigateToHabits: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorPaletteHome.LavenderSoft, RoundedCornerShape(24.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = ColorPaletteHome.Amber,
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your dragon is ready for its first tiny quest.",
            style = MaterialTheme.typography.bodyLarge,
            color = ColorPaletteHome.Ink,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToHabits,
            colors = ButtonDefaults.buttonColors(containerColor = ColorPaletteHome.Amber),
            shape = RoundedCornerShape(999.dp)
        ) {
            Text("Create first habit")
        }
    }
}

@Composable
private fun HomeHabitItem(
    habit: HabitEntity,
    completed: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (completed) ColorPaletteHome.Mint.copy(alpha = 0.16f) else ColorPaletteHome.LavenderSoft,
        shadowElevation = if (completed) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (completed) Icons.Default.CheckCircle else habitIcon(habit),
                contentDescription = if (completed) "Completed" else "Not completed",
                tint = if (completed) ColorPaletteHome.Green else ColorPaletteHome.Violet,
                modifier = Modifier.size(30.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (completed) ColorPaletteHome.Ink else ColorPaletteHome.Ink
                )
                Text(
                    text = if (completed) "Nourished for today" else "${habit.type} • ${habit.currentStreak} day streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (completed) {
                Text(
                    text = "+100 XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorPaletteHome.Green
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Open habit",
                    tint = ColorPaletteHome.Violet.copy(alpha = 0.55f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun PremiumPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = ColorPaletteHome.Card,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorPaletteHome.Ink
                )
            }
        }
    }
}

@Composable
private fun LevelBadge(level: Int) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = ColorPaletteHome.Amber,
        shadowElevation = 3.dp
    ) {
        Text(
            text = "Lv. $level",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
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
    val Honey = Color(0xFFFFB84D)
    val Mint = Color(0xFF4EDB95)
    val Green = Color(0xFF27A86B)
    val Ink = Color(0xFF302B4A)
}
