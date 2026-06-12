package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.HabitProgressEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.ChestRewardConfigProvider
import com.example.mobile.domain.ChestRewardFactory
import com.example.mobile.domain.DragonMoodEngine
import com.example.mobile.domain.EconomyConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitProgressRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.feedback.MicroFeedbackManager
import com.example.mobile.presentation.ui.reward.RewardQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for habit detail screen
 * Handles habit data, completion history, and completion logic
 */
@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository,
    private val habitProgressRepository: HabitProgressRepository,
    private val streakEngine: StreakEngine,
    private val rewardQueue: RewardQueue,
    private val inventoryItemRepository: InventoryItemRepository,
    private val activityTimelineEngine: ActivityTimelineEngine,
    private val microFeedbackManager: MicroFeedbackManager,
    private val dragonMoodEngine: DragonMoodEngine
) : ViewModel() {

    // UI State
    private val _habit = MutableStateFlow<HabitEntity?>(null)
    val habit: StateFlow<HabitEntity?> = _habit

    private val _completions = MutableStateFlow<List<HabitCompletionEntity>>(emptyList())
    val completions: StateFlow<List<HabitCompletionEntity>> = _completions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Pet state
    private val _pet = MutableStateFlow(PetEntity(id = 1))
    val pet: StateFlow<PetEntity> = _pet

    // Timer state
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds


    // =========================
    // EVENTS (FIXED)
    // =========================

    private val _habitCompleted = MutableSharedFlow<Unit>()
    val habitCompleted: SharedFlow<Unit> = _habitCompleted

    private val _rewardUiEvent = MutableSharedFlow<RewardUiEvent>()
    val rewardUiEvent: SharedFlow<RewardUiEvent> = _rewardUiEvent

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack

    // =========================
    // INIT
    // =========================

    fun initialize(habitId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                habitRepository.getHabitById(habitId).firstOrNull()
                    ?.let { _habit.value = it }

                val thirtyDaysAgo =
                    getDayStart(System.currentTimeMillis()) - (30L * 24 * 60 * 60 * 1000)

                _completions.value =
                    habitCompletionRepository
                        .getCompletionsForHabit(habitId, thirtyDaysAgo, System.currentTimeMillis())
                        .firstOrNull()
                        ?: emptyList()

                petRepository.getPet().firstOrNull()
                    ?.let { _pet.value = it }
                    ?: run {
                        val defaultPet = PetEntity(id = 1)
                        petRepository.updatePet(defaultPet)
                        _pet.value = defaultPet
                    }

            } catch (e: Exception) {
                _error.value = "Failed to load habit details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================
    // DAY HELPERS
    // =========================

    private fun getDayStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // =========================
    // CHECK COMPLETION
    // =========================

    fun isCompletedToday(habitId: Long): StateFlow<Boolean> {
        val todayStart = getDayStart(System.currentTimeMillis())
        return habitCompletionRepository
            .getCompletionForHabitOnDate(habitId, todayStart)
            .map { it != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    }

    // =========================
    // CHECKBOX HABIT
    // =========================

    fun completeCheckboxHabit(habitId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (isAlreadyCompletedToday(habitId)) {
                    _error.value = "Already completed today"
                    return@launch
                }

                val xpEarned = ExpConfig.CHECKBOX_HABIT_XP
                val coinsEarned = EconomyConfig.CHECKBOX_HABIT_COINS
                val today = getDayStart(System.currentTimeMillis())
                val wasDailyGoalCompletedToday = statisticsRepository.getStatistics()
                    .firstOrNull()
                    ?.dailyGoalCompletedDate == today / 86_400_000L

                val completionResult = habitCompletionRepository.addCompletionWithCombo(
                    HabitCompletionEntity(
                        id = System.currentTimeMillis(),
                        habitId = habitId,
                        date = today,
                        xpEarned = xpEarned
                    )
                )

                if (completionResult.completionId == -1L) {
                    _error.value = "Habit completion could not be saved"
                    return@launch
                }

                val totalXpEarned = completionResult.totalXpEarned
                activityTimelineEngine.logHabitCompleted(
                    habitName = _habit.value?.name ?: "Habit",
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
                streakEngine.evaluateTodayStreak(System.currentTimeMillis())
                if (dailyGoalCompletedAfterCompletion(today, wasDailyGoalCompletedToday)) {
                    queueDailyGoalReward()
                }
                refreshCompletions(habitId)

                awardPetXpAndCoins(totalXpEarned, coinsEarned)
                maybeTriggerHabitCompletionChest()
                microFeedbackManager.triggerHabitCompleted(
                    xp = totalXpEarned,
                    coins = coinsEarned,
                    combo = completionResult.combo,
                    comboMultiplier = completionResult.comboMultiplier
                )
                dragonMoodEngine.refreshMood()

                _habitCompleted.emit(Unit)

                _navigateBack.emit(Unit)

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================
    // TIMER HABIT
    // =========================

    fun startTimerHabit(habitId: Long) {
        viewModelScope.launch {
            if (isAlreadyCompletedToday(habitId)) {
                _error.value = "Already completed today"
                return@launch
            }

            _isTimerRunning.value = true
            _elapsedSeconds.value = 0

            while (_isTimerRunning.value) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun stopTimerHabit(habitId: Long) {
        _isTimerRunning.value = false

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val habit = _habit.value ?: return@launch

                val today = getDayStart(System.currentTimeMillis())

                val previous = habitProgressRepository
                    .getProgress(habitId, today)
                    .firstOrNull()
                    ?.accumulatedMinutes ?: 0

                val sessionMinutes = _elapsedSeconds.value / 60
                val total = previous + sessionMinutes

                habitProgressRepository.updateProgress(
                    HabitProgressEntity(
                        habitId = habitId,
                        date = today,
                        accumulatedMinutes = total,
                        lastUpdated = System.currentTimeMillis()
                    )
                )

                if (total >= habit.minimumDurationMinutes) {

                    val xpEarned = (ExpConfig.TIMER_HABIT_BASE_XP + total * ExpConfig.TIMER_HABIT_XP_PER_MINUTE).toLong()
                    val coinsEarned = EconomyConfig.TIMER_HABIT_BASE_COINS + sessionMinutes * EconomyConfig.TIMER_HABIT_COINS_PER_MINUTE
                    val wasDailyGoalCompletedToday = statisticsRepository.getStatistics()
                        .firstOrNull()
                        ?.dailyGoalCompletedDate == today / 86_400_000L

                    val completionResult = habitCompletionRepository.addCompletionWithCombo(
                        HabitCompletionEntity(
                            id = System.currentTimeMillis(),
                            habitId = habitId,
                            date = today,
                            xpEarned = xpEarned
                        )
                    )

                    if (completionResult.completionId == -1L) {
                        _error.value = "Habit completion could not be saved"
                        return@launch
                    }

                    val totalXpEarned = completionResult.totalXpEarned
                    activityTimelineEngine.logHabitCompleted(
                        habitName = _habit.value?.name ?: "Habit",
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
                    streakEngine.evaluateTodayStreak(System.currentTimeMillis())
                    if (dailyGoalCompletedAfterCompletion(today, wasDailyGoalCompletedToday)) {
                        queueDailyGoalReward()
                    }
                    refreshCompletions(habitId)

                    awardPetXpAndCoins(totalXpEarned, coinsEarned)
                    maybeTriggerHabitCompletionChest()
                    microFeedbackManager.triggerHabitCompleted(
                        xp = totalXpEarned,
                        coins = coinsEarned,
                        combo = completionResult.combo,
                        comboMultiplier = completionResult.comboMultiplier
                    )
                    dragonMoodEngine.refreshMood()

                    _habitCompleted.emit(Unit)

                    _navigateBack.emit(Unit)

                    habitProgressRepository.reset(habitId)

                } else {
                    val remaining = habit.minimumDurationMinutes - total
                    _error.value = "Need $remaining more minutes"
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
                _isTimerRunning.value = false
                _elapsedSeconds.value = 0
            }
        }
    }

    // =========================
    // HELPERS
    // =========================

    private suspend fun isAlreadyCompletedToday(habitId: Long): Boolean {
        val today = getDayStart(System.currentTimeMillis())
        return habitCompletionRepository
            .getCompletionForHabitOnDate(habitId, today)
            .firstOrNull() != null
    }

    fun getTodayCompletionStatus(habitId: Long): StateFlow<Boolean> {
        val today = getDayStart(System.currentTimeMillis())
        return habitCompletionRepository
            .getCompletionForHabitOnDate(habitId, today)
            .map { it != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        _elapsedSeconds.value = 0
    }

    // =========================
    // REWARDS
    // =========================

    private suspend fun dailyGoalCompletedAfterCompletion(today: Long, wasCompletedToday: Boolean): Boolean {
        val todayKey = today / 86_400_000L
        val completedToday = statisticsRepository.getStatistics()
            .firstOrNull()
            ?.dailyGoalCompletedDate == todayKey

        return completedToday && !wasCompletedToday
    }

    private fun queueDailyGoalReward() {
        val reward = RewardUiEvent.DailyGoalReward(
            goalXp = ExpConfig.DAILY_XP_GOAL,
            bonusCoins = EconomyConfig.DAILY_GOAL_COIN_BONUS,
            bonusExp = ExpConfig.DAILY_GOAL_BONUS_XP
        )

        rewardQueue.addReward(reward)
        activityTimelineEngine.logDailyGoalCompleted(
            goalXp = ExpConfig.DAILY_XP_GOAL,
            bonusCoins = EconomyConfig.DAILY_GOAL_COIN_BONUS,
            bonusExp = ExpConfig.DAILY_GOAL_BONUS_XP
        )
    }

    private fun awardPetXpAndCoins(xpToAdd: Long, coinsToAdd: Int) {
        viewModelScope.launch {
            val current = _pet.value
            val updated = current.copy(xp = current.xp + xpToAdd)

            val newLevel = ExpConfig.calculateLevelFromXp(updated.xp)
            val newEvolutionStage = ExpConfig.calculateEvolutionStageFromXp(updated.xp)
            val evolved = updated.copy(
                level = newLevel,
                evolutionStage = newEvolutionStage
            )

            petRepository.updatePet(evolved)
            _pet.value = evolved
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
                    ChestRewardFactory.buildChestReward(
                        rewardType = "level_up_${chestType.name.lowercase()}",
                        chestType = chestType,
                        inventoryItemRepository = inventoryItemRepository
                    )
                )
            }

            // Tease the next evolution and emit DragonEvolutionReward when a stage changes
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

    private fun maybeTriggerHabitCompletionChest() {
        if (!EconomyConfig.shouldTriggerHabitCompletionChest()) return

        viewModelScope.launch {
            val chestType = EconomyConfig.getRandomSurpriseChestType()
            val surpriseChest = ChestRewardFactory.buildChestReward(
                rewardType = "surprise_${chestType.name.lowercase()}",
                chestType = chestType,
                inventoryItemRepository = inventoryItemRepository
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

    private suspend fun refreshCompletions(habitId: Long) {
        val thirtyDays =
            getDayStart(System.currentTimeMillis()) - (30L * 24 * 60 * 60 * 1000)

        _completions.value =
            habitCompletionRepository
                .getCompletionsForHabit(habitId, thirtyDays, System.currentTimeMillis())
                .firstOrNull()
                ?: emptyList()
    }
}
