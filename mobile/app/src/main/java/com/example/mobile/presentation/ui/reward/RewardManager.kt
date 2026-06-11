package com.example.mobile.presentation.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ChestRewardConfigProvider
import com.example.mobile.domain.ChestType
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

            if (coinsToAdd > 0) {
                statisticsRepository.addCoins(coinsToAdd)
            }

            when (current) {
                is RewardUiEvent.AchievementReward -> {
                    if (current.expAmount > 0) {
                        addPetExp(current.expAmount)
                    }

                    current.chestType?.let { chestType ->
                        rewardQueue.addReward(buildChestReward(chestType))
                    }
                }

                is RewardUiEvent.ChestReward -> {
                    if (current.expAmount > 0) {
                        addPetExp(current.expAmount)
                    }

                    current.accessoryId?.let { accessoryId ->
                        inventoryItemRepository.grantItem(accessoryId)
                    }
                }

                else -> Unit
            }

            _currentReward.value = null
            _isDisplayingReward.value = false

            rewardQueue.rewardDismissed()
        }
    }

    private suspend fun addPetExp(expAmount: Int) {
        val currentPet = petRepository.getPet().firstOrNull() ?: PetEntity(id = 1)
        val updatedPet = currentPet.copy(xp = currentPet.xp + expAmount)
        petRepository.updatePet(updatedPet)
    }

    private suspend fun buildChestReward(chestTypeValue: String): RewardUiEvent.ChestReward {
        val chestType = ChestType.values()
            .firstOrNull { it.name.equals(chestTypeValue, ignoreCase = true) }
            ?: ChestType.NORMAL

        val config = ChestRewardConfigProvider.getConfig(chestType)
        var coinAmount = config.getRandomCoins()
        var expAmount = config.getRandomExp()
        var accessoryId: Long? = null

        if (config.accessoryRarity != null && Math.random() < config.accessoryDropChance) {
            val unownedItems = inventoryItemRepository.getUnownedItemsByType(config.accessoryRarity.name)
                .firstOrNull()
                ?.toList()
                .orEmpty()

            if (unownedItems.isNotEmpty()) {
                val selectedItem = unownedItems.random()
                if (inventoryItemRepository.grantItem(selectedItem.id) == 1) {
                    accessoryId = selectedItem.id
                }
            }
        }

        return RewardUiEvent.ChestReward(
            rewardType = "achievement_${chestType.name.lowercase()}",
            amount = coinAmount,
            expAmount = expAmount,
            accessoryId = accessoryId
        )
    }
}