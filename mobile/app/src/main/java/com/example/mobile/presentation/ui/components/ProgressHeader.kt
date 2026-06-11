package com.example.mobile.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile.domain.ExpConfig

data class ProgressHeaderState(
    val level: Int,
    val xp: Long,
    val evolutionStage: Int,
    val totalCoins: Int,
    val globalStreak: Int
)

@Composable
fun ProgressHeader(
    state: ProgressHeaderState,
    modifier: Modifier = Modifier
) {
    val currentLevelXp = ExpConfig.xpProgressInCurrentLevel(state.xp)
    val nextLevelXp = ExpConfig.xpRequiredForNextLevel(state.xp).coerceAtLeast(1L)
    val levelProgress = (currentLevelXp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)
    val evolutionProgress = evolutionProgressFraction(state.xp, state.evolutionStage)
    val nextEvolutionText = nextEvolutionText(state.xp, state.evolutionStage)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Level ${state.level} • ${ExpConfig.evolutionStageName(state.evolutionStage)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = nextEvolutionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressChip("XP $currentLevelXp / $nextLevelXp")
                ProgressChip("${state.globalStreak}d streak")
                ProgressChip("${state.totalCoins} coins")
                ProgressChip("Evolution ${((evolutionProgress * 100f).toInt())}%")
            }
        }
    }
}

@Composable
fun EvolutionTeaser(
    totalXp: Long,
    currentStage: Int,
    modifier: Modifier = Modifier
) {
    val lastIndex = ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex
    val normalizedStage = currentStage.coerceIn(0, lastIndex)
    val nextStage = (normalizedStage + 1).coerceAtMost(lastIndex)
    val nextStageName = ExpConfig.evolutionStageName(nextStage)
    val currentThreshold = ExpConfig.xpThresholdForStage(normalizedStage)
    val nextThreshold = ExpConfig.xpThresholdForStage(nextStage)
    val xpNeeded = if (nextStage == normalizedStage) {
        0L
    } else {
        (nextThreshold - totalXp).coerceAtLeast(0L)
    }
    val progress = if (nextStage == normalizedStage || nextThreshold <= currentThreshold) {
        1f
    } else {
        ((totalXp - currentThreshold).coerceAtLeast(0L).toFloat() / (nextThreshold - currentThreshold).toFloat())
            .coerceIn(0f, 1f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next evolution",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = nextStageName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )

            Text(
                text = if (xpNeeded == 0L) {
                    "Ancient Dragon reached"
                } else {
                    "$xpNeeded XP to reach $nextStageName"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun evolutionProgressFraction(totalXp: Long, stage: Int): Float {
    val lastIndex = ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex
    val currentStage = stage.coerceIn(0, lastIndex)
    val nextStage = (currentStage + 1).coerceAtMost(lastIndex)
    val currentThreshold = ExpConfig.xpThresholdForStage(currentStage)
    val nextThreshold = ExpConfig.xpThresholdForStage(nextStage)

    if (nextStage == currentStage) return 1f
    if (nextThreshold <= currentThreshold) return 0f

    val progress = (totalXp - currentThreshold).coerceAtLeast(0L)
    val required = nextThreshold - currentThreshold
    return (progress.toFloat() / required.toFloat()).coerceIn(0f, 1f)
}

private fun nextEvolutionText(totalXp: Long, stage: Int): String {
    val lastIndex = ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex
    val currentStage = stage.coerceIn(0, lastIndex)
    val nextStage = (currentStage + 1).coerceAtMost(lastIndex)
    val xpNeeded = (ExpConfig.xpThresholdForStage(nextStage) - totalXp).coerceAtLeast(0L)

    return if (nextStage == currentStage) {
        "Ancient Dragon reached"
    } else {
        "Next: ${ExpConfig.evolutionStageName(nextStage)} • $xpNeeded XP"
    }
}
