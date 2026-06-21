package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.ChallengeEngine
import com.example.mobile.domain.ChestRewardConfigProvider
import com.example.mobile.domain.ChestRewardFactory
import com.example.mobile.domain.DragonMoodEngine
import com.example.mobile.domain.EconomyConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.components.StreakCalendarBuilder
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.feedback.MicroFeedbackManager
import com.example.mobile.presentation.ui.reward.RewardQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val dragonMoodEngine: DragonMoodEngine,
    private val challengeEngine: ChallengeEngine
) : ViewModel() {

    val habits = habitRepository.getAllHabits()

    private val _optimisticCompletedHabitIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _completingHabitIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _error = MutableStateFlow<String?>(null)
    private val _streakCalendarState = MutableStateFlow<StreakCalendarUiState?>(null)
    private var streakCalendarHabitId: Long? = null
    private var surpriseCompletionsSinceLastReward = 0

    val error: StateFlow<String?> = _error
    val completingHabitIds: StateFlow<Set<Long>> = _completingHabitIds
    val streakCalendarState: StateFlow<StreakCalendarUiState?> = _streakCalendarState.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun openHabitStreakCalendar(habitId: Long) {
        viewModelScope.launch {
            val monthStart = getMonthStart(System.currentTimeMillis())
            streakCalendarHabitId = habitId
            _streakCalendarState.value = StreakCalendarUiState.loading(
                title = "Habit Streak",
                subtitle = "Loading habit rhythm...",
                monthStart = monthStart
            )

            val habit = habitRepository.getHabitById(habitId).firstOrNull() ?: return@launch
            _streakCalendarState.value = StreakCalendarBuilder.buildHabit(
                monthStart = monthStart,
                habit = habit,
                completionRepository = habitCompletionRepository
            )
        }
    }

    fun showPreviousStreakMonth() {
        val current = _streakCalendarState.value ?: return
        if (!current.canNavigatePrevious) return

        viewModelScope.launch {
            val previousMonthStart = addMonths(current.monthStart, -1)
            _streakCalendarState.value = StreakCalendarUiState.loading(
                title = current.title,
                subtitle = current.subtitle,
                monthStart = previousMonthStart
            )

            val habitId = streakCalendarHabitId ?: return@launch
            val habit = habitRepository.getHabitById(habitId).firstOrNull() ?: return@launch
            _streakCalendarState.value = StreakCalendarBuilder.buildHabit(
                monthStart = previousMonthStart,
                habit = habit,
                completionRepository = habitCompletionRepository
            )
        }
    }

    fun closeStreakCalendar() {
        _streakCalendarState.value = null
        streakCalendarHabitId = null
    }

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
            _error.value = null
            try {
                habitRepository.deleteHabit(habit)
                streakEngine.recalculateTodayStreak(System.currentTimeMillis())
            } catch (e: Exception) {
                _error.value = e.message ?: "Habit could not be deleted"
            }
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

                val completionResult = habitCompletionRepository.addCompletionWithCombo(
                    HabitCompletionEntity(
                        id = now,
                        habitId = habit.id,
                        date = getDayStart(now),
                        xpEarned = xpEarned
                    )
                )
                _optimisticCompletedHabitIds.value += habit.id

                if (completionResult.completionId == -1L) {
                    _optimisticCompletedHabitIds.value -= habit.id
                    _error.value = "Habit completion could not be saved"
                    return@launch
                }

                if (!completionResult.isNewCompletion) {
                    _optimisticCompletedHabitIds.value -= habit.id
                    _error.value = "Already completed today"
                    return@launch
                }

                val totalXpEarned = completionResult.totalXpEarned
                activityTimelineEngine.logHabitCompleted(
                    habitName = habit.name,
                    xpEarned = totalXpEarned,
                    coinsEarned = coinsEarned,
                    combo = completionResult.combo,
                    comboBonusXp = completionResult.comboBonusXp,
                    comboMultiplier = completionResult.comboMultiplier
                )
                if (completionResult.comboMilestoneReached) {
                    activityTimelineEngine.logComboMilestone(
                        combo = completionResult.combo,
                        bonusXp = completionResult.comboBonusXp,
                        multiplier = completionResult.comboMultiplier
                    )
                }
                streakEngine.evaluateTodayStreak(now)
                awardPetXpAndCoins(totalXpEarned, coinsEarned)
                challengeEngine.recordHabitCompleted(habit.id, totalXpEarned, coinsEarned)
                maybeTriggerHabitCompletionChest()
                microFeedbackManager.triggerHabitCompleted(
                    xp = totalXpEarned,
                    coins = coinsEarned,
                    combo = completionResult.combo,
                    comboMultiplier = completionResult.comboMultiplier
                )
                dragonMoodEngine.refreshMood()
            } catch (e: Exception) {
                _optimisticCompletedHabitIds.value -= habit.id
                _error.value = e.message ?: "Habit completion could not be saved"
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

    private fun getMonthStart(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.timeInMillis
    }

    private fun addMonths(monthStart: Long, months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = monthStart
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }

    private fun awardPetXpAndCoins(
        xpToAdd: Long,
        coinsToAdd: Int,
        trackChallenge: Boolean = false
    ) {
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
            awardCoins(coinsToAdd, trackChallenge)

            if (newLevel > current.level) {
                val bonus = ExpConfig.levelUpCoins(newLevel)

                rewardQueue.addReward(
                    RewardUiEvent.LevelUpReward(
                        previousLevel = current.level,
                        level = newLevel,
                        coins = bonus
                    )
                )
                val chestType = ChestRewardConfigProvider.getRandomChestType()
                rewardQueue.addReward(
                    ChestRewardFactory.buildChestReward(
                        rewardType = "level_up_${chestType.name.lowercase()}",
                        chestType = chestType,
                        inventoryItemRepository = inventoryItemRepository
                    )
                )
            }

            val nextEvolutionStage = (newEvolutionStage + 1).coerceAtMost(ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex)
            val nearingProgressThreshold = ExpConfig.xpThresholdForStage(nextEvolutionStage) * 8L / 10L
            if (
                newEvolutionStage == current.evolutionStage &&
                nextEvolutionStage > newEvolutionStage &&
                updated.xp >= nearingProgressThreshold
            ) {
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

    private fun awardCoins(coins: Int, trackChallenge: Boolean = false) {
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

            if (trackChallenge && coins > 0) {
                challengeEngine.recordCoinsEarned(coins)
            }
        }
    }

    private fun maybeTriggerHabitCompletionChest() {
        surpriseCompletionsSinceLastReward++
        if (surpriseCompletionsSinceLastReward < EconomyConfig.SURPRISE_MIN_COMPLETIONS_BEFORE_CHANCE) return

        surpriseCompletionsSinceLastReward = 0
        if (!EconomyConfig.shouldTriggerHabitCompletionChest()) return

        viewModelScope.launch {
            val chestType = EconomyConfig.getRandomSurpriseChestType()
            val surpriseChest = ChestRewardFactory.buildChestReward(
                rewardType = "surprise_${chestType.name.lowercase()}",
                chestType = chestType,
                inventoryItemRepository = inventoryItemRepository
            )

            awardPetXpAndCoins(
                xpToAdd = EconomyConfig.SURPRISE_BONUS_XP,
                coinsToAdd = EconomyConfig.SURPRISE_BONUS_COINS,
                trackChallenge = true
            )
            challengeEngine.recordXpEarned(EconomyConfig.SURPRISE_BONUS_XP)
            activityTimelineEngine.logSurpriseReward(
                coins = EconomyConfig.SURPRISE_BONUS_COINS,
                xp = EconomyConfig.SURPRISE_BONUS_XP,
                chestType = chestType.name.lowercase(),
                hasCustomization = surpriseChest.customizationId != null
            )
            rewardQueue.addReward(surpriseChest)
        }
    }
}
