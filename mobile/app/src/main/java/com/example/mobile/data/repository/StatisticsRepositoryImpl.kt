package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsDao: StatisticsDao
) : StatisticsRepository {
    override fun getStatistics(): Flow<StatisticsEntity> =
        statisticsDao.getStatistics().map { it ?: StatisticsEntity(id = 1) }

    override suspend fun updateStatistics(statistics: StatisticsEntity): Int =
        statisticsDao.updateStatistics(statistics)
}
