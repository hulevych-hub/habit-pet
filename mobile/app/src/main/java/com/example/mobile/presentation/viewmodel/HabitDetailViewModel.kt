package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.HabitProgressEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitProgressRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
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
    private val rewardQueue: RewardQueue
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

                val xpEarned: Long = 1500

                val completion = HabitCompletionEntity(
                    id = System.currentTimeMillis(),
                    habitId = habitId,
                    date = getDayStart(System.currentTimeMillis()),
                    xpEarned = xpEarned
                )

                habitCompletionRepository.addCompletion(completion)
                val today = getDayStart(System.currentTimeMillis())
                streakEngine.evaluateTodayStreak(System.currentTimeMillis())
                refreshCompletions(habitId)

                awardPetXpAndCoins(xpEarned, 1)

                //rewardQueue.addReward(RewardUiEvent.CoinReward(1))
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

                    val xpEarned = (10 + total).toLong()

                    val completion = HabitCompletionEntity(
                        id = System.currentTimeMillis(),
                        habitId = habitId,
                        date = today,
                        xpEarned = xpEarned
                    )

                    habitCompletionRepository.addCompletion(completion)
                    val today = getDayStart(System.currentTimeMillis())
                    streakEngine.evaluateTodayStreak(System.currentTimeMillis())
                    refreshCompletions(habitId)

                    awardPetXpAndCoins(xpEarned, (10 + sessionMinutes).toInt())

                    //rewardQueue.addReward(RewardUiEvent.CoinReward(1))
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

    private fun awardPetXpAndCoins(xpToAdd: Long, coinsToAdd: Int) {
        viewModelScope.launch {
            val current = _pet.value
            val updated = current.copy(xp = current.xp + xpToAdd)

            val newLevel = calculateLevelFromXp(updated.xp)
            val newEvolutionStage = calculateEvolutionStageFromXp(updated.xp)
            val evolved = updated.copy(
                level = newLevel,
                evolutionStage = newEvolutionStage
            )

            petRepository.updatePet(evolved)
            _pet.value = evolved

            awardCoins(coinsToAdd)

            if (newLevel > current.level) {
                val bonus = newLevel * 10
                awardCoins(bonus)

                rewardQueue.addReward(
                    RewardUiEvent.LevelUpReward(newLevel, bonus)
                )

                rewardQueue.addReward(
                    RewardUiEvent.ChestReward("Level up", 20)
                )
            }

            // Check for evolution stage change and emit DragonEvolutionReward
            if (newEvolutionStage > current.evolutionStage) {
                rewardQueue.addReward(
                    RewardUiEvent.DragonEvolutionReward(current.evolutionStage, newEvolutionStage)
                )
            }
        }
    }

    private fun awardCoins(coins: Int) {
        viewModelScope.launch {
            val stats = statisticsRepository.getStatistics().firstOrNull()
                ?: StatisticsEntity()

            statisticsRepository.updateStatistics(
                stats.copy(
                    totalCoins = stats.totalCoins + coins,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    // =========================
    // FORMULAS
    // =========================

    private fun calculateLevelFromXp(totalXp: Long): Int {
        var level = 0
        var xpRequired = 100
        var remaining = totalXp

        while (remaining >= xpRequired) {
            remaining -= xpRequired
            level++
            xpRequired = 100 + level * 50
        }
        return level
    }

    private fun calculateEvolutionStageFromXp(totalXp: Long): Int {
        return when {
            totalXp < 500 -> 0
            totalXp < 1500 -> 1
            totalXp < 3000 -> 2
            totalXp < 6000 -> 3
            else -> 4
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
