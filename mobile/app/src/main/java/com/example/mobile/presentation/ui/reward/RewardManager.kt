package com.example.mobile.presentation.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.ChallengeRepository
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
    private val microFeedbackManager: MicroFeedbackManager,
    private val challengeRepository: ChallengeRepository
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
                is RewardUiEvent.AchievementReward -> 0
                is RewardUiEvent.ChestReward -> (current.amount as? Int) ?: 0
                is RewardUiEvent.ExpReward,
                is RewardUiEvent.CustomizationReward -> 0
            }

            if (coinsToAdd > 0) {
                statisticsRepository.addCoins(coinsToAdd)
                challengeRepository.recordCoinsEarned(coinsToAdd)
                if (current !is RewardUiEvent.LevelUpReward) {
                    microFeedbackManager.triggerCoinGained(coinsToAdd)
                }
            }

            when (current) {
                is RewardUiEvent.AchievementReward -> Unit

                is RewardUiEvent.ExpReward -> {
                    rewardEventBus.emit(current)
                    if (current.amount > 0) {
                        val expAmount = current.amount.toInt()
                        microFeedbackManager.triggerXpGained(expAmount.toLong())
                        val (previousPet, updatedPet) = addPetExp(expAmount)
                        challengeRepository.recordXpEarned(current.amount)
                        queueLevelAndEvolutionRewards(previousPet, updatedPet)
                    }
                }

                is RewardUiEvent.CustomizationReward -> {
                    rewardEventBus.emit(current)
                    val grantResult = inventoryItemRepository.grantItemByItemId(current.equipableId)
                    if (grantResult >= 0) {
                        challengeRepository.recordCustomizationUnlocked(current.equipableId)
                    }
                }

                is RewardUiEvent.ChestReward -> {
                    rewardEventBus.emit(current)

                    if (current.expAmount > 0) {
                        microFeedbackManager.triggerXpGained(current.expAmount.toLong())
                        val (previousPet, updatedPet) = addPetExp(current.expAmount)
                        challengeRepository.recordXpEarned(current.expAmount.toLong())
                        queueLevelAndEvolutionRewards(previousPet, updatedPet)
                    }

                    val grantResult = when {
                        !current.equipableId.isNullOrBlank() ->
                            inventoryItemRepository.grantItemByItemId(current.equipableId)
                        current.customizationId != null ->
                            inventoryItemRepository.grantItem(current.customizationId)
                        else -> 0
                    }
                    if (grantResult >= 0) {
                        if (!current.equipableId.isNullOrBlank()) {
                            challengeRepository.recordCustomizationUnlocked(current.equipableId)
                        } else {
                            current.customizationId?.let {
                                inventoryItemRepository.getItemById(it).firstOrNull()?.let { item ->
                                    challengeRepository.recordCustomizationUnlocked(item.itemId)
                                }
                            }
                        }
                    } else if (grantResult < 0) {
                        current.customizationId?.let { inventoryItemRepository.grantItem(it) }
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
        val updatedXp = currentPet.xp + expAmount
        val updatedPet = currentPet.copy(
            xp = updatedXp,
            level = ExpConfig.calculateLevelFromXp(updatedXp),
            evolutionStage = ExpConfig.calculateEvolutionStageFromXp(updatedXp)
        )
        petRepository.updatePet(updatedPet)
        return currentPet to updatedPet
    }

    private fun queueLevelAndEvolutionRewards(previousPet: PetEntity, updatedPet: PetEntity) {
        if (updatedPet.level > previousPet.level) {
            val coins = ExpConfig.levelUpCoins(updatedPet.level)
            rewardQueue.addReward(RewardUiEvent.LevelUpReward(updatedPet.level, coins))
            activityTimelineEngine.logLevelUp(updatedPet.level, coins)
        }

        val nextEvolutionStage = (updatedPet.evolutionStage + 1)
            .coerceAtMost(ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex)
        if (nextEvolutionStage > updatedPet.evolutionStage) {
            activityTimelineEngine.logEvolutionMilestoneNearing(
                toStage = nextEvolutionStage,
                xp = updatedPet.xp
            )
        }

        if (updatedPet.evolutionStage > previousPet.evolutionStage) {
            rewardQueue.addReward(
                RewardUiEvent.DragonEvolutionReward(
                    previousPet.evolutionStage,
                    updatedPet.evolutionStage
                )
            )
            activityTimelineEngine.logDragonEvolution(
                fromStage = previousPet.evolutionStage,
                toStage = updatedPet.evolutionStage
            )
        }
    }
}