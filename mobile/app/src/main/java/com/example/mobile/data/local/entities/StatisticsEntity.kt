package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var currentStreak: Int = 0,
    var bestStreak: Int = 0,
    var globalStreak: Int = 0,
    var totalCompletions: Int = 0,
    var totalXp: Long = 0,
    var daysActive: Int = 0,
    var totalHabitsCompleted: Int = 0,
    var petAgeDays: Int = 0,
    var lastUpdated: Long = 0 // timestamp
)