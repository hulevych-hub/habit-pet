package com.example.mobile.presentation.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardManager @Inject constructor(
    private val rewardQueue: RewardQueue,
    private val statisticsRepository: StatisticsRepository,
    private val petRepository: PetRepository,
    private val inventoryItemRepository: InventoryItemRepository
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
                is RewardUiEvent.DragonEvolutionReward -> 0
                is RewardUiEvent.StreakReward -> current.coins
                is RewardUiEvent.AchievementReward -> current.coins
                is RewardUiEvent.ChestReward -> (current.amount as? Int) ?: 0
            }

            // Add coins if any
            if (coinsToAdd > 0) {
                statisticsRepository.addCoins(coinsToAdd)
            }

            // Handle EXP and accessory rewards for ChestReward
            if (current is RewardUiEvent.ChestReward) {
                val chestReward = current

                // Add EXP if any
                if (chestReward.expAmount > 0) {
                    // Get current pet, add EXP, and update
                    val currentPet = petRepository.getPet().firstOrNull() ?: PetEntity(id = 1)
                    val updatedPet = currentPet.copy(xp = currentPet.xp + chestReward.expAmount)
                    petRepository.updatePet(updatedPet)
                }

                // Grant accessory if any
                chestReward.accessoryId?.let { accessoryId ->
                    val grantResult = inventoryItemRepository.grantItem(accessoryId)
                    // Note: We don't handle the result here, but in a production app we might want to
                    // log or handle cases where granting fails (e.g., item already owned)
                }
            }

            // IMPORTANT: clear UI FIRST
            _currentReward.value = null
            _isDisplayingReward.value = false

            // THEN advance queue AFTER UI is dismissed
            rewardQueue.rewardDismissed()
        }
    }
}