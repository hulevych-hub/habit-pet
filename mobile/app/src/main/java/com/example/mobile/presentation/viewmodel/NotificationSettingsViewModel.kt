package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for notification settings screen
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor() : ViewModel() {

    // UI State for daily reminder
    private val _dailyReminderEnabled = MutableStateFlow(false)
    val dailyReminderEnabled: StateFlow<Boolean> = _dailyReminderEnabled

    // UI State for streak reminder
    private val _streakReminderEnabled = MutableStateFlow(false)
    val streakReminderEnabled: StateFlow<Boolean> = _streakReminderEnabled

    // UI State for pet reminder
    private val _petReminderEnabled = MutableStateFlow(false)
    val petReminderEnabled: StateFlow<Boolean> = _petReminderEnabled

    init {
        // Load current preference states
        // Note: In a real implementation, we'd need to pass Context to ViewModel
        // For now, we'll use default values and update them when we have context
        _dailyReminderEnabled.value = true  // default
        _streakReminderEnabled.value = true  // default
        _petReminderEnabled.value = true     // default
    }

    // Methods to update preferences (would be called from UI with context)
    fun setDailyReminderEnabled(enabled: Boolean) {
        _dailyReminderEnabled.value = enabled
        // Actual preference saving would happen in the UI layer with context
    }

    fun setStreakReminderEnabled(enabled: Boolean) {
        _streakReminderEnabled.value = enabled
    }

    fun setPetReminderEnabled(enabled: Boolean) {
        _petReminderEnabled.value = enabled
    }
}