package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey var id: Long = 0,
    var dayNumber: Int = 0,
    var entryText: String = "",
    var timestamp: Long = 0 // when the entry was created
)