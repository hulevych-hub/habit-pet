package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.domain.AchievementsConfig
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.viewmodel.AchievementViewModel
import androidx.compose.runtime.collectAsState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AchievementScreen(
    achievementViewModel: AchievementViewModel = hiltViewModel()
) {
    val achievements by achievementViewModel.achievements.collectAsState()
    val statistics by achievementViewModel.statistics.collectAsState()
    val pet by achievementViewModel.pet.collectAsState()
    val ownedCustomizations by achievementViewModel.ownedCustomizations.collectAsState()
    val habitCount by achievementViewModel.habitCount.collectAsState()
    val isLoading by achievementViewModel.isLoading.collectAsState()
    val error by achievementViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Achievements", color = ColorPaletteAchievements.Ink) }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                AchievementHeader(
                    claimableCount = achievements.count { it.isUnlocked && !it.isClaimed },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }

            when {
                isLoading -> item {
                    Text(
                        text = "Loading achievements...",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                !error.isNullOrBlank() -> item {
                    Text(
                        text = error.orEmpty(),
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                achievements.isEmpty() -> item {
                    EmptyStateCard(
                        title = "Your first milestone is close",
                        message = "Finish a habit, earn XP, or grow your streak to awaken the first achievement.",
                        hint = "Every small win moves your dragon closer to its first badge.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(
                items = achievements,
                key = { achievement -> achievement.id }
            ) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    title = AchievementsConfig.achievementById(achievement.id)?.name ?: achievement.id,
                    description = AchievementsConfig.achievementById(achievement.id)?.description ?: "Keep growing.",
                    progress = achievementViewModel.progressFor(
                        achievement = achievement,
                        stats = statistics,
                        petState = pet,
                        ownedCustomizationCount = ownedCustomizations,
                        currentHabitCount = habitCount
                    ),
                    progressFraction = achievementViewModel.progressFraction(achievement.progress, achievement),
                    progressLabel = achievementViewModel.progressLabel(achievement.progress, achievement),
                    rewards = achievementViewModel.rewardLabels(achievement),
                    onClaim = { achievementViewModel.claimAchievement(achievement.id) }
                )
            }
        }
    }
}

@Composable
private fun AchievementHeader(
    claimableCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ColorPaletteAchievements.Card),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPaletteAchievements.LavenderSoft)
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ColorPaletteAchievements.Amber,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Milestone nest",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ColorPaletteAchievements.Ink
                    )
                }
                Text(
                    text = "Claimable rewards: $claimableCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorPaletteAchievements.Violet
                )
                Text(
                    text = "Each achievement is a cozy keepsake from your real-life rhythm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementEntity,
    title: String,
    description: String,
    progress: Int,
    progressFraction: Float,
    progressLabel: String,
    rewards: List<String>,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                achievement.isClaimed -> ColorPaletteAchievements.Claimed
                achievement.isUnlocked -> ColorPaletteAchievements.Card
                else -> ColorPaletteAchievements.Locked
            }
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (achievement.isUnlocked) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (achievement.isUnlocked) ColorPaletteAchievements.Amber.copy(alpha = 0.18f) else ColorPaletteAchievements.Mystery
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (achievement.isUnlocked) ColorPaletteAchievements.Amber else ColorPaletteAchievements.Card,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(22.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = ColorPaletteAchievements.Ink
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (achievement.isClaimed) {
                    Text(
                        text = "Claimed",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorPaletteAchievements.Mint,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = progressFraction.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = if (achievement.isUnlocked) ColorPaletteAchievements.Amber else ColorPaletteAchievements.Violet.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progressLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = rewards.joinToString(" • "),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (achievement.isUnlocked) ColorPaletteAchievements.Amber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onClaim,
                enabled = achievement.isUnlocked && !achievement.isClaimed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (achievement.isUnlocked) ColorPaletteAchievements.Violet else ColorPaletteAchievements.Mystery
                ),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        achievement.isClaimed -> "Reward Collected"
                        achievement.isUnlocked -> "Claim Reward"
                        else -> "Locked at $progress"
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private object ColorPaletteAchievements {
    val Card = Color(0xFFFFFFFF)
    val LavenderSoft = Color(0xFFF2EEFF)
    val Claimed = Color(0xFFE8FBF2)
    val Locked = Color(0xFFEDEAF6)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Mint = Color(0xFF4EDB95)
    val Mystery = Color(0xFF3F3A6B)
    val Ink = Color(0xFF302B4A)
}
