package com.example.mobile.presentation.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardManager @Inject constructor(
    private val rewardEventBus: RewardEventBus,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _rewardQueue = MutableSharedFlow<RewardUiEvent>(replay = 0)
    val rewardQueue: SharedFlow<RewardUiEvent> = _rewardQueue.shareIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 0
    )

    private val _currentReward = MutableStateFlow<RewardUiEvent?>(null)
    val currentReward: StateFlow<RewardUiEvent?> = _currentReward.asStateFlow()

    private val _isDisplayingReward = MutableStateFlow(false)
    val isDisplayingReward: StateFlow<Boolean> = _isDisplayingReward.asStateFlow()

    init {
        viewModelScope.launch {
            rewardEventBus.rewardEvents.collect { rewardEvent ->

                while (_isDisplayingReward.value) {
                    delay(100)
                }

                _currentReward.value = rewardEvent
                _isDisplayingReward.value = true
            }
        }
    }

    fun addReward(event: RewardUiEvent) {
        viewModelScope.launch {
            _rewardQueue.tryEmit(event)
        }
    }

    fun rewardCompleted() {
        val current = _currentReward.value ?: return

        viewModelScope.launch {
            val coinsToAdd = when (current) {

                is RewardUiEvent.CoinReward -> current.amount

                is RewardUiEvent.LevelUpReward -> current.coins

                is RewardUiEvent.StreakReward -> current.coins

                is RewardUiEvent.AchievementReward -> current.coins

                is RewardUiEvent.ChestReward -> {
                    when (val amount = current.amount) {
                        is Int -> amount
                        else -> 0
                    }
                }
            }

            if (coinsToAdd > 0) {
                statisticsRepository.addCoins(coinsToAdd)
            }

            _currentReward.value = null
            _isDisplayingReward.value = false
        }
    }
}