package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mobile.data.local.entities.GameEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameEventDao {
    @Query("SELECT * FROM game_events ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getRecentEvents(limit: Int, offset: Int): Flow<List<GameEventEntity>>

    @Insert
    suspend fun insertGameEvent(event: GameEventEntity): Long

    @Query("SELECT COUNT(*) FROM game_events WHERE type = :type")
    suspend fun countByType(type: String): Int
}
