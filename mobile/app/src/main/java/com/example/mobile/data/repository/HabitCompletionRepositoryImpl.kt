package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.RecentCompletionsStats
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.EconomyConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitCompletionResult
import com.example.mobile.domain.repository.HabitProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class HabitCompletionRepositoryImpl @Inject constructor(
    private val habitCompletionDao: HabitCompletionDao,
    private val habitDao: HabitDao,
    private val habitProgressRepository: HabitProgressRepository,
    private val statisticsDao: StatisticsDao
) : HabitCompletionRepository {

    override fun getCompletionsForHabit(
        habitId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<HabitCompletionEntity>> =
        habitCompletionDao.getCompletionsForHabit(habitId, startDate, endDate)

    override suspend fun getRecentCompletionsStats(
        startDate: Long,
        endDate: Long
    ): RecentCompletionsStats =
        habitCompletionDao.getRecentCompletionsStats(startDate, endDate)

    override fun getCompletionForHabitOnDate(
        habitId: Long,
        date: Long
    ): Flow<HabitCompletionEntity?> =
        habitCompletionDao.getCompletionForHabitOnDate(habitId, date)

    override suspend fun addCompletion(completion: HabitCompletionEntity): Long =
        addCompletionWithCombo(completion).completionId

    override suspend fun addCompletionWithCombo(completion: HabitCompletionEntity): HabitCompletionResult {

        val existingCompletion =
            habitCompletionDao.getCompletionForHabitOnDateOnce(
                completion.habitId,
                completion.date
            )

        if (existingCompletion != null) {
            return HabitCompletionResult(
                completionId = existingCompletion.id,
                baseXpEarned = completion.xpEarned,
                comboBonusXp = 0L,
                totalXpEarned = existingCompletion.xpEarned,
                combo = 0,
                comboMultiplier = 1f,
                comboMilestoneReached = false,
                isNewCompletion = false
            )
        }

        val completionTimestamp = completion.id.takeIf { it > 0L } ?: System.currentTimeMillis()
        val stats = statisticsDao.getStatistics().firstOrNull() ?: StatisticsEntity(id = 1)
        val comboActive = ExpConfig.isComboActive(
            lastHabitCompletionTimestamp = stats.lastHabitCompletionTimestamp,
            now = completionTimestamp
        )
        val nextCombo = if (comboActive) stats.currentCombo + 1 else 1
        val comboBonusXp = ExpConfig.comboBonusXp(nextCombo)
        val completionWithCombo = completion.copy(xpEarned = completion.xpEarned + comboBonusXp)

        val completionId = habitCompletionDao.insertCompletion(completionWithCombo)
        if (completionId == -1L) {
            return HabitCompletionResult(
                completionId = -1L,
                baseXpEarned = completion.xpEarned,
                comboBonusXp = comboBonusXp,
                totalXpEarned = completionWithCombo.xpEarned,
                combo = nextCombo,
                comboMultiplier = ExpConfig.comboMultiplier(nextCombo),
                comboMilestoneReached = ExpConfig.comboMilestoneReached(nextCombo),
                isNewCompletion = false
            )
        }

        updateHabitStreak(completion.habitId, completion.date)
        updateStatistics(completionWithCombo, completionTimestamp, nextCombo)

        return HabitCompletionResult(
            completionId = completionId,
            baseXpEarned = completion.xpEarned,
            comboBonusXp = comboBonusXp,
            totalXpEarned = completionWithCombo.xpEarned,
            combo = nextCombo,
            comboMultiplier = ExpConfig.comboMultiplier(nextCombo),
            comboMilestoneReached = ExpConfig.comboMilestoneReached(nextCombo),
            isNewCompletion = true
        )
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

    override suspend fun isPartialCompletionOnDate(date: Long): Boolean {
        val habits = habitDao.getAllHabits().firstOrNull() ?: return false
        if (habits.isEmpty()) return false

        val completedCount = habits.count { habit ->
            habitCompletionDao.getCompletionForHabitOnDateOnce(habit.id, date) != null
        }
        return completedCount in 1 until habits.size
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

    private suspend fun updateStatistics(
        completion: HabitCompletionEntity,
        completionTimestamp: Long,
        combo: Int
    ) {
        val stats = statisticsDao.getStatistics().firstOrNull()
            ?: StatisticsEntity(id = 1)

        val habit = habitDao.getHabitById(completion.habitId).firstOrNull()
        val coinsEarned = when (habit?.type) {
            "CHECKBOX" -> EconomyConfig.CHECKBOX_HABIT_COINS
            "TIMER" -> calculateTimerHabitCoins(completion)
            else -> 0
        }
        val updated = stats.copy(
            id = 1,
            totalCompletions = stats.totalCompletions + 1,
            totalHabitsCompleted = stats.totalHabitsCompleted + 1,
            totalXp = stats.totalXp + completion.xpEarned,
            daysActive = habitCompletionDao.getActiveDayCount(),
            currentCombo = combo,
            bestCombo = maxOf(stats.bestCombo, combo),
            lastHabitCompletionTimestamp = completionTimestamp,
            lastUpdated = System.currentTimeMillis()
        )

        upsertStatistics(updated)
    }

    private suspend fun calculateTimerHabitCoins(completion: HabitCompletionEntity): Int {
        val accumulatedMinutes = habitProgressRepository
            .getProgress(completion.habitId, completion.date)
            .firstOrNull()
            ?.accumulatedMinutes
            ?: 0

        return EconomyConfig.TIMER_HABIT_BASE_COINS +
            (accumulatedMinutes * EconomyConfig.TIMER_HABIT_COINS_PER_MINUTE)
    }

    private suspend fun upsertStatistics(statistics: StatisticsEntity) {
        val updated = statisticsDao.updateStatistics(statistics.copy(id = 1))
        if (updated == 0) {
            statisticsDao.insertStatistics(statistics.copy(id = 1))
        }
    }
}
