package com.example.mobile.presentation.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardManager @Inject constructor(
    private val rewardQueue: RewardQueue,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _currentReward = MutableStateFlow<RewardUiEvent?>(null)
    val currentReward: StateFlow<RewardUiEvent?> = _currentReward

    private val _isDisplayingReward = MutableStateFlow(false)
    val isDisplayingReward: StateFlow<Boolean> = _isDisplayingReward

    init {
        viewModelScope.launch {
            rewardQueue.rewardEvents.collect { reward ->
                _currentReward.value = reward
                _isDisplayingReward.value = true
            }
        }
    }

    fun addReward(event: RewardUiEvent) {
        rewardQueue.addReward(event)
    }

    fun rewardCompleted() {
        val current = _currentReward.value ?: return

        viewModelScope.launch {

            val coinsToAdd = when (current) {
                is RewardUiEvent.CoinReward -> current.amount
                is RewardUiEvent.LevelUpReward -> current.coins
                is RewardUiEvent.StreakReward -> current.coins
                is RewardUiEvent.AchievementReward -> current.coins
                is RewardUiEvent.ChestReward -> (current.amount as? Int) ?: 0
            }

            if (coinsToAdd > 0) {
                statisticsRepository.addCoins(coinsToAdd)
            }

            // IMPORTANT: clear UI FIRST
            _currentReward.value = null
            _isDisplayingReward.value = false

            // THEN advance queue AFTER UI is dismissed
            rewardQueue.rewardDismissed()
        }
    }
}