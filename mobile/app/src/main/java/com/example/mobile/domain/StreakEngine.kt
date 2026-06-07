package com.example.mobile.domain

import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.firstOrNull

class StreakEngine(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val statisticsRepository: StatisticsRepository
) {

    /**
     * Called after completing a habit
     */
    suspend fun evaluateTodayStreak(today: Long) {

        val normalizedToday = normalize(today)

        if (statisticsRepository.isStreakAlreadyCountedToday()) return

        val habits = habitRepository.getAllHabits().firstOrNull().orEmpty()

        if (habits.isEmpty()) return

        val allCompleted = habits.all { habit ->
            habitCompletionRepository
                .getCompletionForHabitOnDate(habit.id, normalizedToday)
                .firstOrNull() != null
        }

        if (allCompleted) {
            statisticsRepository.incrementStreak()
            statisticsRepository.markStreakUpdatedToday()
        }
    }

    /**
     * Called after DELETE or correction
     * Recomputes state safely
     */
    suspend fun recalculateTodayStreak(today: Long) {

        val normalizedToday = normalize(today)

        val habits = habitRepository.getAllHabits().firstOrNull().orEmpty()

        if (habits.isEmpty()) return

        val allCompleted = habits.all { habit ->
            habitCompletionRepository
                .getCompletionForHabitOnDate(habit.id, normalizedToday)
                .firstOrNull() != null
        }

        val alreadyCounted = statisticsRepository.isStreakAlreadyCountedToday()

        when {
            allCompleted && !alreadyCounted -> {
                statisticsRepository.incrementStreak()
                statisticsRepository.markStreakUpdatedToday()
            }

            !allCompleted && alreadyCounted -> {
                // optional safety rollback if you ever support it
            }
        }
    }

    private fun normalize(time: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}