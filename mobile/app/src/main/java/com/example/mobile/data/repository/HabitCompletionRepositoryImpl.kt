package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
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

    override fun getCompletionsForHabit(
        habitId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<HabitCompletionEntity>> =
        habitCompletionDao.getCompletionsForHabit(habitId, startDate, endDate)

    override fun getCompletionForHabitOnDate(
        habitId: Long,
        date: Long
    ): Flow<HabitCompletionEntity?> =
        habitCompletionDao.getCompletionForHabitOnDate(habitId, date)

    override suspend fun addCompletion(completion: HabitCompletionEntity): Long {

        val existingCompletion =
            habitCompletionDao.getCompletionForHabitOnDateOnce(
                completion.habitId,
                completion.date
            )

        if (existingCompletion != null) {
            return existingCompletion.id
        }

        val completionId = habitCompletionDao.insertCompletion(completion)
        if (completionId == -1L) return -1L

        updateHabitStreak(completion.habitId, completion.date)
        updatePetProgress(completion.xpEarned)
        updateStatistics(completion)

        return completionId
    }

    override suspend fun updateCompletion(completion: HabitCompletionEntity): Int =
        habitCompletionDao.updateCompletion(completion)

    override suspend fun deleteCompletion(completion: HabitCompletionEntity): Int =
        habitCompletionDao.deleteCompletion(completion)

    override suspend fun deleteAll() =
        habitCompletionDao.deleteAll()

    // NEW: correct streak check for engine
    override suspend fun hasAnyCompletionOnDate(date: Long): Boolean {
        return habitCompletionDao.getCompletionCountOnDate(date) > 0
    }

    override suspend fun areAllHabitsCompletedOnDate(date: Long): Boolean {
        val habits = habitDao.getAllHabits().firstOrNull() ?: return true

        return habits.all { habit ->
            habitCompletionDao.getCompletionForHabitOnDateOnce(habit.id, date) != null
        }
    }

    // -------------------------
    // STREAK LOGIC (UNCHANGED BUT NOW SAFE)
    // -------------------------

    private suspend fun updateHabitStreak(habitId: Long, completionDate: Long) {

        val habit = habitDao.getHabitById(habitId).firstOrNull() ?: return

        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = completionDate
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)

        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        val yesterday = calendar.timeInMillis

        val hadYesterday =
            habitCompletionDao.getCompletionForHabitOnDateOnce(habitId, yesterday) != null

        val newStreak = if (hadYesterday) habit.currentStreak + 1 else 1

        val updated = habit.copy(
            currentStreak = newStreak,
            bestStreak = maxOf(habit.bestStreak, newStreak)
        )

        habitDao.updateHabit(updated)
    }

    private suspend fun updateStatistics(completion: HabitCompletionEntity) {
        val stats = statisticsDao.getStatistics().firstOrNull()
            ?: StatisticsEntity(id = 1)

        val updated = stats.copy(
            id = 1,
            totalCompletions = stats.totalCompletions + 1,
            totalHabitsCompleted = stats.totalHabitsCompleted + 1,
            totalXp = stats.totalXp + completion.xpEarned,
            daysActive = habitCompletionDao.getActiveDayCount(),
            lastUpdated = System.currentTimeMillis()
        )

        upsertStatistics(updated)
    }

    private suspend fun updatePetProgress(xpEarned: Long) {
        val pet = petDao.getPet().firstOrNull() ?: PetEntity(id = 1)

        val newXp = pet.xp + xpEarned
        val newLevel = calculateLevel(newXp)

        val updated = pet.copy(
            id = 1,
            xp = newXp,
            level = newLevel,
            evolutionStage = calculateEvolutionStage(newXp)
        )

        petDao.updatePet(updated)
    }

    private fun calculateLevel(xp: Long): Int {
        var level = 0
        var remaining = xp

        while (remaining >= (100 + level * 50)) {
            remaining -= (100 + level * 50)
            level++
        }

        return level
    }

    private fun calculateEvolutionStage(xp: Long): Int = when {
        xp >= 2500 -> 4
        xp >= 1000 -> 3
        xp >= 350 -> 2
        xp >= 100 -> 1
        else -> 0
    }

    private suspend fun upsertStatistics(statistics: StatisticsEntity) {
        val updated = statisticsDao.updateStatistics(statistics.copy(id = 1))
        if (updated == 0) {
            statisticsDao.insertStatistics(statistics.copy(id = 1))
        }
    }
}
