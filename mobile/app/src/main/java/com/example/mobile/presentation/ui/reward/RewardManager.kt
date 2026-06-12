package com.example.mobile.presentation.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.feedback.MicroFeedbackManager
import com.example.mobile.presentation.ui.reward.RewardEventBus
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
    private val inventoryItemRepository: InventoryItemRepository,
    private val rewardEventBus: RewardEventBus,
    private val activityTimelineEngine: ActivityTimelineEngine,
    private val microFeedbackManager: MicroFeedbackManager
) : ViewModel() {

    private val _currentReward = MutableStateFlow<RewardUiEvent?>(null)
    val currentReward: StateFlow<RewardUiEvent?> = _currentReward

    private val _currentPet = MutableStateFlow(PetEntity(id = 1))
    val currentPet: StateFlow<PetEntity> = _currentPet

    private val _isDisplayingReward = MutableStateFlow(false)
    val isDisplayingReward: StateFlow<Boolean> = _isDisplayingReward

    init {
        viewModelScope.launch {
            petRepository.getPet().collect { pet ->
                _currentPet.value = pet
            }
        }

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
                is RewardUiEvent.DailyGoalReward -> current.bonusCoins
                is RewardUiEvent.AchievementReward -> 0
                is RewardUiEvent.ChestReward -> (current.amount as? Int) ?: 0
            }

            if (coinsToAdd > 0) {
                statisticsRepository.addCoins(coinsToAdd)
                if (current !is RewardUiEvent.LevelUpReward) {
                    microFeedbackManager.triggerCoinGained(coinsToAdd)
                }
            }

            when (current) {
                is RewardUiEvent.AchievementReward -> Unit

                is RewardUiEvent.DailyGoalReward -> {
                    rewardEventBus.emit(current)

                    if (current.bonusExp > 0) {
                        val expAmount = current.bonusExp.toInt()
                        microFeedbackManager.triggerXpGained(expAmount.toLong())
                        val (previousPet, updatedPet) = addPetExp(expAmount)
                        val newLevel = ExpConfig.calculateLevelFromXp(updatedPet.xp)
                        val newEvolutionStage = ExpConfig.calculateEvolutionStageFromXp(updatedPet.xp)

                        if (newLevel > previousPet.level) {
                            activityTimelineEngine.logLevelUp(newLevel, ExpConfig.levelUpCoins(newLevel))
                        }

                        val nextEvolutionStage = (newEvolutionStage + 1).coerceAtMost(ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex)
                        if (nextEvolutionStage > newEvolutionStage) {
                            activityTimelineEngine.logEvolutionMilestoneNearing(
                                toStage = nextEvolutionStage,
                                xp = updatedPet.xp
                            )
                        }

                        if (newEvolutionStage > previousPet.evolutionStage) {
                            activityTimelineEngine.logDragonEvolution(
                                fromStage = previousPet.evolutionStage,
                                toStage = newEvolutionStage
                            )
                        }
                    }
                }

                is RewardUiEvent.ChestReward -> {
                    rewardEventBus.emit(current)

                    if (current.expAmount > 0) {
                        microFeedbackManager.triggerXpGained(current.expAmount.toLong())
                        val (previousPet, updatedPet) = addPetExp(current.expAmount)
                        val newLevel = ExpConfig.calculateLevelFromXp(updatedPet.xp)
                        val newEvolutionStage = ExpConfig.calculateEvolutionStageFromXp(updatedPet.xp)

                        if (newLevel > previousPet.level) {
                            activityTimelineEngine.logLevelUp(newLevel, ExpConfig.levelUpCoins(newLevel))
                        }

                        val nextEvolutionStage = (newEvolutionStage + 1).coerceAtMost(ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex)
                        if (nextEvolutionStage > newEvolutionStage) {
                            activityTimelineEngine.logEvolutionMilestoneNearing(
                                toStage = nextEvolutionStage,
                                xp = updatedPet.xp
                            )
                        }

                        if (newEvolutionStage > previousPet.evolutionStage) {
                            activityTimelineEngine.logDragonEvolution(
                                fromStage = previousPet.evolutionStage,
                                toStage = newEvolutionStage
                            )
                        }
                    }

                    current.customizationId?.let { customizationId ->
                        inventoryItemRepository.grantItem(customizationId)
                    }
                }

                else -> Unit
            }

            _currentReward.value = null
            _isDisplayingReward.value = false

            rewardQueue.rewardDismissed()
        }
    }

    private suspend fun addPetExp(expAmount: Int): Pair<PetEntity, PetEntity> {
        val currentPet = petRepository.getPet().firstOrNull() ?: PetEntity(id = 1)
        val updatedPet = currentPet.copy(xp = currentPet.xp + expAmount)
        petRepository.updatePet(updatedPet)
        return currentPet to updatedPet
    }
}