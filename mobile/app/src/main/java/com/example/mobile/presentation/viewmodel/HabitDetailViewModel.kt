package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
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
    private val petRepository: PetRepository
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
    private val _pet = MutableStateFlow<PetEntity>(PetEntity(id = 1))
    val pet: StateFlow<PetEntity> = _pet

    // Timer habit state
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    // Events
    private val _habitCompleted = MutableSharedFlow<Unit>(replay = 0)
    val habitCompleted: SharedFlow<Unit> = _habitCompleted.shareIn(
        viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        replay = 0
    )

    // Navigation events
    private val _navigateBack = MutableSharedFlow<Unit>(replay = 0)
    val navigateBack: SharedFlow<Unit> = _navigateBack.shareIn(
        viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        replay = 0
    )

    // Initialize with habit ID
    fun initialize(habitId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Get habit
                habitRepository.getHabitById(habitId).firstOrNull()
                    ?.let { _habit.value = it }

                // Get completion history (last 30 days)
                val thirtyDaysAgo = getDayStart(System.currentTimeMillis()) - (30L * 24 * 60 * 60 * 1000)
                _completions.value = habitCompletionRepository
                    .getCompletionsForHabit(habitId, thirtyDaysAgo, System.currentTimeMillis())
                    .firstOrNull()
                    ?: emptyList()

                // Initialize pet
                petRepository.getPet().firstOrNull()
                    ?.let { _pet.value = it }
                    ?: run {
                        // Create default pet if none exists
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

    // Helper to get start of day (00:00:00)
    private fun getDayStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Check if habit was completed today
    fun isCompletedToday(habitId: Long): StateFlow<Boolean> {
        val todayStart = getDayStart(System.currentTimeMillis())
        return habitCompletionRepository.getCompletionForHabitOnDate(habitId, todayStart)
            .map { it != null }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )
    }

    // Complete checkbox habit
    fun completeCheckboxHabit(habitId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Check if already completed today
                if (isAlreadyCompletedToday(habitId)) {
                    _error.value = "Habit already completed today"
                    return@launch
                }

                // Calculate XP (base 10 XP for checkbox habit)
                val xpEarned: Long = 300

                // Create completion entity
                val completion = HabitCompletionEntity(
                    id = System.currentTimeMillis(),
                    habitId = habitId,
                    date = getDayStart(System.currentTimeMillis()),
                    xpEarned = xpEarned
                )

                // Save completion
                habitCompletionRepository.addCompletion(completion)
                refreshCompletions(habitId)

                // Award XP to pet
                awardPetXp(xpEarned)

                // Notify completion
                _habitCompleted.tryEmit(Unit)

                // Navigate back
                _navigateBack.tryEmit(Unit)

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Start timer habit
    fun startTimerHabit(habitId: Long) {
        viewModelScope.launch {
            if (isAlreadyCompletedToday(habitId)) {
                _error.value = "Habit already completed today"
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

    // Stop timer habit and attempt completion
    fun stopTimerHabit(habitId: Long) {
        _isTimerRunning.value = false

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val habit = _habit.value ?: return@launch
                val elapsedMinutes = _elapsedSeconds.value / 60

                // Check if minimum duration is met
                if (elapsedMinutes >= habit.minimumDurationMinutes) {
                    // Calculate XP: 10 base + 1 per minute
                    val xpEarned = (10 + elapsedMinutes).toLong()

                    // Create completion entity
                    val completion = HabitCompletionEntity(
                        id = System.currentTimeMillis(),
                        habitId = habitId,
                        date = getDayStart(System.currentTimeMillis()),
                        xpEarned = xpEarned
                    )

                    // Save completion
                    habitCompletionRepository.addCompletion(completion)
                    refreshCompletions(habitId)

                    // Award XP to pet
                    awardPetXp(xpEarned)

                    // Notify completion
                    _habitCompleted.tryEmit(Unit)

                    // Navigate back
                    _navigateBack.tryEmit(Unit)

                } else {
                    // Not enough time - show remaining time needed
                    val remainingMinutes = habit.minimumDurationMinutes - elapsedMinutes
                    _error.value = "Need ${remainingMinutes} more minutes to complete this habit"
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
                // Reset timer state
                _isTimerRunning.value = false
                _elapsedSeconds.value = 0
            }
        }
    }

    // Check if habit was already completed today
    private suspend fun isAlreadyCompletedToday(habitId: Long): Boolean {
        val todayStart = getDayStart(System.currentTimeMillis())
        return habitCompletionRepository
            .getCompletionForHabitOnDate(habitId, todayStart)
            .firstOrNull() != null
    }

    // Get completion status for today
    fun getTodayCompletionStatus(habitId: Long): StateFlow<Boolean> {
        val todayStart = getDayStart(System.currentTimeMillis())
        return habitCompletionRepository.getCompletionForHabitOnDate(habitId, todayStart)
            .map { it != null }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )
    }

    // Reset timer state
    fun resetTimer() {
        _isTimerRunning.value = false
        _elapsedSeconds.value = 0
    }

    // Award XP to pet and update level/evolution
    private fun awardPetXp(xpToAdd: Long) {
        viewModelScope.launch {
            val currentPet = _pet.value
            val updatedPet = currentPet.copy(xp = currentPet.xp + xpToAdd)

            // Recalculate level based on new XP
            val newLevel = calculateLevelFromXp(updatedPet.xp)
            val updatedPetWithLevel = updatedPet.copy(level = newLevel)

            // Recalculate evolution stage based on lifetime XP
            val newEvolutionStage = calculateEvolutionStageFromXp(updatedPet.xp)
            val finalPet = updatedPetWithLevel.copy(evolutionStage = newEvolutionStage)

            // Update pet in database
            petRepository.updatePet(finalPet)

            // Update UI state
            _pet.value = finalPet
        }
    }

    // Calculate level from XP using progressive formula
    private fun calculateLevelFromXp(totalXp: Long): Int {
        // Simple progressive formula: Level 1 = 100 XP, Level 2 = 200 XP, Level 3 = 350 XP, etc.
        // This creates increasing gaps between levels
        var level = 0
        var xpRequired = 100
        var xpRemaining = totalXp

        while (xpRemaining >= xpRequired) {
            xpRemaining -= xpRequired
            level++
            xpRequired = 100 + (level * 50) // Increases by 50 XP each level
        }

        return level
    }

    // Calculate evolution stage from lifetime XP
    private fun calculateEvolutionStageFromXp(totalXp: Long): Int {
        // Evolution stages based on lifetime XP thresholds
        return when {
            totalXp < 500 -> 0 // Egg
            totalXp < 1500 -> 1 // Hatchling
            totalXp < 3000 -> 2 // Young Dragon
            totalXp < 6000 -> 3 // Adult Dragon
            else -> 4 // Ancient Dragon
        }
    }

    private suspend fun refreshCompletions(habitId: Long) {
        val thirtyDaysAgo = getDayStart(System.currentTimeMillis()) - (30L * 24 * 60 * 60 * 1000)
        _completions.value = habitCompletionRepository
            .getCompletionsForHabit(habitId, thirtyDaysAgo, System.currentTimeMillis())
            .firstOrNull()
            ?: emptyList()
    }
}
