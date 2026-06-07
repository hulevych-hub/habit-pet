package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobile.data.local.entities.StatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics WHERE id = 1")
    fun getStatistics(): Flow<StatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: StatisticsEntity): Long

    @Update
    suspend fun updateStatistics(statistics: StatisticsEntity): Int

    @Query("""
UPDATE statistics 
SET 
    totalCoins = 0,
    totalCompletions = 0,
    currentStreak = 0,
    bestStreak = 0,
    globalStreak = 0,
    totalXp = 0,
    daysActive = 0,
    totalHabitsCompleted = 0,
    rewardChestsAvailable = 0,
    lastStreakDate = 0,
    lastUpdated = 0
""")
    suspend fun reset()

    @Query("SELECT COUNT(*) FROM statistics")
    suspend fun count(): Int
}
