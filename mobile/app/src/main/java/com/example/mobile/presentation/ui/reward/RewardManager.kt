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
                is RewardUiEvent.LevelUpReward -> 0
                is RewardUiEvent.DragonEvolutionReward -> 0
                is RewardUiEvent.StreakReward -> current.coins
                is RewardUiEvent.AchievementReward -> 0
                is RewardUiEvent.ChestReward -> 0
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
                    challengeRepository.recordChestOpened()

                    chestRewardSubEvents(current)
                        .forEach { rewardQueue.addReward(it) }
                }

                else -> Unit
            }

            _currentReward.value = null
            _isDisplayingReward.value = false

            rewardQueue.rewardDismissed()
        }
    }

    private suspend fun chestRewardSubEvents(current: RewardUiEvent.ChestReward): List<RewardUiEvent> {
        val rewards = mutableListOf<RewardUiEvent>()

        when (val amount = current.amount) {
            is Int -> if (amount > 0) rewards.add(RewardUiEvent.CoinReward(amount))
            is String -> amount.toIntOrNull()?.takeIf { it > 0 }?.let { rewards.add(RewardUiEvent.CoinReward(it)) }
        }

        if (current.expAmount > 0) {
            rewards.add(RewardUiEvent.ExpReward(current.expAmount.toLong()))
        }

        val equipableId = current.equipableId
            ?: current.customizationId?.let { id ->
                inventoryItemRepository.getItemById(id).firstOrNull()?.itemId
            }

        if (!equipableId.isNullOrBlank()) {
            rewards.add(RewardUiEvent.CustomizationReward(equipableId))
        }

        return rewards.sortedBy { rewardPriority(it) }
    }

    private fun rewardPriority(reward: RewardUiEvent): Int = when (reward) {
        is RewardUiEvent.LevelUpReward -> 1
        is RewardUiEvent.DragonEvolutionReward -> 2
        is RewardUiEvent.StreakReward -> 3
        is RewardUiEvent.ChestReward -> 4
        is RewardUiEvent.AchievementReward -> 5
        is RewardUiEvent.ExpReward -> 6
        is RewardUiEvent.CustomizationReward -> 7
        is RewardUiEvent.CoinReward -> 8
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

    private suspend fun queueLevelAndEvolutionRewards(previousPet: PetEntity, updatedPet: PetEntity) {
        if (updatedPet.level > previousPet.level) {
            val coins = ExpConfig.levelUpCoins(updatedPet.level)
            statisticsRepository.addCoins(coins)
            challengeRepository.recordCoinsEarned(coins)
            rewardQueue.addReward(
                RewardUiEvent.LevelUpReward(
                    previousLevel = previousPet.level,
                    level = updatedPet.level,
                    coins = coins
                )
            )
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