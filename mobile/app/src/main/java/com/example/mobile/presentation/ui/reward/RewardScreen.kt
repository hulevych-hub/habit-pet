package com.example.mobile.presentation.ui.reward

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile.R
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.AchievementReward as ConfigAchievementReward
import com.example.mobile.presentation.ui.components.PetPhaseTransition
import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RewardScreen(
    reward: RewardUiEvent?,
    pet: PetEntity = PetEntity(id = 1),
    onRewardCompleted: () -> Unit
) {
    if (reward == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF334155)
                    )
                )
            )
            .clickable { onRewardCompleted() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {

            when (reward) {

                is RewardUiEvent.LevelUpReward -> LevelUpRewardContent(
                    level = reward.level,
                    coinsEarned = reward.coins
                )

                is RewardUiEvent.DragonEvolutionReward -> DragonEvolutionRewardContent(
                    pet = pet,
                    fromStage = reward.fromStage,
                    toStage = reward.toStage,
                    onConfirm = onRewardCompleted
                )

                is RewardUiEvent.StreakReward -> StreakRewardContent(
                    streak = reward.streak,
                    coinsEarned = reward.coins,
                    rewardSummary = reward.rewardSummary,
                    onConfirm = onRewardCompleted
                )

                is RewardUiEvent.ChestReward -> ChestRewardContent(
                    rewardType = reward.rewardType,
                    amount = reward.amount,
                    expAmount = reward.expAmount,
                    customizationId = reward.customizationId
                )

                is RewardUiEvent.AchievementReward -> AchievementRewardContent(
                    achievementName = reward.achievementName,
                    coinsEarned = reward.coins,
                    expAmount = reward.expAmount,
                    chestType = reward.chestType,
                    rewards = reward.rewards
                )

                is RewardUiEvent.CoinReward -> CoinRewardContent(
                    amount = reward.amount
                )
            }
        }
    }
}

// Level Up Reward Screen
@Composable
private fun LevelUpRewardContent(
    level: Int,
    coinsEarned: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = "LEVEL UP!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFF3B82F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = level.toString(),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "+$coinsEarned coins",
            color = Color(0xFF34D399)
        )
    }
}

// Chest Reward Screen
@Composable
private fun ChestRewardContent(
    rewardType: String,
    amount: Any,
    expAmount: Int = 0,
    customizationId: Long? = null
) {
    var isOpen by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        val imageRes = if (isOpen) {
            R.drawable.chest_open
        } else {
            R.drawable.chest_closed // ensure this exists in res/drawable
        }

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .clickable { isOpen = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isOpen) {
            val rewardText = mutableListOf<String>()

            // Add coin reward if present
            when (amount) {
                is Int -> if (amount > 0) rewardText.add("+$amount coins")
                is String -> if (!amount.isEmpty()) rewardText.add(amount)
            }

            // Add EXP reward if present
            if (expAmount > 0) {
                rewardText.add("+$expAmount EXP")
            }

            // Note: customization names would require loading the inventory item.
            if (customizationId != null) {
                rewardText.add("Customization unlocked!")
            }

            // Display all rewards
            if (rewardText.isNotEmpty()) {
                Text(
                    text = rewardText.joinToString("\n"),
                    color = Color(0xFFFFD700),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = rewardType,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// Streak Reward Screen
@Composable
private fun StreakRewardContent(
    streak: Int,
    coinsEarned: Int,
    rewardSummary: List<String> = emptyList(),
    onConfirm: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak hearts")
    val mainScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "main heart scale"
    )
    val mainFloat = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "main heart float"
    )
    val displayedSummary = if (rewardSummary.isNotEmpty()) {
        rewardSummary
    } else {
        listOf(
            "$streak Day Streak",
            if (coinsEarned > 0) "+$coinsEarned coins" else "Reward chest unlocked"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onConfirm() },
        contentAlignment = Alignment.Center
    ) {
        HeartOrbit(infiniteTransition)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Streak celebration",
                tint = Color(0xFFFF4D6D),
                modifier = Modifier
                    .size((96 * mainScale.value).dp)
                    .offset(y = mainFloat.value.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "GLOBAL STREAK!",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFFFD166)
            )

            Text(
                text = "$streak DAY STREAK",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            displayedSummary.forEach { rewardLine ->
                Text(
                    text = rewardLine,
                    color = Color(0xFF34D399),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Tap to continue",
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun HeartOrbit(infiniteTransition: InfiniteTransition) {
    repeat(8) { index ->
        val angle = (index * 45).toDouble() * (kotlin.math.PI / 180.0)
        val x = (cos(angle) * 128).dp
        val y = (sin(angle) * 86).dp
        val scale = infiniteTransition.animateFloat(
            initialValue = 0.65f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(700 + index * 70, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbit heart scale $index"
        )
        val alpha = infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(900 + index * 60, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbit heart alpha $index"
        )

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFFFF4D6D).copy(alpha = alpha.value),
            modifier = Modifier
                .offset(x = x, y = y)
                .size((30 * scale.value).dp)
        )
    }
}

// Achievement Reward Screen
@Composable
private fun AchievementRewardContent(
    achievementName: String,
    coinsEarned: Int,
    expAmount: Int = 0,
    chestType: String? = null,
    rewards: List<ConfigAchievementReward> = emptyList()
) {
    val rewardText = if (rewards.isNotEmpty()) {
        rewards.map { it.rewardLabel() }
    } else {
        buildLegacyRewardText(coinsEarned, expAmount, chestType)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ACHIEVEMENT UNLOCKED",
            color = Color.White
        )

        Text(
            text = achievementName,
            color = Color(0xFF818CF8),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = rewardText.joinToString("\n"),
            color = Color(0xFF34D399)
        )
    }
}

private fun buildLegacyRewardText(
    coinsEarned: Int,
    expAmount: Int,
    chestType: String?
): List<String> {
    val rewardText = mutableListOf("+${coinsEarned} coins")

    if (expAmount > 0) {
        rewardText.add("+$expAmount EXP")
    }

    if (!chestType.isNullOrBlank()) {
        val label = chestType.substring(0, 1).uppercase() + chestType.substring(1)
        rewardText.add("$label chest")
    }

    return rewardText
}

private fun ConfigAchievementReward.rewardLabel(): String = when (this) {
    is ConfigAchievementReward.CoinReward -> "+$amount coins"
    is ConfigAchievementReward.ExpReward -> "+$amount EXP"
    is ConfigAchievementReward.ChestReward -> {
        val label = chestType.name.replaceFirstChar { it.uppercase() }
        "$label chest"
    }
    is ConfigAchievementReward.CustomizationReward -> "Customization"
}

// Coin Reward Screen
@Composable
private fun CoinRewardContent(
    amount: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "+$amount",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF34D399)
        )

        Text(
            text = "Coins earned",
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

// Dragon Evolution Reward Screen
@Composable
private fun DragonEvolutionRewardContent(
    pet: PetEntity,
    fromStage: Int,
    toStage: Int,
    onConfirm: () -> Unit
) {
    var isTransitionComplete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clickable {
                if (isTransitionComplete) {
                    onConfirm()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PetPhaseTransition(
            pet = pet.copy(evolutionStage = toStage),
            fromStage = fromStage,
            toStage = toStage,
            size = IntSize(240, 240),
            onTransitionCompleted = { isTransitionComplete = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isTransitionComplete) "Evolution Complete!" else "Watch your dragon evolve",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        if (!isTransitionComplete) {
            Text(
                text = "Tap after the transformation to continue",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }
}

