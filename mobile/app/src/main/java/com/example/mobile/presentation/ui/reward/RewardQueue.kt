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

    private val buffer = mutableListOf<RewardUiEvent>()

    private val _rewardEvents = MutableSharedFlow<RewardUiEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    val rewardEvents = _rewardEvents

    private var isEmitting = false

    fun addReward(event: RewardUiEvent) {
        buffer.add(event)

        buffer.sortBy { it.rewardPriority() }
        mergePendingRewards()

        emitNextIfPossible()
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

        if (!didMerge) return current

        buffer.clear()
        buffer.addAll(remainingRewards)
        buffer.sortBy { it.rewardPriority() }
        mergePendingRewards()

        return merged
    }

    private fun canMergeRewards(left: RewardUiEvent, right: RewardUiEvent): Boolean =
        (left is RewardUiEvent.CoinReward && right is RewardUiEvent.CoinReward) ||
            (left is RewardUiEvent.ExpReward && right is RewardUiEvent.ExpReward)

    private fun mergeRewards(left: RewardUiEvent, right: RewardUiEvent): RewardUiEvent? = when {
        left is RewardUiEvent.CoinReward && right is RewardUiEvent.CoinReward -> {
            left.copy(amount = left.amount + right.amount)
        }
        left is RewardUiEvent.ExpReward && right is RewardUiEvent.ExpReward -> {
            left.copy(amount = left.amount + right.amount)
        }
        else -> null
    }

    fun emitNextIfPossible() {
        if (isEmitting) return
        if (buffer.isEmpty()) return

        val next = buffer.removeAt(0)

        isEmitting = true

        scope.launch {
            _rewardEvents.emit(next)
        }
    }

    fun rewardDismissed() {
        isEmitting = false
        emitNextIfPossible()
    }
}