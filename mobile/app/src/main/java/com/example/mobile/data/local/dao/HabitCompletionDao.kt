package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobile.data.local.entities.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

data class RecentCompletionsStats(
    val count: Int,
    val lastActivityTimestamp: Long
)

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date >= :startDate AND date < :endDate")
    fun getCompletionsForHabit(habitId: Long, startDate: Long, endDate: Long): Flow<List<HabitCompletionEntity>>

    @Query("""
        SELECT
            COUNT(*) AS count,
            COALESCE(MAX(date), 0) AS lastActivityTimestamp
        FROM habit_completions
        WHERE date >= :startDate AND date < :endDate
    """)
    suspend fun getRecentCompletionsStats(startDate: Long, endDate: Long): RecentCompletionsStats

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    fun getCompletionForHabitOnDate(habitId: Long, date: Long): Flow<HabitCompletionEntity?>

    @Query("SELECT COUNT(*) FROM habit_completions WHERE date = :date")
    suspend fun getCompletionCountOnDate(date: Long): Int

    @Query("SELECT COUNT(DISTINCT date) FROM habit_completions")
    suspend fun getActiveDayCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: HabitCompletionEntity): Long

    @Update
    suspend fun updateCompletion(completion: HabitCompletionEntity): Int

    @Delete
    suspend fun deleteCompletion(completion: HabitCompletionEntity): Int

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: Long)

    @Query("DELETE FROM habit_completions")
    suspend fun deleteAll()

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCompletionForHabitOnDateOnce(
        habitId: Long,
        date: Long
    ): HabitCompletionEntity?
}
