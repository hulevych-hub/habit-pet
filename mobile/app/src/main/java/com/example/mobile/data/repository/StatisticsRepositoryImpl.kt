package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val current = statisticsDao.getStatistics().first() ?: return

        val updated = current.copy(
            totalCoins = current.totalCoins + amount
        )

        statisticsDao.updateStatistics(updated)
    }

    override suspend fun reset() {
        statisticsDao.reset()
    }

    override suspend fun isStreakAlreadyCountedToday(): Boolean {
        val stats = statisticsDao.getStatistics().first() ?: return false

        val today = todayKey()

        return stats.lastStreakDate == today
    }

    override suspend fun markStreakUpdatedToday() {
        val stats = statisticsDao.getStatistics().first() ?: return

        val today = todayKey()

        val updated = stats.copy(
            lastStreakDate = today
        )

        statisticsDao.updateStatistics(updated)
    }

    override suspend fun incrementStreak() {
        val stats = statisticsDao.getStatistics().first() ?: return
        val nextStreak = stats.currentStreak + 1

        val updated = stats.copy(
            currentStreak = nextStreak,
            globalStreak = nextStreak,
            bestStreak = maxOf(stats.bestStreak, nextStreak),
            lastUpdated = System.currentTimeMillis()
        )

        statisticsDao.updateStatistics(updated)
    }

    override suspend fun incrementRewardChestsAvailable(amount: Int) {
        val stats = statisticsDao.getStatistics().first() ?: return

        val updated = stats.copy(
            rewardChestsAvailable = stats.rewardChestsAvailable + amount
        )

        statisticsDao.updateStatistics(updated)
    }

    private fun todayKey(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis / 86_400_000L
    }
}
