package com.example.mobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mobile.domain.ChallengeRewardDefinition
import com.example.mobile.domain.ChallengeType
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.EquipableType
import com.example.mobile.domain.repository.ChallengeUiState
import com.example.mobile.domain.rewardLabel

@Composable
fun ChallengeCard(
    state: ChallengeUiState,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val challenge = state.challenge

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD166)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = challenge?.icon?.challengeIcon() ?: Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge?.title ?: "No challenge yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = challenge?.description ?: "A fresh challenge will appear after you claim your current one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.progressLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (state.isCompleted) "Ready to claim" else "Soft objective",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = state.progressFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = if (state.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }

            RewardPreview(rewards = state.rewards)

            if (state.isCompleted && !state.isClaimed) {
                Button(
                    onClick = onClaim,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(6.dp))
                    Text("Claim Challenge Reward")
                }
            }
        }
    }
}

@Composable
private fun RewardPreview(
    rewards: List<ChallengeRewardDefinition>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rewards.isEmpty()) {
            Text(
                text = "Rewards will appear here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            rewards.forEach { reward ->
                RewardChip(reward = reward)
            }
        }
    }
}

@Composable
private fun RewardChip(reward: ChallengeRewardDefinition) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF111827).copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = reward.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = reward.label(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun String.challengeIcon() = when (this) {
    "habit" -> Icons.Default.LocalFireDepartment
    "xp", "stars" -> Icons.Default.Star
    "coins" -> Icons.Default.MonetizationOn
    "chest" -> Icons.Default.Celebration
    "streak" -> Icons.Default.Timeline
    else -> Icons.Default.Star
}

private fun ChallengeRewardDefinition.icon() = when (this) {
    is ChallengeRewardDefinition.CoinReward -> Icons.Default.MonetizationOn
    is ChallengeRewardDefinition.ExpReward -> Icons.Default.Star
    is ChallengeRewardDefinition.ChestReward -> Icons.Default.Celebration
    is ChallengeRewardDefinition.CustomizationReward -> Icons.Default.ShoppingBag
}

private fun ChallengeRewardDefinition.label(): String = when (this) {
    is ChallengeRewardDefinition.CoinReward -> "+$amount coins"
    is ChallengeRewardDefinition.ExpReward -> "+$amount XP"
    is ChallengeRewardDefinition.ChestReward -> "${chestType.lowercase()} chest"
    is ChallengeRewardDefinition.CustomizationReward -> {
        val type = com.example.mobile.domain.EquipableConfig.definition(equipableId)?.type
        when (type) {
            EquipableType.OUTFIT -> "Outfit"
            EquipableType.BACKGROUND -> "Background"
            EquipableType.AURA -> "Aura"
            else -> "Customization"
        }
    }
}
