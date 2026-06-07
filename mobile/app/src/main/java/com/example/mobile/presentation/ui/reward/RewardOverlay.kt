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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobile.R
import com.example.mobile.presentation.ui.events.RewardUiEvent

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

                    is RewardUiEvent.StreakReward -> StreakRewardContent(
                        streak = reward.streak,
                        coinsEarned = reward.coins,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.AchievementReward -> AchievementRewardContent(
                        achievementName = reward.achievementName,
                        coinsEarned = reward.coins,
                        onConfirm = onDismiss
                    )

                    is RewardUiEvent.ChestReward -> ChestRewardContent(
                        rewardType = reward.rewardType,
                        amount = reward.amount,
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
private fun AchievementRewardContent(
    achievementName: String,
    coinsEarned: Int,
    onConfirm: () -> Unit
) {
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

        Text("+$coinsEarned coins")

        Button(onClick = onConfirm) {
            Text("Claim")
        }
    }
}

@Composable
private fun ChestRewardContent(
    rewardType: String,
    amount: Any,
    onConfirm: () -> Unit
) {
    var isOpen by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        val chestImage = if (isOpen)
            R.drawable.chest_open
        else
            R.drawable.chest_closed

        Image(
            painter = painterResource(id = chestImage),
            contentDescription = "Chest",
            modifier = Modifier
                .size(80.dp)
                .clickable { isOpen = true }
        )

        if (isOpen) {
            Text("Chest Reward!")
            Text("Type: $rewardType")

            val text = when (amount) {
                is Int -> "+$amount coins"
                is String -> amount
                else -> "Unknown"
            }

            Text(text)

            Button(onClick = onConfirm) {
                Text("Collect")
            }
        }
    }
}