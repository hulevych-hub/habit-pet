package com.example.mobile.presentation.ui.reward

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

                is RewardUiEvent.ChestReward -> ChestRewardContent(
                    rewardType = reward.rewardType,
                    amount = reward.amount
                )

                is RewardUiEvent.StreakReward -> StreakRewardContent(
                    streak = reward.streak,
                    coinsEarned = reward.coins
                )

                is RewardUiEvent.AchievementReward -> AchievementRewardContent(
                    achievementName = reward.achievementName,
                    coinsEarned = reward.coins
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
    amount: Any
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
            Text(
                text = when (amount) {
                    is Int -> "+$amount coins"
                    is String -> amount
                    else -> amount.toString()
                },
                color = Color(0xFFFFD700),
                style = MaterialTheme.typography.titleLarge
            )

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
    coinsEarned: Int
) {
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
            text = "+$coinsEarned coins",
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