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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for habit edit screen
 * Handles form state and habit editing logic
 */
@HiltViewModel
class HabitEditViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    // UI State
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _icon = MutableStateFlow("")
    val icon: StateFlow<String> = _icon.asStateFlow()

    private val _type = MutableStateFlow("CHECKBOX")
    val type: StateFlow<String> = _type.asStateFlow()

    private val _minimumDuration = MutableStateFlow(0)
    val minimumDuration: StateFlow<Int> = _minimumDuration.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Events
    private val _habitUpdated = MutableSharedFlow<Unit>(replay = 0)
    val habitUpdated: SharedFlow<Unit> = _habitUpdated.shareIn(
        viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        replay = 0
    )

    private val _habitDeleted = MutableSharedFlow<Unit>(replay = 0)
    val habitDeleted: SharedFlow<Unit> = _habitDeleted.shareIn(
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

    // Habit being edited
    private var habitBeingEdited: HabitEntity? = null

    // Initialize with habit data
    fun initialize(habitId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get habit from repository
                habitRepository.getHabitById(habitId).firstOrNull()
                    ?.let { habit ->
                        habitBeingEdited = habit
                        _name.value = habit.name
                        _icon.value = habit.icon
                        _type.value = habit.type
                        _minimumDuration.value = habit.minimumDurationMinutes
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Event handlers for UI updates
    fun onNameChanged(name: String) {
        _name.value = name
    }

    fun onIconSelected(icon: String) {
        _icon.value = icon
    }

    fun onTypeSelected(type: String) {
        _type.value = type
        // Reset minimum duration when switching types
        if (type == "CHECKBOX") {
            _minimumDuration.value = 0
        }
    }

    fun onMinimumDurationChanged(duration: Int) {
        _minimumDuration.value = duration
    }

    fun updateHabit() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Validate form
                val validationError = validateForm()
                if (validationError != null) {
                    _error.value = validationError
                    return@launch
                }

                // Check if we have a habit to edit
                val habit = habitBeingEdited ?: return@launch

                // Update habit entity
                val updatedHabit = habit.copy(
                    name = _name.value.trim(),
                    icon = _icon.value,
                    type = _type.value,
                    minimumDurationMinutes = if (_type.value == "TIMER") _minimumDuration.value else 0
                )

                // Save to database
                habitRepository.updateHabit(updatedHabit)

                // Notify habit update
                _habitUpdated.emit(Unit)

            } catch (e: Exception) {
                _error.value = "Failed to update habit: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHabit() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Check if we have a habit to delete
                val habit = habitBeingEdited ?: return@launch

                // Delete from database
                habitRepository.deleteHabit(habit)

                // Notify habit deletion
                _habitDeleted.emit(Unit)

            } catch (e: Exception) {
                _error.value = "Failed to delete habit: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateForm(): String? {
        if (_name.value.isBlank()) {
            return "Habit name is required"
        }

        if (_icon.value.isBlank()) {
            return "Please select an icon"
        }

        if (_type.value !in listOf("CHECKBOX", "TIMER")) {
            return "Invalid habit type"
        }

        if (_type.value == "TIMER" && _minimumDuration.value <= 0) {
            return "Minimum duration must be greater than 0 for timer habits"
        }

        return null
    }

    fun resetForm() {
        _name.value = ""
        _icon.value = ""
        _type.value = "CHECKBOX"
        _minimumDuration.value = 0
        _error.value = null
    }

    // Navigation handlers
    suspend fun onNavigateUp() {
        _navigateUp.emit(Unit)
    }
}
