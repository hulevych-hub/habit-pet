package com.example.mobile.data.local.database

import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.StatisticsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDatabaseInitializer @Inject constructor(
    private val statisticsDao: StatisticsDao
) {

    suspend fun initialize() {
        val count = statisticsDao.count()

        if (count == 0) {
            statisticsDao.insertStatistics(
                StatisticsEntity(
                    id = 1,
                    totalCoins = 0,
                    totalCompletions = 0,
                    currentStreak = 0,
                    lastUpdated = -1L
                )
            )
        }
    }

    fun initializeStatisticsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            initialize()
        }
    }
}