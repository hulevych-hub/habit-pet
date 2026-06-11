package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.GameEventEntity
import kotlinx.coroutines.flow.Flow

interface GameEventRepository {
    fun getRecentEvents(limit: Int = 100, offset: Int = 0): Flow<List<GameEventEntity>>
    suspend fun logEvent(event: GameEventEntity)
}
