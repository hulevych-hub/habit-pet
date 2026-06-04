package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mobile.data.local.entities.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date >= :startDate AND date < :endDate")
    fun getCompletionsForHabit(habitId: Long, startDate: Long, endDate: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    fun getCompletionForHabitOnDate(habitId: Long, date: Long): Flow<HabitCompletionEntity?>

    @Insert
    suspend fun insertCompletion(completion: HabitCompletionEntity): Long

    @Update
    suspend fun updateCompletion(completion: HabitCompletionEntity): Int

    @Delete
    suspend fun deleteCompletion(completion: HabitCompletionEntity): Int
}