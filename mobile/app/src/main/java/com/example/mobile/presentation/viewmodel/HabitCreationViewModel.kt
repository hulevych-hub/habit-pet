package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

/**
 * ViewModel for habit creation screen
 * Handles form state and habit creation logic
 */
@HiltViewModel
class HabitCreationViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    // UI State
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _icon = MutableStateFlow("")
    val icon: StateFlow<String> = _icon

    private val _type = MutableStateFlow("CHECKBOX")
    val type: StateFlow<String> = _type

    private val _minimumDuration = MutableStateFlow(0)
    val minimumDuration: StateFlow<Int> = _minimumDuration

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Events
    private val _habitCreated = MutableSharedFlow<Unit>(replay = 0)
    val habitCreated: SharedFlow<Unit> = _habitCreated.shareIn(
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

    fun createHabit() {
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

                // Create habit entity
                val habit = HabitEntity(
                    id = System.currentTimeMillis(), // Simple ID generation
                    name = _name.value.trim(),
                    icon = _icon.value,
                    type = _type.value,
                    minimumDurationMinutes = if (_type.value == "TIMER") _minimumDuration.value else 0
                )

                // Save to database
                habitRepository.addHabit(habit)

                // Notify habit creation
                _habitCreated.emit(Unit)

            } catch (e: Exception) {
                _error.value = e.message
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
    fun onNavigateUp() {
        _navigateUp.tryEmit(Unit)
    }
}
