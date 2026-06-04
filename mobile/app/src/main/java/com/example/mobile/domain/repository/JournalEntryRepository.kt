package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

interface JournalEntryRepository {
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>
    fun getJournalEntryByDay(dayNumber: Int): Flow<JournalEntryEntity?>
    suspend fun addJournalEntry(entry: JournalEntryEntity): Long
    suspend fun updateJournalEntry(entry: JournalEntryEntity): Int
}