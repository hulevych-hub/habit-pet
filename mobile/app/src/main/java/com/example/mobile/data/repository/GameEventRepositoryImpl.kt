package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.GameEventDao
import com.example.mobile.data.local.entities.GameEventEntity
import com.example.mobile.domain.repository.GameEventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GameEventRepositoryImpl @Inject constructor(
    private val gameEventDao: GameEventDao
) : GameEventRepository {
    override fun getRecentEvents(limit: Int, offset: Int): Flow<List<GameEventEntity>> =
        gameEventDao.getRecentEvents(limit, offset)

    override suspend fun logEvent(event: GameEventEntity) {
        gameEventDao.insertGameEvent(event)
    }

    override suspend fun countByType(type: String): Int =
        gameEventDao.countByType(type)
}
