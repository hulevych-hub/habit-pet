package com.example.mobile.presentation.ui.reward

import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.events.rewardPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardQueue @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val lock = Any()

    private val buffer = mutableListOf<RewardUiEvent>()

    private val _rewardEvents = MutableSharedFlow<RewardUiEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    val rewardEvents = _rewardEvents

    private var isEmitting = false

    fun addReward(event: RewardUiEvent) {
        val nextReward = synchronized(lock) {
            buffer.add(event)

            buffer.sortBy { it.rewardPriority() }
            mergePendingRewards()

            takeNextRewardIfIdleLocked()
        }

        nextReward?.let { emitReward(it) }
    }

    private fun mergePendingRewards() {
        val mergedRewards = mutableListOf<RewardUiEvent>()

        buffer.forEach { reward ->
            val existingIndex = mergedRewards.indexOfFirst { canMergeRewards(it, reward) }
            if (existingIndex >= 0) {
                mergedRewards[existingIndex] = mergeRewards(mergedRewards[existingIndex], reward)!!
            } else {
                mergedRewards.add(reward)
            }
        }

        buffer.clear()
        buffer.addAll(mergedRewards)
    }

    fun mergeNextRewardIfPossible(current: RewardUiEvent): RewardUiEvent {
        return synchronized(lock) {
            var merged = current
            var didMerge = false
            val remainingRewards = mutableListOf<RewardUiEvent>()

            buffer.forEach { reward ->
                val mergedReward = mergeRewards(merged, reward)
                if (mergedReward != null) {
                    merged = mergedReward
                    didMerge = true
                } else {
                    remainingRewards.add(reward)
                }
            }

            if (!didMerge) return@synchronized current

            buffer.clear()
            buffer.addAll(remainingRewards)
            buffer.sortBy { it.rewardPriority() }
            mergePendingRewards()

            merged
        }
    }

    private fun canMergeRewards(left: RewardUiEvent, right: RewardUiEvent): Boolean =
        (left is RewardUiEvent.CoinReward && right is RewardUiEvent.CoinReward) ||
            (left is RewardUiEvent.ExpReward && right is RewardUiEvent.ExpReward)

    private fun mergeRewards(left: RewardUiEvent, right: RewardUiEvent): RewardUiEvent? = when {
        left is RewardUiEvent.CoinReward && right is RewardUiEvent.CoinReward -> {
            left.copy(
                amount = left.amount + right.amount,
                tracksChallengeProgress = left.tracksChallengeProgress && right.tracksChallengeProgress
            )
        }
        left is RewardUiEvent.ExpReward && right is RewardUiEvent.ExpReward -> {
            left.copy(
                amount = left.amount + right.amount,
                tracksChallengeProgress = left.tracksChallengeProgress && right.tracksChallengeProgress
            )
        }
        else -> null
    }

    fun emitNextIfPossible() {
        val nextReward = synchronized(lock) {
            takeNextRewardIfIdleLocked()
        }

        nextReward?.let { emitReward(it) }
    }

    fun rewardDismissed() {
        val nextReward = synchronized(lock) {
            isEmitting = false
            takeNextRewardIfIdleLocked()
        }

        nextReward?.let { emitReward(it) }
    }

    private fun takeNextRewardIfIdleLocked(): RewardUiEvent? {
        if (isEmitting) return null
        if (buffer.isEmpty()) return null

        isEmitting = true
        return buffer.removeAt(0)
    }

    private fun emitReward(reward: RewardUiEvent) {
        scope.launch {
            _rewardEvents.emit(reward)
        }
    }
}