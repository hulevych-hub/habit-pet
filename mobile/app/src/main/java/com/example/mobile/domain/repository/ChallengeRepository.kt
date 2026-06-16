package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.ChallengeEntity
import com.example.mobile.domain.ChallengeRewardDefinition
import kotlinx.coroutines.flow.Flow

data class ChallengeUiState(
    val challenge: ChallengeEntity?,
    val progress: Int,
    val target: Int,
    val progressFraction: Float,
    val progressLabel: String,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val rewards: List<ChallengeRewardDefinition>
) {
    val isEmpty: Boolean = challenge == null

    companion object {
        fun empty(): ChallengeUiState = ChallengeUiState(
            challenge = null,
            progress = 0,
            target = 1,
            progressFraction = 0f,
            progressLabel = "0/1",
            isCompleted = false,
            isClaimed = false,
            rewards = emptyList()
        )
    }
}

data class ChallengeClaimResult(
    val challenge: ChallengeEntity,
    val rewards: List<ChallengeRewardDefinition>
)

interface ChallengeRepository {
    fun getActiveChallengeUiState(): Flow<ChallengeUiState>
    suspend fun ensureActiveChallenge()
    suspend fun recordHabitCompleted(habitId: Long, xpEarned: Long, coinsEarned: Int)
    suspend fun recordXpEarned(amount: Long)
    suspend fun recordCoinsEarned(amount: Int)
    suspend fun recordChestOpened()
    suspend fun recordCustomizationUnlocked(equipableId: String)
    suspend fun recordCustomizationEquipped(type: String, itemId: String)
    suspend fun recordStreak(streak: Int)
    suspend fun claimActiveChallenge(): ChallengeClaimResult
    suspend fun reset()
}
