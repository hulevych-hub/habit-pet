package com.example.mobile.presentation.ui.reward

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile.domain.AchievementReward as ConfigAchievementReward
import com.example.mobile.presentation.ui.components.rememberAssetPainter
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.util.AssetResolver

@Composable
fun RewardOverlay(
    rewardManager: RewardManager,
    onDismiss: () -> Unit
) {
    val currentReward by rewardManager.currentReward.collectAsState()
    val isDisplaying by rewardManager.isDisplayingReward.collectAsState()

    val dismissReward = {
        rewardManager.rewardCompleted()
        onDismiss()
    }

    if (isDisplaying && currentReward != null) {
        RewardDialog(
            reward = currentReward!!,
            onDismiss = dismissReward
        )
    }
}

@Composable
private fun RewardDialog(
    reward: RewardUiEvent,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(280.dp)
                    .padding(24.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                when (reward) {
                    is RewardUiEvent.CoinReward -> CoinRewardContent(
                        amount = reward.amount,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.LevelUpReward -> LevelUpRewardContent(
                        level = reward.level,
                        coinsEarned = reward.coins,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.DragonEvolutionReward -> DragonEvolutionRewardContent(
                        fromStage = reward.fromStage,
                        toStage = reward.toStage,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.StreakReward -> StreakRewardContent(
                        streak = reward.streak,
                        coinsEarned = reward.coins,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.ExpReward -> ExpRewardContent(
                        amount = reward.amount,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.CustomizationReward -> CustomizationRewardContent(
                        equipableId = reward.equipableId,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.AchievementReward -> AchievementRewardContent(
                        achievementName = reward.achievementName,
                        coinsEarned = reward.coins,
                        expAmount = reward.expAmount,
                        chestType = reward.chestType,
                        rewards = reward.rewards,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.ChestReward -> ChestRewardContent(
                        rewardType = reward.rewardType,
                        amount = reward.amount,
                        expAmount = reward.expAmount,
                        customizationId = reward.customizationId,
                        onConfirm = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun CoinRewardContent(
    amount: Int,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = "Coin",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "+$amount",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Coins Earned!")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onConfirm) {
            Text("Collect")
        }
    }
}

@Composable
private fun LevelUpRewardContent(
    level: Int,
    coinsEarned: Int,
    onConfirm: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Emergency,
            contentDescription = "Level Up",
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.secondary
        )

        Text("Level $level")
        Text("+$coinsEarned coins")

        Button(onClick = onConfirm) {
            Text("Claim")
        }
    }
}

@Composable
private fun StreakRewardContent(
    streak: Int,
    coinsEarned: Int,
    onConfirm: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Streak",
            tint = Color.Red
        )

        Text("$streak Day Streak")
        Text("+$coinsEarned coins")

        Button(onClick = onConfirm) {
            Text("Claim")
        }
    }
}

@Composable
private fun ExpRewardContent(
    amount: Long,
    onConfirm: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "EXP",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )

        Text("+${amount} EXP")
        Text("XP Boost!")

        Button(onClick = onConfirm) {
            Text("Claim")
        }
    }
}

@Composable
private fun CustomizationRewardContent(
    equipableId: String,
    onConfirm: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = "Customization",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )

        Text("Customization Unlocked!")
        Text(equipableId)

        Button(onClick = onConfirm) {
            Text("Claim")
        }
    }
}

@Composable
private fun AchievementRewardContent(
    achievementName: String,
    coinsEarned: Int,
    expAmount: Int = 0,
    chestType: String? = null,
    rewards: List<ConfigAchievementReward> = emptyList(),
    onConfirm: () -> Unit
) {
    val rewardText = if (rewards.isNotEmpty()) {
        rewards.map { it.rewardLabel() }
    } else {
        buildLegacyRewardText(coinsEarned, expAmount, chestType)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Achievement",
            tint = MaterialTheme.colorScheme.tertiary
        )

        Text("Achievement Unlocked!")

        Text(
            text = achievementName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(rewardText.joinToString("\n"))

        Button(onClick = onConfirm) {
            Text("Claim")
        }
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

@Composable
private fun ChestRewardContent(
    rewardType: String,
    amount: Any,
    expAmount: Int = 0,
    customizationId: Long? = null,
    onConfirm: () -> Unit
) {
    var isOpen by remember { mutableStateOf(false) }
    val assetManager = LocalContext.current.assets
    val chestAssetPath = remember(isOpen) {
        AssetResolver.assetPath(assetManager, "", if (isOpen) "chest_open" else "chest_closed")
    }
    val chestPainter = rememberAssetPainter(chestAssetPath, "chest")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        if (chestPainter != null) {
            Image(
                painter = chestPainter,
                contentDescription = "Chest",
                modifier = Modifier
                    .size(80.dp)
                    .clickable { isOpen = true }
            )
        }

        if (isOpen) {
            Text("Chest Reward!")
            Text("Type: $rewardType")

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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(onClick = onConfirm) {
                Text("Collect")
            }
        }
    }
}

@Composable
private fun DragonEvolutionRewardContent(
    fromStage: Int,
    toStage: Int,
    onConfirm: () -> Unit
) {
    var isEvolved by remember { mutableStateOf(false) }

    val assetManager = LocalContext.current.assets
    val displayedStage = if (isEvolved) toStage else fromStage
    val dragonAssetPath = remember(displayedStage) {
        AssetResolver.defaultAssetPath(assetManager, displayedStage)
    }
    val dragonPainter = rememberAssetPainter(dragonAssetPath, "dragon evolution")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
        if (dragonPainter != null) {
            Image(
                painter = dragonPainter,
                contentDescription = if (isEvolved) "Dragon evolved to stage $toStage" else "Dragon stage $fromStage",
                modifier = Modifier
                    .size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isEvolved) "Evolution Complete!" else "Tap to evolve",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!isEvolved) {
            Text(
                text = "Your pet is transforming...",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }
}