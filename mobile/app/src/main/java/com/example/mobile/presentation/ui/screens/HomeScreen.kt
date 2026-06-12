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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.DragonMood
import com.example.mobile.domain.ExpConfig
import com.example.mobile.presentation.ui.components.AnimatedPet

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

    Scaffold(
        containerColor = Color(0xFFFAFAFC),
        topBar = {
            GamifiedFixedHeader(
                streak = uiState.globalStreak,
                coins = uiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(pet.evolutionStage)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                DragonHero(
                    pet = pet,
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
}

@Composable
private fun GamifiedFixedHeader(
    streak: Int,
    coins: Int,
    stageName: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
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
                    tint = ColorPaletteHome.Honey,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet Balance",
                    tint = ColorPaletteHome.Amber,
                    modifier = Modifier.size(22.dp)
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
private fun DragonHero(
    pet: com.example.mobile.data.local.entities.PetEntity,
    modifier: Modifier = Modifier
) {
    // Exact mapping from your PetScreen implementation
    val currentLevelXp = ExpConfig.xpProgressInCurrentLevel(pet.xp)
    val xpRequiredForNextLevel = ExpConfig.xpRequiredForCurrentLevelProgress(pet.xp)
    val progressFraction = (currentLevelXp.toFloat() / xpRequiredForNextLevel.toFloat()).coerceIn(0f, 1f)

    val currentStageName = ExpConfig.evolutionStageName(pet.evolutionStage)
    val nextStageName = ExpConfig.evolutionStageName(pet.evolutionStage + 1).ifBlank { "Max Tier reached" }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ColorPaletteHome.LavenderSoft.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(ColorPaletteHome.AmethystSoft.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                )

                val petSize = if (LocalConfiguration.current.screenWidthDp < 360) 150.dp else 175.dp
                AnimatedPet(
                    pet = pet,
                    modifier = Modifier.size(petSize),
                    showNameOverlay = false
                )
            }

            Text(
                text = pet.name.ifBlank { "Meet My Pet" },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = ColorPaletteHome.Ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelBadge(pet.level)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = ColorPaletteHome.Mint.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = DragonMood.from(pet.mood).displayName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ColorPaletteHome.Green,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Modernized Minimalist Experience & Evolution Milestone Meter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Level Progress",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteHome.Ink.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$currentLevelXp / ${xpRequiredForNextLevel} XP",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteHome.Violet
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = ColorPaletteHome.Violet,
                    trackColor = ColorPaletteHome.Line,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current: $currentStageName",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = ColorPaletteHome.Ink.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Next Phase: $nextStageName ✨",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteHome.Violet.copy(alpha = 0.9f)
                    )
                }
            }
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
    val sortedHabits = habits.sortedWith(
        compareBy<HabitEntity> { completedToday[it.id] != true }
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
                    completed = completedToday[habit.id] == true,
                    onClick = { onNavigateToHabitDetail(habit.id) }
                )
            }
        }

        Text(
            text = "✨ Next Unlock: Dragon Nest Background at Lv. 3",
            style = MaterialTheme.typography.bodySmall,
            color = ColorPaletteHome.Ink.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
    }
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
                    text = "+100 XP",
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

@Composable
private fun LevelBadge(level: Int) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = ColorPaletteHome.Amber
    ) {
        Text(
            text = "Lv. $level",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
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
    val Honey = Color(0xFFFF9F1C)
    val Mint = Color(0xFF4EDB95)
    val Green = Color(0xFF27A86B)
    val Line = Color(0xFFEBE9F5)
    val Ink = Color(0xFF1C1930)
}