package com.example.mobile.presentation.ui.reward

import com.example.mobile.presentation.ui.events.RewardUiEvent
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

    private val rewardPriorities = mapOf(
        RewardUiEvent.LevelUpReward::class.java to 1,
        RewardUiEvent.StreakReward::class.java to 2,
        RewardUiEvent.ChestReward::class.java to 3,
        RewardUiEvent.AchievementReward::class.java to 4
    )

    private val buffer = mutableListOf<RewardUiEvent>()

    private val _rewardEvents = MutableSharedFlow<RewardUiEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    val rewardEvents = _rewardEvents

    private var isEmitting = false

    fun addReward(event: RewardUiEvent) {
        buffer.add(event)

        buffer.sortBy {
            rewardPriorities[it::class.java] ?: Int.MAX_VALUE
        }

        emitNextIfPossible()
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