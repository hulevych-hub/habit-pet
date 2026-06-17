package com.example.mobile.presentation.ui.reward

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.ui.theme.AppTheme

@Composable
fun RewardOverlayHost(
    rewardManager: RewardManager,
    onRewardCompleted: () -> Unit
) {
    val currentReward by rewardManager.currentReward.collectAsState()
    val currentPet by rewardManager.currentPet.collectAsState()
    val isDisplaying by rewardManager.isDisplayingReward.collectAsState()

    if (isDisplaying && currentReward != null) {
        val backgroundModifier = if (currentReward is RewardUiEvent.DragonEvolutionReward) {
            Modifier
        } else {
            Modifier.clickable { onRewardCompleted() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.current.overlayBackground)
                .then(backgroundModifier)
        ) {
            RewardScreen(
                reward = currentReward,
                pet = currentPet,
                onRewardCompleted = onRewardCompleted
            )
        }
    }
}