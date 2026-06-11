package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.AchievementsConfig
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.viewmodel.AchievementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    achievementViewModel: AchievementViewModel = hiltViewModel()
) {
    val achievements by achievementViewModel.achievements.collectAsState()
    val isLoading by achievementViewModel.isLoading.collectAsState()
    val error by achievementViewModel.error.collectAsState()
    val stats by achievementViewModel.statistics.collectAsState()
    val pet by achievementViewModel.pet.collectAsState()
    val ownedCustomizations by achievementViewModel.ownedCustomizations.collectAsState()
    val habitCount by achievementViewModel.habitCount.collectAsState()

    val unlockedCount = achievements.count { it.isUnlocked }
    val claimableCount = achievements.count { it.isUnlocked && !it.isClaimed }
    val completionProgress = if (achievements.isEmpty()) 0f else {
        unlockedCount.toFloat() / achievements.size.toFloat()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Achievements") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Text(
                        text = "Loading achievements...",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                !error.isNullOrBlank() -> {
                    Text(
                        text = error!!,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                achievements.isEmpty() -> {
                    EmptyStateCard(
                        title = "Your first milestone is close",
                        message = "Finish a habit, earn XP, or grow your streak to awaken the first achievement.",
                        hint = "Every small win moves your dragon closer to its first badge.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            AchievementSummary(
                                unlockedCount = unlockedCount,
                                totalCount = achievements.size,
                                claimableCount = claimableCount,
                                progress = completionProgress
                            )
                        }

                        items(
                            items = achievements,
                            key = { it.id }
                        ) { achievement ->
                            AchievementItem(
                                achievement = achievement,
                                stats = stats,
                                pet = pet,
                                ownedCustomizations = ownedCustomizations,
                                habitCount = habitCount,
                                viewModel = achievementViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementSummary(
    unlockedCount: Int,
    totalCount: Int,
    claimableCount: Int,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Progress",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$unlockedCount / $totalCount unlocked",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "$claimableCount ready to claim",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AchievementItem(
    achievement: AchievementEntity,
    stats: StatisticsEntity,
    pet: PetEntity,
    ownedCustomizations: Int,
    habitCount: Int,
    viewModel: AchievementViewModel
) {
    val currentValue = viewModel.progressFor(
        achievement = achievement,
        stats = stats,
        petState = pet,
        ownedCustomizationCount = ownedCustomizations,
        currentHabitCount = habitCount
    )
    val progress = viewModel.progressFraction(currentValue, achievement).coerceIn(0f, 1f)
    val definition = AchievementsConfig.achievementById(achievement.id)
    val isClaimable = achievement.isUnlocked && !achievement.isClaimed
    val isClaimed = achievement.isClaimed
    val statusText = when {
        isClaimed -> "Claimed"
        isClaimable -> "Ready to claim"
        achievement.isUnlocked -> "Unlocked"
        else -> "Locked"
    }
    val statusColor = when {
        isClaimed -> MaterialTheme.colorScheme.onSurfaceVariant
        isClaimable -> Color(0xFFFFD700)
        achievement.isUnlocked -> Color(0xFF34D399)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val progressText = viewModel.progressLabel(currentValue, achievement)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (achievement.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (achievement.isUnlocked) Color(0xFF34D399) else Color.Gray,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = definition?.name ?: achievement.id,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = definition?.description ?: "Milestone progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isClaimable) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )

        RewardChips(viewModel.rewardLabels(achievement))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )

            if (isClaimable) {
                Button(
                    onClick = { viewModel.claimAchievement(achievement.id) }
                ) {
                    Text("Claim")
                }
            }
        }
    }
}

@Composable
private fun RewardChips(rewards: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rewards.forEach { reward ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = rewardIcon(reward),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = reward,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun rewardIcon(reward: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        reward.contains("coins", ignoreCase = true) -> Icons.Default.Star
        reward.contains("EXP", ignoreCase = true) -> Icons.Default.Star
        reward.contains("chest", ignoreCase = true) -> Icons.Default.EmojiEvents
        else -> Icons.Default.CheckCircle
    }
}
