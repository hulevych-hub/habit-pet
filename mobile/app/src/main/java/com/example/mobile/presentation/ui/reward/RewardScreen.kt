package com.example.mobile.presentation.ui.reward

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile.R
import com.example.mobile.presentation.ui.events.RewardUiEvent

@Composable
fun RewardScreen(
    reward: RewardUiEvent?,
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
                    fromStage = reward.fromStage,
                    toStage = reward.toStage,
                    onConfirm = onRewardCompleted
                )

                is RewardUiEvent.StreakReward -> StreakRewardContent(
                    streak = reward.streak,
                    coinsEarned = reward.coins
                )

                is RewardUiEvent.ChestReward -> ChestRewardContent(
                    rewardType = reward.rewardType,
                    amount = reward.amount,
                    expAmount = reward.expAmount,
                    accessoryId = reward.accessoryId
                )

                is RewardUiEvent.AchievementReward -> AchievementRewardContent(
                    achievementName = reward.achievementName,
                    coinsEarned = reward.coins,
                    expAmount = reward.expAmount,
                    chestType = reward.chestType
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
    accessoryId: Long? = null
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

            // Note: Accessory rewards would require accessing the inventory to get the name
            // For now, we'll show a generic message if an accessory is included
            if (accessoryId != null) {
                rewardText.add("Accessory!")
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
    coinsEarned: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = "🔥 $streak DAY STREAK!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "+$coinsEarned coins",
            color = Color(0xFF34D399)
        )
    }
}

// Achievement Reward Screen
@Composable
private fun AchievementRewardContent(
    achievementName: String,
    coinsEarned: Int,
    expAmount: Int = 0,
    chestType: String? = null
) {
    val rewardText = mutableListOf("+${coinsEarned} coins")

    if (expAmount > 0) {
        rewardText.add("+$expAmount EXP")
    }

    if (!chestType.isNullOrBlank()) {
        val label = chestType.substring(0, 1).uppercase() + chestType.substring(1)
        rewardText.add("$label chest")
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
    fromStage: Int,
    toStage: Int,
    onConfirm: () -> Unit
) {
    var isEvolved by remember { mutableStateOf(false) }

    // Map stage numbers to drawable resources (using actual asset names)
    val fromImageRes = when (fromStage) {
        0 -> R.drawable.egg
        1 -> R.drawable.hatchling
        2 -> R.drawable.young_dragon
        3 -> R.drawable.adult_dragon
        4 -> R.drawable.ancient_dragon
        else -> R.drawable.egg // fallback
    }

    val toImageRes = when (toStage) {
        0 -> R.drawable.egg
        1 -> R.drawable.hatchling
        2 -> R.drawable.young_dragon
        3 -> R.drawable.adult_dragon
        4 -> R.drawable.ancient_dragon
        else -> R.drawable.ancient_dragon // fallback
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clickable {
                if (isEvolved) {
                    onConfirm()
                } else {
                    isEvolved = true
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(if (isEvolved) toImageRes else fromImageRes),
            contentDescription = if (isEvolved) "Dragon evolved to stage $toStage" else "Dragon stage $fromStage",
            modifier = Modifier
                .size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isEvolved) "Evolution Complete!" else "Tap to evolve",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        if (!isEvolved) {
            Text(
                text = "Your pet is transforming...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }
}

