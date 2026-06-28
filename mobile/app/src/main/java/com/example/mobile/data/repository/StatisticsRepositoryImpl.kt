package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsDao: StatisticsDao
) : StatisticsRepository {
    override fun getStatistics(): Flow<StatisticsEntity> =
        statisticsDao.getStatistics().map { it ?: StatisticsEntity(id = 1) }

    override suspend fun updateStatistics(statistics: StatisticsEntity): Int {
        val updatedRows = statisticsDao.updateStatistics(statistics.copy(id = 1))
        if (updatedRows == 0) {
            statisticsDao.insertStatistics(statistics.copy(id = 1))
        }
        return updatedRows
    }

    override suspend fun addCoins(amount: Int) {
        val current = statisticsDao.getStatistics().firstOrNull() ?: return

        val updated = current.copy(
            totalCoins = current.totalCoins + amount
        )

        statisticsDao.updateStatistics(updated)
    }

    override suspend fun reset() {
        statisticsDao.reset()
    }

    override suspend fun isStreakAlreadyCountedToday(): Boolean {
        val stats = statisticsDao.getStatistics().firstOrNull() ?: return false
        val today = todayKey()

        return stats.currentStreak > 0 && stats.lastStreakDate == today
    }

    override suspend fun markStreakUpdatedToday() {
        val stats = statisticsDao.getStatistics().firstOrNull() ?: return
        val today = todayKey()

        val updated = stats.copy(
            lastStreakDate = today
        )

        statisticsDao.updateStatistics(updated)
    }

    override suspend fun incrementStreak() {
        val stats = statisticsDao.getStatistics().firstOrNull()
            ?: StatisticsEntity(id = 1)
        val today = todayKey()
        val nextStreak = stats.currentStreak + 1

        val updated = stats.copy(
            id = 1,
            currentStreak = nextStreak,
            globalStreak = nextStreak,
            bestStreak = maxOf(stats.bestStreak, nextStreak),
            lastStreakDate = today,
            lastUpdated = System.currentTimeMillis()
        )

        val rows = statisticsDao.updateStatistics(updated)
        if (rows == 0) {
            statisticsDao.insertStatistics(updated)
        }
    }

    override suspend fun incrementRewardChestsAvailable(amount: Int) {
        val stats = statisticsDao.getStatistics().firstOrNull() ?: return

        val updated = stats.copy(
            rewardChestsAvailable = stats.rewardChestsAvailable + amount
        )

        statisticsDao.updateStatistics(updated)
    }

    override suspend fun syncGlobalStreak() {
        val stats = statisticsDao.getStatistics().firstOrNull() ?: return
        if (stats.globalStreak != stats.currentStreak) {
            statisticsDao.updateStatistics(stats.copy(globalStreak = stats.currentStreak))
        }
    }

    private fun todayKey(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        // Timezone-safe day key: midnight-local is not generally a multiple of 86400000
        // from the UTC epoch, so add the zone+DST offset before dividing.
        val offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
        return (calendar.timeInMillis + offset) / 86_400_000L
    }
}
