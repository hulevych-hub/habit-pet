package com.example.mobile.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val habitRepository: HabitRepository,
    private val petRepository: PetRepository,
    private val habitCompletionRepository: HabitCompletionRepository
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

    val pet: StateFlow<PetEntity> = petRepository.getPet()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PetEntity(id = 1)
        )

    private val todayCompletionStatuses: StateFlow<Map<Long, Boolean>> = habitRepository.getAllHabits()
        .flatMapLatest { habits ->
            if (habits.isEmpty()) {
                flowOf(emptyMap())
            } else {
                val today = getDayStart(System.currentTimeMillis())
                combine(
                    habits.map { habit ->
                        habitCompletionRepository.getCompletionForHabitOnDate(habit.id, today)
                            .map { completion -> habit.id to (completion != null) }
                    }
                ) { pairs -> pairs.toMap() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Combined state for easy access in UI
    val uiState: StateFlow<UiState> = combine(
        statistics,
        habits,
        pet,
        todayCompletionStatuses
    ) { stats, habList, petState, completionStatuses ->
        UiState(
            globalStreak = stats.globalStreak,
            habits = habList,
            pet = petState,
            completedToday = completionStatuses
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState(
                globalStreak = 0,
                habits = emptyList(),
                pet = PetEntity(id = 1),
                completedToday = emptyMap()
            )
        )

    data class UiState(
        val globalStreak: Int,
        val habits: List<HabitEntity>,
        val pet: PetEntity,
        val completedToday: Map<Long, Boolean>
    )

    private fun getDayStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
