package com.example.mobile.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    // UI State
    val statistics: StateFlow<StatisticsEntity> = statisticsRepository.getStatistics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsEntity()
        )

    val habits: StateFlow<List<HabitEntity>> = habitRepository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combined state for easy access in UI
    val uiState: StateFlow<UiState> = combine(
        statistics,
        habits
    ) { stats, habList ->
        UiState(
            globalStreak = stats.globalStreak,
            habits = habList
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState(globalStreak = 0, habits = emptyList())
        )

    data class UiState(
        val globalStreak: Int,
        val habits: List<HabitEntity>
    )
}
