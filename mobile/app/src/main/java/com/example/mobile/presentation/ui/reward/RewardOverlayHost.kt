package com.example.mobile.presentation.ui.reward

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun RewardOverlayHost(
    rewardManager: RewardManager,
    onRewardCompleted: () -> Unit
) {
    val currentReward by rewardManager.currentReward.collectAsState()
    val isDisplaying by rewardManager.isDisplayingReward.collectAsState()

    if (isDisplaying && currentReward != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onRewardCompleted() }
        ) {
            RewardScreen(
                reward = currentReward,
                onRewardCompleted = onRewardCompleted
            )
        }
    }
}