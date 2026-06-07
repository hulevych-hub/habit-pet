package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

        val today = System.currentTimeMillis() / 86_400_000L

        return stats.lastStreakDate == today
    }

    override suspend fun markStreakUpdatedToday() {
        val stats = statisticsDao.getStatistics().first() ?: return

        val today = System.currentTimeMillis() / 86_400_000L

        val updated = stats.copy(
            lastStreakDate = today
        )

        statisticsDao.updateStatistics(updated)
    }

    override suspend fun incrementStreak() {
        val stats = statisticsDao.getStatistics().first() ?: return

        val updated = stats.copy(
            currentStreak = stats.currentStreak + 1
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
}
