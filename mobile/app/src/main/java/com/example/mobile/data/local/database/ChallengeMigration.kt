package com.example.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS challenges (" +
                "id INTEGER NOT NULL, " +
                "challenge_id TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "icon TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "target_value INTEGER NOT NULL, " +
                "progress_value INTEGER NOT NULL DEFAULT 0, " +
                "reward_ids_json TEXT NOT NULL DEFAULT '[]', " +
                "is_completed INTEGER NOT NULL DEFAULT 0, " +
                "is_claimed INTEGER NOT NULL DEFAULT 0, " +
                "previous_challenge_id TEXT, " +
                "created_at INTEGER NOT NULL, " +
                "completed_at INTEGER, " +
                "claimed_at INTEGER, " +
                "PRIMARY KEY(id))"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS statistics_new (" +
                "id INTEGER NOT NULL, " +
                "currentStreak INTEGER NOT NULL, " +
                "bestStreak INTEGER NOT NULL, " +
                "globalStreak INTEGER NOT NULL, " +
                "totalCompletions INTEGER NOT NULL, " +
                "totalXp INTEGER NOT NULL, " +
                "daysActive INTEGER NOT NULL, " +
                "totalHabitsCompleted INTEGER NOT NULL, " +
                "petAgeDays INTEGER NOT NULL, " +
                "totalCoins INTEGER NOT NULL, " +
                "lastStreakAwardedAt INTEGER NOT NULL, " +
                "lastUpdated INTEGER NOT NULL, " +
                "rewardChestsAvailable INTEGER NOT NULL, " +
                "lastStreakDate INTEGER NOT NULL, " +
                "currentCombo INTEGER NOT NULL, " +
                "bestCombo INTEGER NOT NULL, " +
                "lastHabitCompletionTimestamp INTEGER NOT NULL, " +
                "PRIMARY KEY(id))"
        )

        db.execSQL(
            "INSERT INTO statistics_new (" +
                "id, currentStreak, bestStreak, globalStreak, totalCompletions, totalXp, " +
                "daysActive, totalHabitsCompleted, petAgeDays, totalCoins, " +
                "lastStreakAwardedAt, lastUpdated, rewardChestsAvailable, lastStreakDate, " +
                "currentCombo, bestCombo, lastHabitCompletionTimestamp) " +
                "SELECT id, currentStreak, bestStreak, globalStreak, totalCompletions, totalXp, " +
                "daysActive, totalHabitsCompleted, petAgeDays, totalCoins, " +
                "lastStreakAwardedAt, lastUpdated, rewardChestsAvailable, lastStreakDate, " +
                "currentCombo, bestCombo, lastHabitCompletionTimestamp " +
                "FROM statistics"
        )

        val now = System.currentTimeMillis()
        db.execSQL(
            "INSERT OR IGNORE INTO challenges (" +
                "id, challenge_id, title, description, icon, type, target_value, " +
                "progress_value, reward_ids_json, is_completed, is_claimed, " +
                "previous_challenge_id, created_at, completed_at, claimed_at) " +
                "SELECT 1, 'xp_30', 'XP warmup', " +
                "'Earn 30 XP to keep the rhythm going.', 'xp', 'XP_EARNED', 30, " +
                "MIN(COALESCE(dailyGoalProgressXp, 0), 30), " +
                "'[{\"type\":\"COIN\",\"amount\":20},{\"type\":\"EXP\",\"amount\":20}]', " +
                "CASE WHEN COALESCE(dailyGoalProgressXp, 0) >= COALESCE(dailyGoalXp, 30) THEN 1 ELSE 0 END, " +
                "CASE WHEN COALESCE(dailyGoalCompletedDate, 0) = COALESCE(dailyGoalDate, 0) " +
                "AND COALESCE(dailyGoalDate, 0) > 0 THEN 1 ELSE 0 END, " +
                "'daily_goal', $now, " +
                "CASE WHEN COALESCE(dailyGoalProgressXp, 0) >= COALESCE(dailyGoalXp, 30) THEN $now ELSE NULL END, " +
                "CASE WHEN COALESCE(dailyGoalCompletedDate, 0) = COALESCE(dailyGoalDate, 0) " +
                "AND COALESCE(dailyGoalDate, 0) > 0 THEN $now ELSE NULL END " +
                "FROM statistics WHERE id = 1"
        )

        db.execSQL("DROP TABLE statistics")
        db.execSQL("ALTER TABLE statistics_new RENAME TO statistics")
    }
}
