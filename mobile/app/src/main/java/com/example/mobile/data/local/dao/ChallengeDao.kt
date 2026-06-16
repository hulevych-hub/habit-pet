package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobile.data.local.entities.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE id = 1 LIMIT 1")
    fun getActiveChallenge(): Flow<ChallengeEntity?>

    @Query("SELECT * FROM challenges WHERE id = 1 LIMIT 1")
    suspend fun getActiveChallengeOnce(): ChallengeEntity?

    @Query("SELECT COUNT(*) FROM challenges")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(challenge: ChallengeEntity): Long

    @Query("DELETE FROM challenges")
    suspend fun deleteAll()
}
