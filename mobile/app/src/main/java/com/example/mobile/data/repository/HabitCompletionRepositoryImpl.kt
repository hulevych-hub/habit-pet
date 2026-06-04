package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.domain.StreakCalculator
import com.example.mobile.domain.repository.HabitCompletionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class HabitCompletionRepositoryImpl @Inject constructor(
    private val habitCompletionDao: HabitCompletionDao,
    private val habitDao: HabitDao,
    private val statisticsDao: StatisticsDao,
    private val petDao: PetDao
) : HabitCompletionRepository {
    override fun getCompletionsForHabit(habitId: Long, startDate: Long, endDate: Long): Flow<List<HabitCompletionEntity>> =
        habitCompletionDao.getCompletionsForHabit(habitId, startDate, endDate)

    override fun getCompletionForHabitOnDate(habitId: Long, date: Long): Flow<HabitCompletionEntity?> =
        habitCompletionDao.getCompletionForHabitOnDate(habitId, date)

    override suspend fun addCompletion(completion: HabitCompletionEntity): Long {
        val existingCompletion = habitCompletionDao
            .getCompletionForHabitOnDate(completion.habitId, completion.date)
            .firstOrNull()
        if (existingCompletion != null) {
            return existingCompletion.id
        }

        val completionId = habitCompletionDao.insertCompletion(completion)
        if (completionId == -1L) return -1L

        updateHabitStreak(completion.habitId, completion.date)
        updatePetProgress(completion.xpEarned)
        updateStatistics(completion)
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
        val statistics = statisticsDao.getStatistics().firstOrNull() ?: StatisticsEntity(id = 1)
        val globalStreak = calculateGlobalStreakFrom(completionDate)
        val updatedStatistics = statistics.copy(
            id = 1,
            globalStreak = globalStreak,
            currentStreak = globalStreak,
            bestStreak = maxOf(statistics.bestStreak, globalStreak),
            lastUpdated = System.currentTimeMillis()
        )
        upsertStatistics(updatedStatistics)
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

    private suspend fun updateStatistics(completion: HabitCompletionEntity) {
        val statistics = statisticsDao.getStatistics().firstOrNull() ?: StatisticsEntity(id = 1)
        val updatedStatistics = statistics.copy(
            id = 1,
            totalCompletions = statistics.totalCompletions + 1,
            totalHabitsCompleted = statistics.totalHabitsCompleted + 1,
            totalXp = statistics.totalXp + completion.xpEarned,
            daysActive = habitCompletionDao.getActiveDayCount(),
            lastUpdated = System.currentTimeMillis()
        )
        upsertStatistics(updatedStatistics)
    }

    private suspend fun updatePetProgress(xpEarned: Int) {
        val pet = petDao.getPet().firstOrNull() ?: PetEntity(id = 1)
        val newXp = pet.xp + xpEarned
        val newLevel = calculateLevel(newXp)
        val updatedPet = pet.copy(
            id = 1,
            xp = newXp,
            level = newLevel,
            evolutionStage = calculateEvolutionStage(newXp)
        )
        val updatedRows = petDao.updatePet(updatedPet)
        if (updatedRows == 0) {
            petDao.insertPet(updatedPet)
        }
    }

    private fun calculateLevel(xp: Long): Int {
        var level = 0
        var remainingXp = xp
        while (remainingXp >= xpRequiredForNextLevel(level)) {
            remainingXp -= xpRequiredForNextLevel(level)
            level++
        }
        return level
    }

    private fun xpRequiredForNextLevel(level: Int): Long = 100L + (level * 50L)

    private fun calculateEvolutionStage(xp: Long): Int = when {
        xp >= 2_500L -> 4
        xp >= 1_000L -> 3
        xp >= 350L -> 2
        xp >= 100L -> 1
        else -> 0
    }

    private suspend fun calculateGlobalStreakFrom(date: Long): Int {
        return StreakCalculator.calculateConsecutiveStreak(date) { day ->
            areAllHabitsCompletedOnDate(day)
        }
    }

    private suspend fun upsertStatistics(statistics: StatisticsEntity) {
        val updatedRows = statisticsDao.updateStatistics(statistics.copy(id = 1))
        if (updatedRows == 0) {
            statisticsDao.insertStatistics(statistics.copy(id = 1))
        }
    }
}
