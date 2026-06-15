package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.domain.AchievementsConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.presentation.ui.components.CoinIcon
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.viewmodel.AchievementViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AchievementScreen(
    achievementViewModel: AchievementViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val achievements by achievementViewModel.achievements.collectAsState()
    val isLoading by achievementViewModel.isLoading.collectAsState()
    val error by achievementViewModel.error.collectAsState()
    val progressUiState by homeScreenViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFFAFAFC),
        topBar = {
            GamifiedFixedHeader(
                streak = progressUiState.globalStreak,
                coins = progressUiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(progressUiState.pet.evolutionStage),
                streakCompletedToday = progressUiState.globalStreakCompletedToday
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            item {
                AchievementHeader(
                    claimableCount = achievements.count { it.isUnlocked && !it.isClaimed }
                )
            }

            when {
                isLoading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColorPaletteAchievements.Violet)
                    }
                }
                !error.isNullOrBlank() -> item {
                    ErrorStateCard(
                        message = error.orEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        onRetry = achievementViewModel::retryLoadAchievements
                    )
                }
                achievements.isEmpty() -> item {
                    EmptyStateCard(
                        title = "Your first milestone is close",
                        message = "Finish a habit, earn XP, or grow your streak to awaken the first achievement.",
                        hint = "Every small win moves your dragon closer to its first badge.",
                        modifier = Modifier.fillMaxWidth()
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
private fun GamifiedFixedHeader(
    streak: Int,
    coins: Int,
    stageName: String,
    streakCompletedToday: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
        val streakTint = if (streakCompletedToday) ColorPaletteAchievements.Honey else Color(0xFFA9A3B8)

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
                    color = ColorPaletteAchievements.Ink
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ColorPaletteAchievements.Violet.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = ColorPaletteAchievements.Violet,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stageName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteAchievements.Violet
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CoinIcon(
                    modifier = Modifier.size(22.dp),
                    tint = ColorPaletteAchievements.Amber
                )
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteAchievements.Ink
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteAchievements.Ink),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ColorPaletteAchievements.Ink, Color(0xFF231D3D))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Milestone Nest",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Every badge reflects a rhythm maintained in your real-world journey.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )

                    if (claimableCount > 0) {
                        Surface(
                            modifier = Modifier.padding(top = 8.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = ColorPaletteAchievements.Amber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "✨ $claimableCount Rewards Ready to Claim",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ColorPaletteAchievements.Amber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (claimableCount > 0) ColorPaletteAchievements.Amber else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementEntity,
    title: String,
    description: String,
    progressFraction: Float,
    progressLabel: String,
    rewards: List<String>,
    onClaim: () -> Unit
) {
    val isUnlockedAndUnclaimed = achievement.isUnlocked && !achievement.isClaimed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isUnlockedAndUnclaimed) 2.dp else 0.dp,
                brush = Brush.sweepGradient(listOf(ColorPaletteAchievements.Amber, ColorPaletteAchievements.Violet)),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isClaimed) ColorPaletteAchievements.ClaimedSurface else ColorPaletteAchievements.Card
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (achievement.isUnlocked && !achievement.isClaimed) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = when {
                                    achievement.isClaimed -> ColorPaletteAchievements.Mint.copy(alpha = 0.12f)
                                    achievement.isUnlocked -> ColorPaletteAchievements.Amber.copy(alpha = 0.15f)
                                    else -> ColorPaletteAchievements.Ink.copy(alpha = 0.05f)
                                },
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (achievement.isClaimed) Icons.Default.CheckCircle else if (achievement.isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                            contentDescription = null,
                            tint = when {
                                achievement.isClaimed -> ColorPaletteAchievements.Mint
                                achievement.isUnlocked -> ColorPaletteAchievements.Amber
                                else -> ColorPaletteAchievements.Muted.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (achievement.isClaimed) ColorPaletteAchievements.Ink.copy(alpha = 0.6f) else ColorPaletteAchievements.Ink
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorPaletteAchievements.Muted
                        )
                    }
                }

                if (achievement.isClaimed) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ColorPaletteAchievements.Mint.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Claimed",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ColorPaletteAchievements.Mint,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!achievement.isClaimed) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(ColorPaletteAchievements.Ink.copy(alpha = 0.04f), RoundedCornerShape(999.dp)),
                        color = if (achievement.isUnlocked) ColorPaletteAchievements.Amber else ColorPaletteAchievements.Violet,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = progressLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = ColorPaletteAchievements.Muted
                        )
                        Text(
                            text = rewards.joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (achievement.isUnlocked) ColorPaletteAchievements.Amber else ColorPaletteAchievements.Violet
                        )
                    }
                }
            }

            if (!achievement.isClaimed) {
                Button(
                    onClick = onClaim,
                    enabled = isUnlockedAndUnclaimed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUnlockedAndUnclaimed) ColorPaletteAchievements.Violet else ColorPaletteAchievements.Ink.copy(alpha = 0.05f),
                        disabledContainerColor = ColorPaletteAchievements.Ink.copy(alpha = 0.04f),
                        disabledContentColor = ColorPaletteAchievements.Muted.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(
                        text = if (isUnlockedAndUnclaimed) "Claim Loot Reward" else "Locked Milestone",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

private object ColorPaletteAchievements {
    val Card = Color(0xFFFFFFFF)
    val ClaimedSurface = Color(0xFFF4FBF7)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Honey = Color(0xFFFF9F1C)
    val Mint = Color(0xFF1E9453)
    val Muted = Color(0xFF7E7A94)
    val Ink = Color(0xFF1E1A34)
}