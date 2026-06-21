package com.example.mobile.domain

import com.example.mobile.domain.repository.ChallengeClaimResult
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.ChallengeUiState
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChallengeEngine @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val inventoryItemRepository: InventoryItemRepository,
    private val activityTimelineEngine: ActivityTimelineEngine,
    private val rewardQueue: RewardQueue
) {
    fun getActiveChallengeUiState(): Flow<ChallengeUiState> =
        challengeRepository.getActiveChallengeUiState()

    suspend fun ensureActiveChallenge() {
        challengeRepository.ensureActiveChallenge()
    }

    suspend fun recordHabitCompleted(habitId: Long, xpEarned: Long, coinsEarned: Int) {
        challengeRepository.recordHabitCompleted(habitId, xpEarned, coinsEarned)
    }

    suspend fun recordXpEarned(amount: Long) {
        challengeRepository.recordXpEarned(amount)
    }

    suspend fun recordCoinsEarned(amount: Int) {
        challengeRepository.recordCoinsEarned(amount)
    }

    suspend fun recordChestOpened() {
        challengeRepository.recordChestOpened()
    }

    suspend fun recordCustomizationUnlocked(equipableId: String) {
        challengeRepository.recordCustomizationUnlocked(equipableId)
    }

    suspend fun recordCustomizationEquipped(type: String, itemId: String) {
        challengeRepository.recordCustomizationEquipped(type, itemId)
    }

    suspend fun recordStreak(streak: Int) {
        challengeRepository.recordStreak(streak)
    }

    suspend fun reset() {
        challengeRepository.reset()
    }

    suspend fun claimActiveChallenge(): ChallengeClaimResult {
        val result = challengeRepository.claimActiveChallenge()
        if (result.rewards.isEmpty()) return result

        result.rewards.forEach { reward ->
            when (reward) {
                is ChallengeRewardDefinition.CoinReward -> {
                    rewardQueue.addReward(RewardUiEvent.CoinReward(reward.amount, tracksChallengeProgress = false))
                }
                is ChallengeRewardDefinition.ExpReward -> {
                    rewardQueue.addReward(RewardUiEvent.ExpReward(reward.amount, tracksChallengeProgress = false))
                }
                is ChallengeRewardDefinition.ChestReward -> {
                    val chestType = ChestType.values().firstOrNull {
                        it.name.equals(reward.chestType, ignoreCase = true)
                    } ?: ChestType.NORMAL
                    rewardQueue.addReward(
                        ChestRewardFactory.buildChestReward(
                            rewardType = "challenge_${chestType.name.lowercase()}",
                            chestType = chestType,
                            inventoryItemRepository = inventoryItemRepository,
                            tracksChallengeProgress = false
                        )
                    )
                }
                is ChallengeRewardDefinition.CustomizationReward -> {
                    rewardQueue.addReward(RewardUiEvent.CustomizationReward(reward.equipableId, tracksChallengeProgress = false))
                }
            }
        }

        activityTimelineEngine.logChallengeCompleted(
            challengeName = result.challenge.title,
            rewards = result.rewards
        )

        return result
    }
}
