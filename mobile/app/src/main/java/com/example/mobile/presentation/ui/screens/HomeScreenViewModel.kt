package com.example.mobile.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val habitRepository: HabitRepository,
    private val petRepository: PetRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    fun resetAllGameData() {
        viewModelScope.launch {
            habitRepository.deleteAll()
            habitCompletionRepository.deleteAll()
            petRepository.resetPet()
            statisticsRepository.reset()
            achievementRepository.reset()
        }
    }

    fun renamePet(name: String) {
        viewModelScope.launch {
            val currentPet = pet.value
            petRepository.updatePet(currentPet.copy(id = 1, name = name))
        }
    }

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
            started = SharingStarted.Eagerly,
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

    private val todayXpProgress: StateFlow<Long> = habitRepository.getAllHabits()
        .flatMapLatest { habits ->
            if (habits.isEmpty()) {
                flowOf(0L)
            } else {
                val today = getDayStart(System.currentTimeMillis())
                combine(
                    habits.map { habit ->
                        habitCompletionRepository.getCompletionForHabitOnDate(habit.id, today)
                            .map { completion -> completion?.xpEarned ?: 0L }
                    }
                ) { xpValues -> xpValues.sum() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    // Combined state for easy access in UI
    val uiState: StateFlow<UiState> = combine(
        statistics,
        habits,
        pet,
        todayCompletionStatuses,
        todayXpProgress
    ) { stats, habList, petState, completionStatuses, xpProgress ->
        UiState(
            globalStreak = stats.globalStreak,
            habits = habList,
            pet = petState,
            completedToday = completionStatuses,
            totalCoins = stats.totalCoins,
            lastStreakDate = stats.lastStreakDate,
            currentCombo = activeCombo(stats),
            lastHabitCompletionTimestamp = stats.lastHabitCompletionTimestamp,
            dailyGoalXp = stats.dailyGoalXp,
            dailyGoalProgressXp = if (stats.dailyGoalDate == getDayStart(System.currentTimeMillis()) / 86_400_000L) xpProgress else 0L,
            dailyGoalCompleted = stats.dailyGoalCompletedDate == getDayStart(System.currentTimeMillis()) / 86_400_000L
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(
            globalStreak = 0,
            habits = emptyList(),
            pet = PetEntity(id = 1),
            completedToday = emptyMap(),
            totalCoins = 0,
            lastStreakDate = 0L,
            currentCombo = 0,
            lastHabitCompletionTimestamp = 0L,
            dailyGoalXp = 300,
            dailyGoalProgressXp = 0L,
            dailyGoalCompleted = false
        )
    )

    data class UiState(
        val globalStreak: Int,
        val habits: List<HabitEntity>,
        val pet: PetEntity,
        val completedToday: Map<Long, Boolean>,
        val totalCoins: Int,
        val lastStreakDate: Long,
        val currentCombo: Int,
        val lastHabitCompletionTimestamp: Long,
        val dailyGoalXp: Int,
        val dailyGoalProgressXp: Long,
        val dailyGoalCompleted: Boolean
    )

    private fun activeCombo(stats: StatisticsEntity): Int {
        return if (ExpConfig.isComboActive(stats.lastHabitCompletionTimestamp, System.currentTimeMillis())) {
            stats.currentCombo
        } else {
            0
        }
    }

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
