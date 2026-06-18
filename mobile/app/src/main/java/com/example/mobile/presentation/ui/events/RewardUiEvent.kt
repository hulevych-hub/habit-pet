package com.example.mobile.presentation.ui.events

import com.example.mobile.domain.AchievementReward as ConfigAchievementReward

/**
 * UI events for reward popups and dialogs
 * These events are emitted by ViewModels and collected in Compose UI layer
 */
sealed class RewardUiEvent {
    data class CoinReward(val amount: Int) : RewardUiEvent()
    data class LevelUpReward(
        val previousLevel: Int,
        val level: Int,
        val coins: Int
    ) : RewardUiEvent()
    data class DragonEvolutionReward(val fromStage: Int, val toStage: Int) : RewardUiEvent()
    data class StreakReward(
        val streak: Int,
        val coins: Int,
        val rewardSummary: List<String> = emptyList()
    ) : RewardUiEvent()
    data class ExpReward(val amount: Long) : RewardUiEvent()
    data class CustomizationReward(val equipableId: String) : RewardUiEvent()
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
        val customizationId: Long? = null,
        val equipableId: String? = null
    ) : RewardUiEvent()
}

fun RewardUiEvent.rewardPriority(): Int = when (this) {
    is RewardUiEvent.LevelUpReward -> 1
    is RewardUiEvent.DragonEvolutionReward -> 2
    is RewardUiEvent.StreakReward -> 3
    is RewardUiEvent.ChestReward -> 4
    is RewardUiEvent.AchievementReward -> 5
    is RewardUiEvent.ExpReward -> 6
    is RewardUiEvent.CustomizationReward -> 7
    is RewardUiEvent.CoinReward -> 8
}
