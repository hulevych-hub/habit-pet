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
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.DesignTokens

@Composable
fun ChallengeCard(
    state: ChallengeUiState,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val challenge = state.challenge

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignTokens.cardCornerRounded,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.current.surfaceVariant.copy(alpha = DesignTokens.alpha72)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.elevationLg)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Card.padding),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.space12)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.space2),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.space12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(DesignTokens.Card.iconSizeLg)
                        .clip(CircleShape)
                        .background(AppTheme.current.gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = challenge?.icon?.challengeIcon() ?: Icons.Default.Star,
                        contentDescription = null,
                        tint = AppTheme.current.onSecondary,
                        modifier = Modifier.size(DesignTokens.Icon.size2xl)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge?.title ?: "No challenge yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppTheme.current.ink,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = challenge?.description ?: "A fresh challenge will appear after you claim your current one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.current.muted,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.space8)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.progressLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.current.ink
                    )
                    Text(
                        text = if (state.isCompleted) "Ready to claim" else "Soft objective",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.isCompleted) AppTheme.current.success else AppTheme.current.violet
                    )
                }

                LinearProgressIndicator(
                    progress = state.progressFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.space2)
                        .height(DesignTokens.space10)
                        .clip(DesignTokens.cardCornerCircle),
                    color = if (state.isCompleted) AppTheme.current.success else AppTheme.current.violet,
                    trackColor = AppTheme.current.progressTrack
                )
            }

            RewardPreview(
                rewards = state.rewards,
                modifier = Modifier.padding(horizontal = DesignTokens.space2)
            )

            if (state.isCompleted && !state.isClaimed) {
                Button(
                    onClick = onClaim,
                    modifier = Modifier
                        .padding(horizontal = DesignTokens.space2)
                        .fillMaxWidth(),
                    shape = DesignTokens.cardCornerSm
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        modifier = Modifier.size(DesignTokens.Icon.sizeSm)
                    )
                    Spacer(modifier = Modifier.padding(DesignTokens.space6))
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
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.space8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rewards.isEmpty()) {
            Text(
                text = "Rewards will appear here",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.current.muted
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
            .clip(DesignTokens.cardCornerCircle)
            .background(AppTheme.current.violet.copy(alpha = DesignTokens.alpha8))
            .padding(horizontal = DesignTokens.space10, vertical = DesignTokens.space6),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.space6),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = reward.icon(),
                contentDescription = null,
                tint = AppTheme.current.violet,
                modifier = Modifier.size(DesignTokens.Icon.sizeXs)
            )
            Text(
                text = reward.label(),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.current.muted
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
