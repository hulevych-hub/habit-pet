package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mobile.data.local.entities.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY dayNumber")
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE dayNumber = :dayNumber")
    fun getJournalEntryByDay(dayNumber: Int): Flow<JournalEntryEntity?>

    @Insert
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    @Update
    suspend fun updateJournalEntry(entry: JournalEntryEntity): Int
}