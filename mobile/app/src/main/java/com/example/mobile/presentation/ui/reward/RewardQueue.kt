package com.example.mobile.presentation.ui.reward

import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardQueue @Inject constructor(
    private val rewardEventBus: RewardEventBus
) {

    private var isDisplaying = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val rewardPriorities = mapOf(
        RewardUiEvent.LevelUpReward::class.java to 1,
        RewardUiEvent.StreakReward::class.java to 2,
        RewardUiEvent.ChestReward::class.java to 3,
        //RewardUiEvent.CoinReward::class.java to 4,
        RewardUiEvent.AchievementReward::class.java to 5
    )

    private val rewardDelays = mapOf(
        RewardUiEvent.LevelUpReward::class.java to 2500L,
        RewardUiEvent.StreakReward::class.java to 1500L,
        RewardUiEvent.ChestReward::class.java to 3000L,
        //RewardUiEvent.CoinReward::class.java to 1000L,
        RewardUiEvent.AchievementReward::class.java to 2000L
    )

    private val rewardBuffer = mutableListOf<RewardUiEvent>()

    private var isProcessing = false

    fun addReward(event: RewardUiEvent) {
        rewardBuffer.add(event)

        rewardBuffer.sortBy {
            rewardPriorities[it::class.java] ?: Int.MAX_VALUE
        }

        if (!isProcessing) {
            processQueue()
        }
    }

    private fun processQueue() {
        scope.launch {

            isProcessing = true

            while (rewardBuffer.isNotEmpty()) {

                val reward = rewardBuffer.removeAt(0)

                rewardEventBus.emit(reward)
                isDisplaying = true

                delay(
                    rewardDelays[reward::class.java] ?: 1000L
                )
            }

            isProcessing = false
        }
    }

    fun rewardDismissed() {
        isDisplaying = false

        if (rewardBuffer.isNotEmpty()) {
            processQueue()
        }
    }
}