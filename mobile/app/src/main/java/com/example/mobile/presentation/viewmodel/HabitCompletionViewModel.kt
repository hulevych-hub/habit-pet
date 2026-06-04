package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for habit completion logic
 * Handles both checkbox and timer habit completion
 * Note: In the final implementation, completion logic is integrated into HabitDetailViewModel
 * This file is kept for reference but not used in the current implementation
 */
@HiltViewModel
class HabitCompletionViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    // This ViewModel is not currently used as completion logic is handled in HabitDetailViewModel
    // Keeping it for reference/completeness

    // UI State
    private val _habit = MutableStateFlow<HabitEntity?>(null)
    val habit: StateFlow<HabitEntity?> = _habit.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Timer habit state
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    // Events
    private val _habitCompleted = MutableSharedFlow<Unit>(replay = 0)
    val habitCompleted: SharedFlow<Unit> = _habitCompleted.shareIn(
        viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        replay = 0
    )

    private val _navigateUp = MutableSharedFlow<Unit>(replay = 0)
    val navigateUp: SharedFlow<Unit> = _navigateUp.shareIn(
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
                habitRepository.getHabitById(habitId)
                    .collect { habit ->
                        habit?.let { _habit.value = it }
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load habit: ${e.message}"
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

    // Placeholder methods - actual implementation is in HabitDetailViewModel
    fun completeCheckboxHabit(habitId: Long) {
        // Implementation moved to HabitDetailViewModel
    }

    fun startTimerHabit(habitId: Long) {
        // Implementation moved to HabitDetailViewModel
    }

    fun stopTimerHabit(habitId: Long) {
        // Implementation moved to HabitDetailViewModel
    }

    fun resetTimer() {
        // Implementation moved to HabitDetailViewModel
    }

    // Navigation handlers
    suspend fun onNavigateUp() {
        _navigateUp.emit(Unit)
    }
}