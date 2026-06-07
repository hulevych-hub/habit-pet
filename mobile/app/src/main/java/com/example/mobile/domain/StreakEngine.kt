package com.example.mobile.domain

import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.firstOrNull

class StreakEngine(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val statisticsRepository: StatisticsRepository,
    private val rewardQueue: RewardQueue
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

            // Check for chest rewards (7-day milestone)
            val stats = statisticsRepository.getStatistics().firstOrNull()
            val currentStreak = stats?.currentStreak ?: 0
            val lastStreakAwardedAt = stats?.lastStreakAwardedAt ?: 0

            // Award chest every 7 days
            if (currentStreak >= 7 && currentStreak % 7 == 0 && currentStreak > lastStreakAwardedAt) {
                // Award chest reward
                val chestEvent = RewardUiEvent.ChestReward(
                    "7_day_streak",
                    50 // 50 coins as chest reward
                )
                rewardQueue.addReward(chestEvent)

                // Update last streak awarded
                statisticsRepository.updateStatistics(
                    stats?.copy(
                        lastStreakAwardedAt = currentStreak
                    ) ?: StatisticsEntity().copy(
                        lastStreakAwardedAt = currentStreak
                    )
                )
            }
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