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
}
