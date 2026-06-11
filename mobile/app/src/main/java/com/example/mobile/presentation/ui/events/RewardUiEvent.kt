package com.example.mobile.presentation.ui.events

import com.example.mobile.domain.AchievementReward as ConfigAchievementReward

/**
 * UI events for reward popups and dialogs
 * These events are emitted by ViewModels and collected in Compose UI layer
 */
sealed class RewardUiEvent {
    data class CoinReward(val amount: Int) : RewardUiEvent()
    data class LevelUpReward(val level: Int, val coins: Int) : RewardUiEvent()
    data class DragonEvolutionReward(val fromStage: Int, val toStage: Int) : RewardUiEvent()
    data class StreakReward(
        val streak: Int,
        val coins: Int,
        val rewardSummary: List<String> = emptyList()
    ) : RewardUiEvent()
    data class DailyGoalReward(
        val goalXp: Long,
        val bonusCoins: Int,
        val bonusExp: Long
    ) : RewardUiEvent()
    data class AchievementReward(
        val achievementName: String,
        val rewards: List<ConfigAchievementReward> = emptyList(),
        val coins: Int = rewards.sumOf { reward ->
            (reward as? ConfigAchievementReward.CoinReward)?.amount ?: 0
        },
        val expAmount: Int = rewards.sumOf { reward ->
            (reward as? ConfigAchievementReward.ExpReward)?.amount ?: 0
        },
        val chestType: String? = rewards
            .filterIsInstance<ConfigAchievementReward.ChestReward>()
            .firstOrNull()
            ?.chestType
            ?.name
            ?.lowercase()
    ) : RewardUiEvent()
    data class ChestReward(
        val rewardType: String,
        val amount: Any,
        val expAmount: Int = 0,
        val customizationId: Long? = null
    ) : RewardUiEvent()
}