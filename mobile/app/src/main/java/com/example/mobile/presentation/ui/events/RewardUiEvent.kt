package com.example.mobile.presentation.ui.events

/**
 * UI events for reward popups and dialogs
 * These events are emitted by ViewModels and collected in Compose UI layer
 */
sealed class RewardUiEvent {
    data class CoinReward(
        val amount: Int,
        val tracksChallengeProgress: Boolean = true
    ) : RewardUiEvent()
    data class LevelUpReward(
        val previousLevel: Int,
        val level: Int,
        val coins: Int,
        val tracksChallengeProgress: Boolean = true
    ) : RewardUiEvent()
    data class DragonEvolutionReward(
        val fromStage: Int,
        val toStage: Int,
        val tracksChallengeProgress: Boolean = true
    ) : RewardUiEvent()
    data class StreakReward(
        val streak: Int,
        val coins: Int,
        val rewardSummary: List<String> = emptyList(),
        val tracksChallengeProgress: Boolean = true
    ) : RewardUiEvent()
    data class ExpReward(
        val amount: Long,
        val tracksChallengeProgress: Boolean = true
    ) : RewardUiEvent()
    data class CustomizationReward(
        val equipableId: String,
        val tracksChallengeProgress: Boolean = true
    ) : RewardUiEvent()
    data class ChestReward(
        val rewardType: String,
        val amount: Any,
        val expAmount: Int = 0,
        val customizationId: Long? = null,
        val equipableId: String? = null,
        val tracksChallengeProgress: Boolean = true
    ) : RewardUiEvent()
}

val RewardUiEvent.tracksChallengeProgress: Boolean
    get() = when (this) {
        is RewardUiEvent.CoinReward -> tracksChallengeProgress
        is RewardUiEvent.LevelUpReward -> tracksChallengeProgress
        is RewardUiEvent.DragonEvolutionReward -> tracksChallengeProgress
        is RewardUiEvent.StreakReward -> tracksChallengeProgress
        is RewardUiEvent.ExpReward -> tracksChallengeProgress
        is RewardUiEvent.CustomizationReward -> tracksChallengeProgress
        is RewardUiEvent.ChestReward -> tracksChallengeProgress
    }

fun RewardUiEvent.rewardPriority(): Int = when (this) {
    is RewardUiEvent.LevelUpReward -> 1
    is RewardUiEvent.DragonEvolutionReward -> 2
    is RewardUiEvent.StreakReward -> 3
    is RewardUiEvent.ChestReward -> 4
    is RewardUiEvent.ExpReward -> 5
    is RewardUiEvent.CustomizationReward -> 6
    is RewardUiEvent.CoinReward -> 7
}
