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
import com.example.mobile.presentation.ui.events.rewardPriority
import com.example.mobile.presentation.ui.events.tracksChallengeProgress
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
        val reward = rewardQueue.mergeNextRewardIfPossible(current)
        _currentReward.value = reward

        viewModelScope.launch {

            val coinsToAdd = when (reward) {
                is RewardUiEvent.CoinReward -> reward.amount
                // LevelUpReward coins are awarded here in RewardManager as the
                // single source of truth. ViewModels do NOT award level-up coins
                // directly — they only queue the reward for the UI overlay.
                is RewardUiEvent.LevelUpReward -> reward.coins
                is RewardUiEvent.DragonEvolutionReward -> 0
                is RewardUiEvent.StreakReward -> reward.coins
                is RewardUiEvent.ChestReward -> 0
                is RewardUiEvent.ExpReward,
                is RewardUiEvent.CustomizationReward -> 0
            }

            if (coinsToAdd > 0) {
                statisticsRepository.addCoins(coinsToAdd)
                if (reward.tracksChallengeProgress) {
                    challengeRepository.recordCoinsEarned(coinsToAdd)
                }
                if (reward !is RewardUiEvent.LevelUpReward) {
                    microFeedbackManager.triggerCoinGained(coinsToAdd)
                }
            }

            when (reward) {
                is RewardUiEvent.LevelUpReward -> {
                    activityTimelineEngine.logLevelUp(reward.level, reward.coins)
                }

                is RewardUiEvent.ExpReward -> {
                    rewardEventBus.emit(reward)
                    if (reward.amount > 0) {
                        val expAmount = reward.amount.toInt()
                        microFeedbackManager.triggerXpGained(expAmount.toLong())
                        val (previousPet, updatedPet) = addPetExp(expAmount)
                        if (reward.tracksChallengeProgress) {
                            challengeRepository.recordXpEarned(reward.amount)
                        }
                        queueLevelAndEvolutionRewards(previousPet, updatedPet, reward.tracksChallengeProgress)
                    }
                }

                is RewardUiEvent.CustomizationReward -> {
                    rewardEventBus.emit(reward)
                    val grantResult = inventoryItemRepository.grantItemByItemId(reward.equipableId)
                    if (grantResult >= 0 && reward.tracksChallengeProgress) {
                        challengeRepository.recordCustomizationUnlocked(reward.equipableId)
                    }
                }

                is RewardUiEvent.ChestReward -> {
                    rewardEventBus.emit(reward)
                    if (reward.tracksChallengeProgress) {
                        challengeRepository.recordChestOpened()
                    }

                    chestRewardSubEvents(reward)
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
            is Int -> if (amount > 0) rewards.add(RewardUiEvent.CoinReward(amount, current.tracksChallengeProgress))
            is String -> amount.toIntOrNull()?.takeIf { it > 0 }?.let {
                rewards.add(RewardUiEvent.CoinReward(it, current.tracksChallengeProgress))
            }
        }

        if (current.expAmount > 0) {
            rewards.add(RewardUiEvent.ExpReward(current.expAmount.toLong(), current.tracksChallengeProgress))
        }

        val equipableId = current.equipableId
            ?: current.customizationId?.let { id ->
                inventoryItemRepository.getItemById(id).firstOrNull()?.itemId
            }

        if (!equipableId.isNullOrBlank()) {
            rewards.add(RewardUiEvent.CustomizationReward(equipableId, current.tracksChallengeProgress))
        }

        return rewards.sortedBy { it.rewardPriority() }
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

    private suspend fun queueLevelAndEvolutionRewards(
        previousPet: PetEntity,
        updatedPet: PetEntity,
        tracksChallengeProgress: Boolean
    ) {
        if (updatedPet.level > previousPet.level) {
            val coins = ExpConfig.levelUpCoins(updatedPet.level)
            rewardQueue.addReward(
                RewardUiEvent.LevelUpReward(
                    previousLevel = previousPet.level,
                    level = updatedPet.level,
                    coins = coins,
                    tracksChallengeProgress = tracksChallengeProgress
                )
            )
        }

        val nextEvolutionStage = (updatedPet.evolutionStage + 1)
            .coerceAtMost(ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex)
        val nearingProgressThreshold = ExpConfig.xpThresholdForStage(nextEvolutionStage) * 8L / 10L
        if (
            updatedPet.evolutionStage == previousPet.evolutionStage &&
            nextEvolutionStage > updatedPet.evolutionStage &&
            updatedPet.xp >= nearingProgressThreshold
        ) {
            activityTimelineEngine.logEvolutionMilestoneNearing(
                toStage = nextEvolutionStage,
                xp = updatedPet.xp
            )
        }

        if (updatedPet.evolutionStage > previousPet.evolutionStage) {
            rewardQueue.addReward(
                RewardUiEvent.DragonEvolutionReward(
                    previousPet.evolutionStage,
                    updatedPet.evolutionStage,
                    tracksChallengeProgress = tracksChallengeProgress
                )
            )
            activityTimelineEngine.logDragonEvolution(
                fromStage = previousPet.evolutionStage,
                toStage = updatedPet.evolutionStage
            )
        }
    }
}