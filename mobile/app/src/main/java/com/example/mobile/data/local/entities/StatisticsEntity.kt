package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey
    var id: Long = 1,
    var currentStreak: Int = 0,
    var bestStreak: Int = 0,
    var globalStreak: Int = 0,
    var totalCompletions: Int = 0,
    var totalXp: Long = 0,
    var daysActive: Int = 0,
    var totalHabitsCompleted: Int = 0,
    var petAgeDays: Int = 0,
    var totalCoins: Int = 0,
    var lastStreakAwardedAt: Int = 0,
    var lastUpdated: Long = 0, // timestamp
    var rewardChestsAvailable: Int = 0,
    val lastStreakDate: Long = 0L,
    var currentCombo: Int = 0,
    var bestCombo: Int = 0,
    var lastHabitCompletionTimestamp: Long = 0L,
    var dailyGoalXp: Int = 300,
    var dailyGoalProgressXp: Int = 0,
    var dailyGoalDate: Long = 0L,
    var dailyGoalCompletedDate: Long = 0L
)