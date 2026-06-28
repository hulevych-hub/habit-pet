package com.example.mobile.domain

import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyLoginStreakEngine @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val rewardQueue: RewardQueue
) {
    companion object {
        private const val MILLISECONDS_PER_DAY = 86_400_000L

        val LOGIN_MILESTONES = listOf(3, 7, 14, 30, 60, 100)

        fun rewardForMilestone(streak: Int): LoginStreakReward = when (streak) {
            3 -> LoginStreakReward(coins = 50)
            7 -> LoginStreakReward(coins = 100, expAmount = 50)
            14 -> LoginStreakReward(coins = 200, expAmount = 100)
            30 -> LoginStreakReward(coins = 500, expAmount = 200)
            60 -> LoginStreakReward(coins = 1000, expAmount = 500)
            100 -> LoginStreakReward(coins = 2500, expAmount = 1000)
            else -> LoginStreakReward(coins = 25)
        }
    }

    data class LoginStreakReward(
        val coins: Int = 0,
        val expAmount: Int = 0
    )

    data class LoginStreakResult(
        val streak: Int,
        val isNewMilestone: Boolean,
        val milestone: Int?
    )

    /**
     * Called once at app launch. Checks if this is a new day, updates the login streak,
     * and queues rewards if a milestone was reached.
     */
    suspend fun checkAndUpdateLoginStreak(now: Long): LoginStreakResult {
        val stats = statisticsRepository.getStatistics().firstOrNull()
            ?: StatisticsEntity(id = 1)

        val todayKey = dayKey(now)
        val lastLoginKey = stats.lastDailyLoginDate

        // Already logged in today — no update needed
        if (lastLoginKey == todayKey) {
            return LoginStreakResult(
                streak = stats.dailyLoginStreak,
                isNewMilestone = false,
                milestone = null
            )
        }

        val yesterdayKey = todayKey - 1
        val isConsecutive = lastLoginKey == yesterdayKey

        val newStreak = if (isConsecutive) {
            stats.dailyLoginStreak + 1
        } else {
            1 // Reset streak — missed a day or first login
        }

        val milestone = LOGIN_MILESTONES.firstOrNull { it == newStreak }
        val isNewMilestone = milestone != null && milestone > stats.lastDailyLoginRewardDay

        // Persist the updated streak
        statisticsRepository.updateStatistics(
            stats.copy(
                dailyLoginStreak = newStreak,
                lastDailyLoginDate = todayKey,
                lastDailyLoginRewardDay = if (isNewMilestone) milestone!! else stats.lastDailyLoginRewardDay,
                lastUpdated = now
            )
        )

        // Queue reward for milestone
        if (isNewMilestone && milestone != null) {
            val reward = rewardForMilestone(milestone)
            if (reward.coins > 0) {
                rewardQueue.addReward(RewardUiEvent.CoinReward(amount = reward.coins))
            }
            if (reward.expAmount > 0) {
                rewardQueue.addReward(RewardUiEvent.ExpReward(amount = reward.expAmount.toLong()))
            }
            rewardQueue.addReward(
                RewardUiEvent.StreakReward(
                    streak = newStreak,
                    coins = reward.coins,
                    rewardSummary = listOf("$newStreak-Day Login Streak", "Login milestone reward")
                )
            )
        }

        return LoginStreakResult(
            streak = newStreak,
            isNewMilestone = isNewMilestone,
            milestone = if (isNewMilestone) milestone else null
        )
    }

    private fun dayKey(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
        return (calendar.timeInMillis + offset) / MILLISECONDS_PER_DAY
    }
}
