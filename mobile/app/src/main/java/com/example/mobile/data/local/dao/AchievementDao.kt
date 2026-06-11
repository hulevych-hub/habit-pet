package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobile.data.local.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY id")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    fun getAchievementById(achievementId: String): Flow<AchievementEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: AchievementEntity): Long

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity): Int

    @Query(
        "UPDATE achievements " +
            "SET progress = :progress, isUnlocked = :isUnlocked " +
            "WHERE id = :achievementId"
    )
    suspend fun updateProgress(
        achievementId: String,
        progress: Int,
        isUnlocked: Boolean
    ): Int

    @Query("UPDATE achievements SET isClaimed = 1 WHERE id = :achievementId")
    suspend fun markClaimed(achievementId: String): Int

    @Query("UPDATE achievements SET isUnlocked = 0, isClaimed = 0, unlockedDate = NULL")
    suspend fun resetAll()

    @Query("SELECT * FROM achievements")
    suspend fun getAllRaw(): List<AchievementEntity>
}
