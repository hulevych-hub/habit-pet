package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_progress")
data class HabitProgressEntity(
    @PrimaryKey val habitId: Long,
    val date: Long,
    val accumulatedMinutes: Int,
    val lastUpdated: Long,
    val startedAt: Long? = null,
    val lastSessionSeconds: Int = 0,
)
