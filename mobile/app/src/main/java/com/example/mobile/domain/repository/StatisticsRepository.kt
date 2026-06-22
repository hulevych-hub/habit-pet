package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.StatisticsEntity
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getStatistics(): Flow<StatisticsEntity>
    suspend fun updateStatistics(statistics: StatisticsEntity): Int

    suspend fun addCoins(amount: Int)

    suspend fun reset()

    suspend fun markStreakUpdatedToday()

    suspend fun isStreakAlreadyCountedToday(): Boolean

    suspend fun incrementStreak()

    suspend fun incrementRewardChestsAvailable(amount: Int)

    suspend fun syncGlobalStreak()
}