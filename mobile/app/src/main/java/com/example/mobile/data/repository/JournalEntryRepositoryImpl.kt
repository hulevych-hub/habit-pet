package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.JournalEntryDao
import com.example.mobile.data.local.entities.JournalEntryEntity
import com.example.mobile.domain.repository.JournalEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JournalEntryRepositoryImpl @Inject constructor(
    private val journalEntryDao: JournalEntryDao
) : JournalEntryRepository {
    override fun getAllJournalEntries(): Flow<List<JournalEntryEntity>> =
        journalEntryDao.getAllJournalEntries()

    override fun getJournalEntryByDay(dayNumber: Int): Flow<JournalEntryEntity?> =
        journalEntryDao.getJournalEntryByDay(dayNumber)

    override suspend fun addJournalEntry(entry: JournalEntryEntity): Long =
        journalEntryDao.insertJournalEntry(entry)

    override suspend fun updateJournalEntry(entry: JournalEntryEntity): Int =
        journalEntryDao.updateJournalEntry(entry)
}