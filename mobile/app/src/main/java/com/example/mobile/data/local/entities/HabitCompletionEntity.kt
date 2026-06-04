package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_completions")
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var habitId: Long = 0,
    var date: Long = 0, // Stored as timestamp
    var xpEarned: Int = 0
)