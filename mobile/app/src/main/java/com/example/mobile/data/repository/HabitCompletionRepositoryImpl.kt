package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.domain.repository.HabitCompletionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class HabitCompletionRepositoryImpl @Inject constructor(
    private val habitCompletionDao: HabitCompletionDao,
    private val habitDao: HabitDao,
    private val statisticsDao: StatisticsDao
) : HabitCompletionRepository {
    override fun getCompletionsForHabit(habitId: Long, startDate: Long, endDate: Long): Flow<List<HabitCompletionEntity>> =
        habitCompletionDao.getCompletionsForHabit(habitId, startDate, endDate)

    override fun getCompletionForHabitOnDate(habitId: Long, date: Long): Flow<HabitCompletionEntity?> =
        habitCompletionDao.getCompletionForHabitOnDate(habitId, date)

    override suspend fun addCompletion(completion: HabitCompletionEntity): Long {
        // Add the completion
        val completionId = habitCompletionDao.insertCompletion(completion)

        // Update habit streak
        updateHabitStreak(completion.habitId, completion.date)

        // Update global streak
        updateGlobalStreak(completion.date)

        return completionId
    }

    override suspend fun updateCompletion(completion: HabitCompletionEntity): Int =
        habitCompletionDao.updateCompletion(completion)

    override suspend fun deleteCompletion(completion: HabitCompletionEntity): Int =
        habitCompletionDao.deleteCompletion(completion)

    private suspend fun updateHabitStreak(habitId: Long, completionDate: Long) {
        // Get the habit
        val habitOptional = habitDao.getHabitById(habitId).firstOrNull()
        if (habitOptional == null) {
            return // Habit not found, nothing to update
        }
        val habit = habitOptional

        // Get yesterday's date (start of day)
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = completionDate
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val yesterdayStart = calendar.timeInMillis

        // Check if there was a completion yesterday
        val yesterdayCompletionOptional = habitCompletionDao.getCompletionForHabitOnDate(habitId, yesterdayStart).firstOrNull()
        if (yesterdayCompletionOptional != null) {
            // Consecutive day - increment streak
            val newCurrentStreak = habit.currentStreak + 1
            val newBestStreak = Math.max(habit.bestStreak, newCurrentStreak)

            // Update habit
            val updatedHabit = habit.copy(
                currentStreak = newCurrentStreak,
                bestStreak = newBestStreak
            )
            habitDao.updateHabit(updatedHabit)
        } else {
            // Not consecutive - reset streak to 1 (for today's completion)
            val newCurrentStreak = 1
            val newBestStreak = Math.max(habit.bestStreak, newCurrentStreak)

            // Update habit
            val updatedHabit = habit.copy(
                currentStreak = newCurrentStreak,
                bestStreak = newBestStreak
            )
            habitDao.updateHabit(updatedHabit)
        }
    }

    private suspend fun updateGlobalStreak(completionDate: Long) {
        // Get statistics
        val statisticsOptional = statisticsDao.getStatistics().firstOrNull()
        if (statisticsOptional == null) {
            return // Statistics not found, nothing to update
        }
        val statistics = statisticsOptional

        // Get yesterday's date (start of day)
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = completionDate
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val yesterdayStart = calendar.timeInMillis

        // Check if all habits were completed yesterday
        val allHabitsCompletedYesterday = areAllHabitsCompletedOnDate(yesterdayStart)

        if (allHabitsCompletedYesterday) {
            // Consecutive day - increment global streak
            val newGlobalStreak = statistics.globalStreak + 1

            // Update statistics
            val updatedStatistics = statistics.copy(
                globalStreak = newGlobalStreak
            )
            statisticsDao.updateStatistics(updatedStatistics)
        } else {
            // Not consecutive - reset global streak to 1 (for today's completion if all habits completed)
            val allHabitsCompletedToday = areAllHabitsCompletedOnDate(completionDate)
            val newGlobalStreak = if (allHabitsCompletedToday) 1 else 0

            // Update statistics
            val updatedStatistics = statistics.copy(
                globalStreak = newGlobalStreak
            )
            statisticsDao.updateStatistics(updatedStatistics)
        }
    }

    private suspend fun areAllHabitsCompletedOnDate(date: Long): Boolean {
        // Get all habits
        val allHabitsOptional = habitDao.getAllHabits().firstOrNull()
        if (allHabitsOptional == null || allHabitsOptional.isEmpty()) {
            return true // No habits to check, consider as completed
        }
        val allHabits = allHabitsOptional

        // For each habit, check if completed on the given date
        for (habit in allHabits) {
            val completionOptional = habitCompletionDao.getCompletionForHabitOnDate(habit.id, date).firstOrNull()
            if (completionOptional == null) {
                return false // Found a habit not completed today
            }
        }
        return true // All habits completed
    }
}