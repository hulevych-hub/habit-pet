package com.example.mobile.domain

import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ChestRewardFactory
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.firstOrNull

class StreakEngine(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val statisticsRepository: StatisticsRepository,
    private val rewardQueue: RewardQueue,
    private val inventoryItemRepository: InventoryItemRepository,
    private val activityTimelineEngine: ActivityTimelineEngine,
    private val dragonMoodEngine: DragonMoodEngine,
    private val challengeRepository: ChallengeRepository
) {

    companion object {
        private const val FREEZE_COOLDOWN_DAYS = 7L
        private const val MILLISECONDS_PER_DAY = 86_400_000L

        private val STREAK_MILESTONES = listOf(7, 14, 30, 60, 100)

        private fun getChestTypeForMilestone(streak: Int): ChestType = when (streak) {
            14 -> ChestType.RARE
            30, 60 -> ChestType.EPIC
            100 -> ChestType.LEGENDARY
            else -> ChestType.NORMAL
        }

        private fun formatChestLabel(chestType: ChestType): String = when (chestType) {
            ChestType.NORMAL -> "Normal"
            ChestType.RARE -> "Rare"
            ChestType.EPIC -> "Epic"
            ChestType.LEGENDARY -> "Legendary"
        }
    }

    data class StreakFreezePrompt(
        val streak: Int,
        val frozenDateKey: Long
    )

    /**
     * Called after completing a habit
     */
    suspend fun evaluateTodayStreak(today: Long) {

        val normalizedToday = getDayStart(today)
        val todayKey = dayKey(today)

        if (statisticsRepository.isStreakAlreadyCountedToday()) return

        val habits = habitRepository.getAllHabits().firstOrNull().orEmpty()

        if (habits.isEmpty()) return

        val allCompleted = habitCompletionRepository.areAllHabitsCompletedOnDate(normalizedToday)

        if (allCompleted) {
            val stats = statisticsRepository.getStatistics().firstOrNull() ?: StatisticsEntity(id = 1)

            if (stats.currentStreak > 0 && !isContinuationDay(todayKey, stats, habits)) {
                resetBrokenStreak(stats, System.currentTimeMillis())
            }

            statisticsRepository.incrementStreak()
            statisticsRepository.markStreakUpdatedToday()
            dragonMoodEngine.refreshMood()

            val updatedStats = statisticsRepository.getStatistics().firstOrNull()
            val currentStreak = updatedStats?.currentStreak ?: stats.currentStreak + 1
            challengeRepository.recordStreak(currentStreak)
            val lastStreakAwardedAt = updatedStats?.lastStreakAwardedAt ?: 0
            val milestone = STREAK_MILESTONES
                .filter { currentStreak >= it && it > lastStreakAwardedAt }
                .lastOrNull()

            if (milestone != null) {
                val milestoneChestType = getChestTypeForMilestone(milestone)
                val streakChestReward = ChestRewardFactory.buildChestReward(
                    rewardType = "global_streak_${milestone}_${milestoneChestType.name.lowercase()}",
                    chestType = milestoneChestType,
                    inventoryItemRepository = inventoryItemRepository
                )
                val chestConfig = ChestRewardConfigProvider.getConfig(milestoneChestType)
                val streakChestSummary = buildRewardSummary(
                    streak = currentStreak,
                    chestType = milestoneChestType,
                    coinAmount = (streakChestReward.amount as? Int) ?: 0,
                    expAmount = streakChestReward.expAmount,
                    customizationId = streakChestReward.customizationId,
                    hasCustomizationChance = chestConfig.customizationRarity != null
                )

                rewardQueue.addReward(
                    RewardUiEvent.StreakReward(
                        streak = currentStreak,
                        coins = 0,
                        rewardSummary = streakChestSummary
                    )
                )
                rewardQueue.addReward(streakChestReward)
                activityTimelineEngine.logStreakMilestone(currentStreak, milestoneChestType)

                statisticsRepository.updateStatistics(
                    updatedStats?.copy(lastStreakAwardedAt = milestone)
                        ?: StatisticsEntity().copy(lastStreakAwardedAt = milestone)
                )
            }
        }
    }

    /**
     * Called after DELETE or correction
     * Recomputes state safely
     */
    suspend fun recalculateTodayStreak(today: Long) {

        val normalizedToday = getDayStart(today)
        val todayKey = dayKey(today)

        val stats = statisticsRepository.getStatistics().firstOrNull() ?: StatisticsEntity(id = 1)
        val habits = habitRepository.getAllHabits().firstOrNull().orEmpty()

        if (habits.isEmpty()) return

        maybeResetBrokenStreakIfNeeded(todayKey, stats, habits)

        val allCompleted = habitCompletionRepository.areAllHabitsCompletedOnDate(normalizedToday)

        val alreadyCounted = statisticsRepository.isStreakAlreadyCountedToday()

        when {
            allCompleted && !alreadyCounted -> evaluateTodayStreak(today)
            !allCompleted && alreadyCounted -> {
                // optional safety rollback if you ever support it
            }
        }
    }

    suspend fun checkPendingStreakFreeze(now: Long): StreakFreezePrompt? {
        val stats = statisticsRepository.getStatistics().firstOrNull() ?: StatisticsEntity(id = 1)
        val habits = habitRepository.getAllHabits().firstOrNull().orEmpty()

        if (habits.isEmpty()) return null

        val todayStart = getDayStart(now)
        val todayKey = dayKey(now)
        val yesterdayKey = todayKey - 1

        if (habitCompletionRepository.areAllHabitsCompletedOnDate(todayStart)) {
            evaluateTodayStreak(now)
            return null
        }

        if (stats.currentStreak <= 0) return null
        if (isDayCompletedOrFrozen(yesterdayKey, stats, habits)) return null
        if (canFreezeDate(frozenDateKey = yesterdayKey, todayKey = todayKey, stats = stats, habits = habits)) {
            return StreakFreezePrompt(
                streak = stats.currentStreak,
                frozenDateKey = yesterdayKey
            )
        }

        resetBrokenStreak(stats, now)
        return null
    }

    suspend fun useStreakFreeze(now: Long): Boolean {
        val prompt = checkPendingStreakFreeze(now) ?: return false
        val stats = statisticsRepository.getStatistics().firstOrNull() ?: return false
        val freezeDateKey = dayKey(now)
        val frozenDates = StatisticsEntity.parseFreezeDates(stats.streakFreezeDatesJson).toMutableSet()
        frozenDates.add(prompt.frozenDateKey)

        statisticsRepository.updateStatistics(
            stats.copy(
                lastStreakFreezeDate = freezeDateKey,
                lastFrozenStreakDate = prompt.frozenDateKey,
                streakFreezeDatesJson = StatisticsEntity.freezeDatesToJson(frozenDates),
                lastUpdated = now
            )
        )

        if (habitCompletionRepository.areAllHabitsCompletedOnDate(getDayStart(now))) {
            evaluateTodayStreak(now)
        } else {
            dragonMoodEngine.refreshMood()
        }

        return true
    }

    suspend fun resetBrokenStreak(now: Long) {
        val stats = statisticsRepository.getStatistics().firstOrNull() ?: return
        if (stats.currentStreak <= 0) return

        resetBrokenStreak(stats, now)
    }

    private suspend fun canFreezeDate(
        frozenDateKey: Long,
        todayKey: Long,
        stats: StatisticsEntity,
        habits: List<HabitEntity>
    ): Boolean {
        if (stats.currentStreak <= 0) return false
        if (isDayCompletedOrFrozen(frozenDateKey, stats, habits)) return false
        if (stats.lastFrozenStreakDate == frozenDateKey - 1) return false
        if (stats.lastStreakFreezeDate > 0 && todayKey - stats.lastStreakFreezeDate < FREEZE_COOLDOWN_DAYS) {
            return false
        }

        return isDayCompletedOrFrozen(frozenDateKey - 1, stats, habits)
    }

    private suspend fun isContinuationDay(
        todayKey: Long,
        stats: StatisticsEntity,
        habits: List<HabitEntity>
    ): Boolean {
        if (stats.currentStreak <= 0) return true

        return isDayCompletedOrFrozen(todayKey - 1, stats, habits)
    }

    private suspend fun maybeResetBrokenStreakIfNeeded(
        todayKey: Long,
        stats: StatisticsEntity,
        habits: List<HabitEntity>
    ) {
        val yesterdayKey = todayKey - 1
        val dayBeforeYesterdayKey = yesterdayKey - 1

        if (!isDayCompletedOrFrozen(yesterdayKey, stats, habits) &&
            !isDayCompletedOrFrozen(dayBeforeYesterdayKey, stats, habits)
        ) {
            resetBrokenStreak(stats, System.currentTimeMillis())
        }
    }

    private suspend fun resetBrokenStreak(stats: StatisticsEntity, now: Long) {
        statisticsRepository.updateStatistics(
            stats.copy(
                currentStreak = 0,
                globalStreak = 0,
                lastStreakDate = 0L,
                lastUpdated = now
            )
        )
        dragonMoodEngine.refreshMood()
    }

    private suspend fun isDayCompletedOrFrozen(
        dateKey: Long,
        stats: StatisticsEntity,
        habits: List<HabitEntity>
    ): Boolean {
        if (habits.isEmpty()) return true
        if (StatisticsEntity.parseFreezeDates(stats.streakFreezeDatesJson).contains(dateKey)) return true

        return habitCompletionRepository.areAllHabitsCompletedOnDate(getDayStart(dateKey * MILLISECONDS_PER_DAY))
    }

    private fun dayKey(time: Long): Long = getDayStart(time) / MILLISECONDS_PER_DAY

    private fun getDayStart(time: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun buildRewardSummary(
        streak: Int,
        chestType: ChestType,
        coinAmount: Int,
        expAmount: Int,
        customizationId: Long?,
        hasCustomizationChance: Boolean
    ): List<String> {
        val summary = mutableListOf(
            "$streak Day Streak Milestone",
            "${formatChestLabel(chestType)} Reward Chest"
        )

        if (coinAmount > 0) {
            summary.add("+$coinAmount coins")
        }

        if (expAmount > 0) {
            summary.add("+$expAmount EXP")
        }

        if (customizationId != null) {
            summary.add("Customization unlocked")
        } else if (hasCustomizationChance) {
            summary.add("Customization chance")
        }

        return summary
    }

}
