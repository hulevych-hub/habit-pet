package com.example.mobile.presentation.ui.events

/**
 * UI events for reward popups and dialogs
 * These events are emitted by ViewModels and collected in Compose UI layer
 */
sealed class RewardUiEvent {
    data class CoinReward(val amount: Int) : RewardUiEvent()
    data class LevelUpReward(val level: Int, val coins: Int) : RewardUiEvent()
    data class DragonEvolutionReward(val fromStage: Int, val toStage: Int) : RewardUiEvent()
    data class StreakReward(val streak: Int, val coins: Int) : RewardUiEvent()
    data class AchievementReward(
        val achievementName: String,
        val coins: Int,
        val expAmount: Int = 0,
        val chestType: String? = null
    ) : RewardUiEvent()
    data class ChestReward(
        val rewardType: String,
        val amount: Any,
        val expAmount: Int = 0,
        val accessoryId: Long? = null
    ) : RewardUiEvent()
}