package com.example.mobile.presentation.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.mobile.domain.ExpConfig
import com.example.mobile.ui.theme.AppTheme
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

data class ProgressHeaderState(
    val level: Int,
    val xp: Long,
    val evolutionStage: Int,
    val totalCoins: Int,
    val globalStreak: Int,
    val lastStreakDate: Long? = null,
    val currentCombo: Int = 0,
    val lastHabitCompletionTimestamp: Long = 0L
)

@Composable
fun ProgressHeader(
    state: ProgressHeaderState,
    modifier: Modifier = Modifier
) {
    val currentLevelXp = ExpConfig.xpProgressInCurrentLevel(state.xp)
    val nextLevelXp = ExpConfig.xpRequiredForCurrentLevelProgress(state.xp)
    val levelProgress = (currentLevelXp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)
    val evolutionProgress = evolutionProgressFraction(state.xp, state.evolutionStage)
    val nextEvolutionText = nextEvolutionText(state.xp, state.evolutionStage)
    val visualState = streakVisualState(state.globalStreak)
    val nextMilestone = STREAK_MILESTONE_MARKERS.firstOrNull { it > state.globalStreak }
    val protectionText = streakProtectionText(state.globalStreak, state.lastStreakDate)
    val activeCombo = if (ExpConfig.isComboActive(state.lastHabitCompletionTimestamp, System.currentTimeMillis())) {
        state.currentCombo
    } else {
        0
    }
    val comboMultiplier = ExpConfig.comboMultiplier(activeCombo)

    var pulseVersion by remember { mutableStateOf(0) }
    var previousStreak by remember { mutableStateOf(state.globalStreak) }
    var previousCombo by remember { mutableStateOf(activeCombo) }
    LaunchedEffect(state.globalStreak) {
        if (state.globalStreak > previousStreak) {
            pulseVersion += 1
        }
        previousStreak = state.globalStreak
    }
    LaunchedEffect(activeCombo) {
        if (activeCombo > previousCombo) {
            pulseVersion += 1
        }
        previousCombo = activeCombo
    }
    LaunchedEffect(pulseVersion) {
        if (pulseVersion > 0) {
            delay(240)
            pulseVersion = 0
        }
    }
    val streakScale by animateFloatAsState(
        targetValue = if (pulseVersion > 0) 1.06f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "streakPulseScale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.current.primaryContainer.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StreakIndicator(
                visualState = visualState,
                streak = state.globalStreak,
                nextMilestone = nextMilestone,
                protectionText = protectionText,
                scale = streakScale
            )

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
                    color = AppTheme.current.violet
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
                if (activeCombo > 1) {
                    ProgressChip(
                        text = "Combo x${formatMultiplier(comboMultiplier)}",
                        achieved = true,
                        accentColor = COMBO_COLOR
                    )
                }
                CoinPill(amount = state.totalCoins)
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
            containerColor = AppTheme.current.secondaryContainer.copy(alpha = 0.35f)
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
                    color = AppTheme.current.onSurfaceVariant
                )
                Text(
                    text = nextStageName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.current.violet
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
                color = AppTheme.current.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressChip(
    text: String,
    achieved: Boolean = false,
    accentColor: Color? = null
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (achieved && accentColor != null) {
            accentColor.copy(alpha = 0.16f)
        } else {
            AppTheme.current.surface.copy(alpha = 0.85f)
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accentColor?.takeIf { achieved }
                ?: AppTheme.current.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private data class StreakVisualState(
    val label: String,
    val title: String,
    val color: Color
)

private val STREAK_MILESTONE_MARKERS = listOf(3, 7, 14, 30, 60, 100)
private val LOW_STREAK_COLOR = AppTheme.current.amber
private val STABLE_STREAK_COLOR = AppTheme.current.blue
private val STRONG_STREAK_COLOR = AppTheme.current.danger
private val COMBO_COLOR = AppTheme.current.purple

@Composable
private fun StreakIndicator(
    visualState: StreakVisualState,
    streak: Int,
    nextMilestone: Int?,
    protectionText: String,
    scale: Float
) {
    val streakLabel = if (streak == 0) "No streak yet" else "$streak Day Streak"
    val milestoneLabel = nextMilestone?.let { "Next marker: $it days" } ?: "Legendary rhythm"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        color = visualState.color.copy(alpha = 0.10f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = visualState.title,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.current.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = streakLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = visualState.color
                )
                Text(
                    text = visualState.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = visualState.color
                )
            }
            StreakMilestoneMarkers(
                streak = streak,
                milestoneLabel = milestoneLabel,
                visualState = visualState
            )
            Text(
                text = protectionText,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.current.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StreakMilestoneMarkers(
    streak: Int,
    milestoneLabel: String,
    visualState: StreakVisualState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Markers",
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.current.onSurfaceVariant
        )
        STREAK_MILESTONE_MARKERS.forEach { marker ->
            ProgressChip(
                text = "${marker}d",
                achieved = streak >= marker,
                accentColor = visualState.color
            )
        }
        ProgressChip(text = milestoneLabel, accentColor = visualState.color)
    }
}

private fun formatMultiplier(multiplier: Float): String =
    String.format(Locale.US, "%.2f", multiplier)

private fun streakVisualState(streak: Int): StreakVisualState = when {
    streak == 0 -> StreakVisualState(
        label = "Starting",
        title = "Your dragon is ready for a first tiny rhythm.",
        color = LOW_STREAK_COLOR
    )
    streak < 3 -> StreakVisualState(
        label = "Low",
        title = "A small flame is forming.",
        color = LOW_STREAK_COLOR
    )
    streak < 7 -> StreakVisualState(
        label = "Stable",
        title = "Your dragon feels your steady rhythm.",
        color = STABLE_STREAK_COLOR
    )
    else -> StreakVisualState(
        label = "Strong",
        title = "Your consistency is becoming rare.",
        color = STRONG_STREAK_COLOR
    )
}

private fun streakProtectionText(streak: Int, lastStreakDate: Long?): String = when {
    lastStreakDate == null -> "Your rhythm is visible wherever you play."
    streak == 0 -> "Start with one tiny win today."
    lastStreakDate == todayInDaysSinceEpoch() -> "Today is protected. Finish every habit to strengthen the flame."
    else -> "Protect the flame: complete every habit today to keep $streak days alive."
}

private fun todayInDaysSinceEpoch(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis / 86_400_000L
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
