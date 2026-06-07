package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobile.data.local.entities.HabitProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitProgressDao {

    @Query("SELECT * FROM habit_progress WHERE habitId = :habitId AND date = :date")
    fun getProgress(habitId: Long, date: Long): Flow<HabitProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: HabitProgressEntity)

    @Query("DELETE FROM habit_progress WHERE habitId = :habitId")
    suspend fun clear(habitId: Long)

    @Query("DELETE FROM habit_progress")
    suspend fun deleteAll()
}