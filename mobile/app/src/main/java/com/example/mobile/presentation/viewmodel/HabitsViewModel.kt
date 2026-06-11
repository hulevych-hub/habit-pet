package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.ChestRewardConfigProvider
import com.example.mobile.domain.ChestType
import com.example.mobile.domain.DragonMoodEngine
import com.example.mobile.domain.EconomyConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.feedback.MicroFeedbackManager
import com.example.mobile.presentation.ui.reward.RewardQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val streakEngine: StreakEngine,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository,
    private val rewardQueue: RewardQueue,
    private val inventoryItemRepository: InventoryItemRepository,
    private val activityTimelineEngine: ActivityTimelineEngine,
    private val microFeedbackManager: MicroFeedbackManager,
    private val dragonMoodEngine: DragonMoodEngine
) : ViewModel() {

    val habits = habitRepository.getAllHabits()

    private val _optimisticCompletedHabitIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _completingHabitIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _error = MutableStateFlow<String?>(null)
    private var completionsSinceLastSurprise = 0

    val error: StateFlow<String?> = _error
    val completingHabitIds: StateFlow<Set<Long>> = _completingHabitIds

    private val completedTodayFromRepository = habitRepository.getAllHabits()
        .flatMapLatest { habits ->
            val today = getDayStart(System.currentTimeMillis())

            if (habits.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    habits.map { habit ->
                        habitCompletionRepository.getCompletionForHabitOnDate(habit.id, today)
                            .map { completion -> habit.id to (completion != null) }
                    }
                ) { pairs ->
                    pairs.toList().toMap()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val completedToday: StateFlow<Map<Long, Boolean>> = combine(
        completedTodayFromRepository,
        _optimisticCompletedHabitIds
    ) { completed, optimisticIds ->
        completed + optimisticIds.associateWith { true }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
            streakEngine.recalculateTodayStreak(System.currentTimeMillis())
        }
    }

    fun completeCheckboxHabit(habit: HabitEntity) {
        if (habit.type != "CHECKBOX") return

        viewModelScope.launch {
            if (habit.id in _optimisticCompletedHabitIds.value || isAlreadyCompletedToday(habit.id)) {
                return@launch
            }

            _completingHabitIds.value += habit.id
            _error.value = null

            try {
                val now = System.currentTimeMillis()
                val xpEarned = ExpConfig.CHECKBOX_HABIT_XP
                val coinsEarned = EconomyConfig.CHECKBOX_HABIT_COINS

                habitCompletionRepository.addCompletion(
                    HabitCompletionEntity(
                        id = now,
                        habitId = habit.id,
                        date = getDayStart(now),
                        xpEarned = xpEarned
                    )
                )
                _optimisticCompletedHabitIds.value += habit.id

                activityTimelineEngine.logHabitCompleted(
                    habitName = habit.name,
                    xpEarned = xpEarned,
                    coinsEarned = coinsEarned
                )
                streakEngine.evaluateTodayStreak(now)
                awardPetXpAndCoins(xpEarned, coinsEarned)
                maybeTriggerSurpriseReward()
                microFeedbackManager.triggerHabitCompleted(xpEarned, coinsEarned)
                dragonMoodEngine.refreshMood()
            } catch (e: Exception) {
                _optimisticCompletedHabitIds.value -= habit.id
                _error.value = e.message
            } finally {
                _completingHabitIds.value -= habit.id
            }
        }
    }

    private suspend fun isAlreadyCompletedToday(habitId: Long): Boolean {
        val today = getDayStart(System.currentTimeMillis())
        return habitCompletionRepository
            .getCompletionForHabitOnDate(habitId, today)
            .firstOrNull() != null
    }

    private fun getDayStart(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun awardPetXpAndCoins(xpToAdd: Long, coinsToAdd: Int) {
        viewModelScope.launch {
            val current = petRepository.getPet().firstOrNull() ?: PetEntity(id = 1)
            val updated = current.copy(xp = current.xp + xpToAdd)
            val newLevel = ExpConfig.calculateLevelFromXp(updated.xp)
            val newEvolutionStage = ExpConfig.calculateEvolutionStageFromXp(updated.xp)
            val evolved = updated.copy(
                level = newLevel,
                evolutionStage = newEvolutionStage
            )

            petRepository.updatePet(evolved)
            microFeedbackManager.triggerXpGained(xpToAdd)
            awardCoins(coinsToAdd)

            if (newLevel > current.level) {
                val bonus = ExpConfig.levelUpCoins(newLevel)
                awardCoins(bonus)

                rewardQueue.addReward(
                    RewardUiEvent.LevelUpReward(newLevel, bonus)
                )
                activityTimelineEngine.logLevelUp(newLevel, bonus)

                val chestType = ChestRewardConfigProvider.getRandomChestType()
                rewardQueue.addReward(
                    buildChestReward(
                        rewardType = "level_up_${chestType.name.lowercase()}",
                        chestType = chestType
                    )
                )
            }

            val nextEvolutionStage = (newEvolutionStage + 1).coerceAtMost(ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex)
            if (nextEvolutionStage > newEvolutionStage) {
                activityTimelineEngine.logEvolutionMilestoneNearing(
                    toStage = nextEvolutionStage,
                    xp = updated.xp
                )
            }

            if (newEvolutionStage > current.evolutionStage) {
                rewardQueue.addReward(
                    RewardUiEvent.DragonEvolutionReward(current.evolutionStage, newEvolutionStage)
                )
                activityTimelineEngine.logDragonEvolution(
                    fromStage = current.evolutionStage,
                    toStage = newEvolutionStage
                )
            }
        }
    }

    private fun awardCoins(coins: Int) {
        viewModelScope.launch {
            val stats = statisticsRepository.getStatistics().firstOrNull()
                ?: StatisticsEntity()

            microFeedbackManager.triggerCoinGained(coins)

            statisticsRepository.updateStatistics(
                stats.copy(
                    totalCoins = stats.totalCoins + coins,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    private fun maybeTriggerSurpriseReward() {
        completionsSinceLastSurprise += 1
        if (!EconomyConfig.shouldTriggerSurpriseReward(completionsSinceLastSurprise)) return

        completionsSinceLastSurprise = 0
        viewModelScope.launch {
            val chestType = EconomyConfig.getRandomSurpriseChestType()
            val surpriseChest = buildChestReward(
                rewardType = "surprise_${chestType.name.lowercase()}",
                chestType = chestType
            )

            awardPetXpAndCoins(EconomyConfig.SURPRISE_BONUS_XP, EconomyConfig.SURPRISE_BONUS_COINS)
            activityTimelineEngine.logSurpriseReward(
                coins = EconomyConfig.SURPRISE_BONUS_COINS,
                xp = EconomyConfig.SURPRISE_BONUS_XP,
                chestType = chestType.name.lowercase(),
                hasCustomization = surpriseChest.customizationId != null
            )
            rewardQueue.addReward(surpriseChest)
        }
    }

    private suspend fun buildChestReward(
        rewardType: String,
        chestType: ChestType
    ): RewardUiEvent.ChestReward {
        val config = ChestRewardConfigProvider.getConfig(chestType)
        var customizationId: Long? = null

        if (config.customizationRarity != null && Math.random() < config.customizationDropChance) {
            val unownedItems = inventoryItemRepository.getUnownedItemsByRarity(config.customizationRarity)
                .firstOrNull()
                ?.toList()
                .orEmpty()

            if (unownedItems.isNotEmpty()) {
                val selectedItem = unownedItems.random()
                val grantResult = inventoryItemRepository.grantItem(selectedItem.id)
                if (grantResult == 1) {
                    customizationId = selectedItem.id
                }
            }
        }

        return RewardUiEvent.ChestReward(
            rewardType = rewardType,
            amount = config.getRandomCoins(),
            expAmount = config.getRandomExp(),
            customizationId = customizationId
        )
    }
}
