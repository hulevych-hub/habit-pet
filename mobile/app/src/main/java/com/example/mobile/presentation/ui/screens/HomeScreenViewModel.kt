package com.example.mobile.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ChallengeEngine
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.ChallengeUiState
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.components.StreakCalendarBuilder
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
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
    private val achievementRepository: AchievementRepository,
    private val challengeEngine: ChallengeEngine
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _streakCalendarState = MutableStateFlow<StreakCalendarUiState?>(null)
    val streakCalendarState: StateFlow<StreakCalendarUiState?> = _streakCalendarState.asStateFlow()

    fun resetAllGameData() {
        viewModelScope.launch {
            habitRepository.deleteAll()
            habitCompletionRepository.deleteAll()
            petRepository.resetPet()
            statisticsRepository.reset()
            achievementRepository.reset()
            challengeEngine.reset()
        }
    }

    fun renamePet(name: String) {
        viewModelScope.launch {
            val currentPet = pet.value
            petRepository.updatePet(currentPet.copy(id = 1, name = name))
        }
    }

    fun openGlobalStreakCalendar() {
        viewModelScope.launch {
            val monthStart = getMonthStart(System.currentTimeMillis())
            _streakCalendarState.value = StreakCalendarUiState.loading(
                title = "Global Streak",
                subtitle = "Loading your rhythm calendar...",
                monthStart = monthStart
            )
            _streakCalendarState.value = StreakCalendarBuilder.buildGlobal(
                monthStart = monthStart,
                habits = habits.value,
                completionRepository = habitCompletionRepository
            )
        }
    }

    fun showPreviousStreakMonth() {
        val current = _streakCalendarState.value ?: return
        if (!current.canNavigatePrevious) return

        viewModelScope.launch {
            val previousMonthStart = addMonths(current.monthStart, -1)
            _streakCalendarState.value = StreakCalendarUiState.loading(
                title = current.title,
                subtitle = current.subtitle,
                monthStart = previousMonthStart
            )
            _streakCalendarState.value = StreakCalendarBuilder.buildGlobal(
                monthStart = previousMonthStart,
                habits = habits.value,
                completionRepository = habitCompletionRepository
            )
        }
    }

    fun closeStreakCalendar() {
        _streakCalendarState.value = null
    }

    fun claimChallenge() {
        viewModelScope.launch {
            challengeEngine.claimActiveChallenge()
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

    val challengeUiState: StateFlow<ChallengeUiState> = challengeEngine.getActiveChallengeUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChallengeUiState.empty()
        )

    private val todayCompletionXp: StateFlow<Map<Long, Long>> = habitRepository.getAllHabits()
        .flatMapLatest { habits ->
            if (habits.isEmpty()) {
                flowOf(emptyMap())
            } else {
                val today = getDayStart(System.currentTimeMillis())
                combine(
                    habits.map { habit ->
                        habitCompletionRepository.getCompletionForHabitOnDate(habit.id, today)
                            .map { completion ->
                                completion?.let { habit.id to it.xpEarned }
                            }
                    }
                ) { pairs -> pairs.filterNotNull().toMap() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    init {
        viewModelScope.launch {
            challengeEngine.ensureActiveChallenge()
        }

        viewModelScope.launch {
            combine(
                statistics,
                habits,
                pet,
                todayCompletionXp
            ) { _, _, _, _ -> }
                .take(1)
                .collect { _isLoading.value = false }
        }
    }

    // Combined state for easy access in UI
    val uiState: StateFlow<UiState> = combine(
        statistics,
        habits,
        pet,
        todayCompletionXp
    ) { stats, habList, petState, completionXp ->
        UiState(
            globalStreak = stats.globalStreak,
            habits = habList,
            pet = petState,
            completedTodayXp = completionXp,
            totalCoins = stats.totalCoins,
            lastStreakDate = stats.lastStreakDate,
            currentCombo = activeCombo(stats),
            lastHabitCompletionTimestamp = stats.lastHabitCompletionTimestamp,
            globalStreakCompletedToday = stats.lastStreakDate == getDayStart(System.currentTimeMillis()) / 86_400_000L
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(
            globalStreak = 0,
            habits = emptyList(),
            pet = PetEntity(id = 1),
            completedTodayXp = emptyMap(),
            totalCoins = 0,
            lastStreakDate = 0L,
            currentCombo = 0,
            lastHabitCompletionTimestamp = 0L,
            globalStreakCompletedToday = false
        )
    )

    data class UiState(
        val globalStreak: Int,
        val habits: List<HabitEntity>,
        val pet: PetEntity,
        val completedTodayXp: Map<Long, Long>,
        val totalCoins: Int,
        val lastStreakDate: Long,
        val currentCombo: Int,
        val lastHabitCompletionTimestamp: Long,
        val globalStreakCompletedToday: Boolean
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

    private fun getMonthStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.timeInMillis
    }

    private fun addMonths(monthStart: Long, months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = monthStart
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }
}
