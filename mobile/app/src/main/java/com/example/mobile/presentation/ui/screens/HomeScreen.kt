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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.DragonMood
import com.example.mobile.domain.ExpConfig
import com.example.mobile.presentation.ui.components.AnimatedPet
import com.example.mobile.presentation.ui.components.GamifiedFixedHeader
import com.example.mobile.presentation.ui.components.LoadingStateCard
import com.example.mobile.presentation.ui.components.StreakCalendarOverlay
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.HabitPetTheme

@Composable
fun HomeScreen(
    onNavigateToHabits: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit,
    onNavigateToRewardsLocked: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val isLoading by homeScreenViewModel.isLoading.collectAsState()
    val streakCalendarState by homeScreenViewModel.streakCalendarState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        isLoading = isLoading,
        onNavigateToHabits = onNavigateToHabits,
        onNavigateToHabitDetail = onNavigateToHabitDetail,
        onNavigateToRewardsLocked = onNavigateToRewardsLocked,
        onRenamePet = homeScreenViewModel::renamePet,
        onResetGameData = homeScreenViewModel::resetAllGameData,
        onStreakClick = homeScreenViewModel::openGlobalStreakCalendar,
        onStreakCalendarDismiss = homeScreenViewModel::closeStreakCalendar,
        onPreviousStreakMonth = homeScreenViewModel::showPreviousStreakMonth,
        streakCalendarState = streakCalendarState
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeScreenViewModel.UiState,
    isLoading: Boolean,
    onNavigateToHabits: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit,
    onNavigateToRewardsLocked: () -> Unit,
    onRenamePet: (String) -> Unit,
    onResetGameData: () -> Unit,
    onStreakClick: () -> Unit,
    onStreakCalendarDismiss: () -> Unit,
    onPreviousStreakMonth: () -> Unit,
    streakCalendarState: StreakCalendarUiState?
) {
    val pet = uiState.pet
    val shouldRequestPetName = pet.id == 0L || pet.name.trim().isEmpty()
    var showMandatoryPetNameDialog by remember { mutableStateOf(false) }
    var showResetGameDialog by remember { mutableStateOf(false) }
    var petNameDraft by remember { mutableStateOf(pet.name) }

    LaunchedEffect(shouldRequestPetName) { showMandatoryPetNameDialog = shouldRequestPetName }
    LaunchedEffect(pet.name) { petNameDraft = pet.name }

    Scaffold(
        containerColor = AppTheme.current.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GamifiedFixedHeader(
                streak = uiState.globalStreak,
                coins = uiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(pet.evolutionStage),
                streakCompletedToday = uiState.globalStreakCompletedToday,
                onCoinsClick = onNavigateToRewardsLocked,
                onStreakClick = onStreakClick
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
                    onRenamePet(newName.trim())
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
                            onResetGameData()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.current.danger,
                            contentColor = AppTheme.current.onPrimary
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

        StreakCalendarOverlay(
            state = streakCalendarState,
            onDismiss = onStreakCalendarDismiss,
            onPreviousMonth = onPreviousStreakMonth
        )
    }
}

@Composable
private fun PetSummary(pet: PetEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.current.card,
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
                            colors = listOf(AppTheme.current.lavenderSoft.copy(alpha = 0.4f), Color.Transparent)
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
                    color = AppTheme.current.ink,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = AppTheme.current.amber
                    ) {
                        Text(
                            text = "Lv. ${pet.level}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.current.onSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = AppTheme.current.mint.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = DragonMood.from(pet.mood).displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.current.success,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
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
            containerColor = AppTheme.current.danger,
            contentColor = AppTheme.current.onSurface
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
                color = AppTheme.current.ink
            )
            Text(
                text = "Manage",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = AppTheme.current.violet,
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
private fun EmptyHomeQuest(onNavigateToHabits: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.current.card, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = AppTheme.current.amber,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Your dragon is ready for its first tiny quest.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = AppTheme.current.ink,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onNavigateToHabits,
            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.current.violet),
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
        color = if (completed) AppTheme.current.mint.copy(alpha = 0.15f) else AppTheme.current.card,
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
                tint = if (completed) AppTheme.current.success else AppTheme.current.violet,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.current.ink
                )
                Text(
                    text = if (completed) "Nourished for today" else "${habit.type} • ${habit.currentStreak}d streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.current.ink.copy(alpha = 0.6f)
                )
            }
            if (completed) {
                Text(
                    text = "+${completedXp ?: 0L} XP",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.current.success
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = AppTheme.current.violet.copy(alpha = 0.4f),
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

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390px,height=844px,dpi=420")
@Composable
private fun HomeScreenPreview() {
    HabitPetTheme {
        HomeScreenContent(
            uiState = HomeScreenViewModel.UiState(
                globalStreak = 4,
                habits = listOf(
                    HabitEntity(
                        id = 1,
                        name = "Morning hydration",
                        icon = "pet",
                        type = "CHECKBOX",
                        currentStreak = 4,
                        bestStreak = 7
                    ),
                    HabitEntity(
                        id = 2,
                        name = "Focused reading",
                        icon = "📚",
                        type = "TIMER",
                        minimumDurationMinutes = 15,
                        currentStreak = 2,
                        bestStreak = 5
                    )
                ),
                pet = PetEntity(
                    id = 1,
                    name = "Luna",
                    level = 3,
                    xp = 180,
                    evolutionStage = 1,
                    mood = "Calm"
                ),
                completedTodayXp = mapOf(1L to 10L),
                totalCoins = 128,
                lastStreakDate = 0L,
                currentCombo = 1,
                lastHabitCompletionTimestamp = 0L,
                globalStreakCompletedToday = false
            ),
            isLoading = false,
            onNavigateToHabits = {},
            onNavigateToHabitDetail = {},
            onNavigateToRewardsLocked = {},
            onRenamePet = {},
            onResetGameData = {},
            onStreakClick = {},
            onStreakCalendarDismiss = {},
            onPreviousStreakMonth = {},
            streakCalendarState = null
        )
    }
}
