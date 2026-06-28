package com.example.mobile.data.local.entities

import androidx.room.ColumnInfo
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
    var lastStreakFreezeDate: Long = 0L,
    var lastFrozenStreakDate: Long = 0L,
    var streakFreezeDatesJson: String = "[]",
    @ColumnInfo(name = "daily_login_streak") var dailyLoginStreak: Int = 0,
    @ColumnInfo(name = "last_daily_login_date") var lastDailyLoginDate: Long = 0L,
    @ColumnInfo(name = "last_daily_login_reward_day") var lastDailyLoginRewardDay: Int = 0
) {
    companion object {
        fun parseFreezeDates(value: String): Set<Long> = value
            .trim()
            .trim('[', ']')
            .split(',')
            .mapNotNull { it.trim().trim('"').toLongOrNull() }
            .toSet()

        fun freezeDatesToJson(dates: Set<Long>): String = if (dates.isEmpty()) {
            "[]"
        } else {
            dates.sorted().joinToString(prefix = "[", postfix = "]") { it.toString() }
        }
    }
}