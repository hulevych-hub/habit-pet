package com.example.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobile.domain.AchievementsConfig

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS achievements_new (" +
                "id TEXT NOT NULL, " +
                "progress INTEGER NOT NULL DEFAULT 0, " +
                "isUnlocked INTEGER NOT NULL DEFAULT 0, " +
                "isClaimed INTEGER NOT NULL DEFAULT 0, " +
                "unlockedDate INTEGER, " +
                "PRIMARY KEY(id))"
        )

        AchievementsConfig.achievements.forEach { definition ->
            db.execSQL(
                "INSERT OR IGNORE INTO achievements_new " +
                    "(id, progress, isUnlocked, isClaimed, unlockedDate) " +
                    "VALUES ('${definition.id}', 0, 0, 0, NULL)"
            )
        }

        preserveProgress(db, "First Habit", AchievementsConfig.FIRST_HABIT)
        preserveProgress(db, "3 Habit Builder", AchievementsConfig.THREE_HABIT_BUILDER)
        preserveProgress(db, "7 Day Streak", AchievementsConfig.SEVEN_DAY_STREAK)
        preserveProgress(db, "30 Day Streak", AchievementsConfig.THIRTY_DAY_STREAK)
        preserveProgress(db, "100 Completions", AchievementsConfig.ONE_HUNDRED_COMPLETIONS)
        preserveProgress(db, "1000 XP", AchievementsConfig.ONE_THOUSAND_XP)
        preserveProgress(db, "5000 XP", AchievementsConfig.FIVE_THOUSAND_XP)
        preserveProgress(db, "Level 10", AchievementsConfig.LEVEL_TEN)
        preserveProgress(db, "Level 25", AchievementsConfig.LEVEL_TWENTY_FIVE)
        preserveProgress(db, "First Customization", AchievementsConfig.FIRST_CUSTOMIZATION)
        preserveProgress(db, "Customization Collector", AchievementsConfig.CUSTOMIZATION_COLLECTOR)

        db.execSQL("DROP TABLE achievements")
        db.execSQL("ALTER TABLE achievements_new RENAME TO achievements")
    }

    private fun preserveProgress(
        db: SupportSQLiteDatabase,
        legacyName: String,
        configId: String
    ) {
        db.execSQL(
            "UPDATE achievements_new " +
                "SET progress = COALESCE((SELECT progress FROM achievements WHERE name = '$legacyName'), progress), " +
                "isUnlocked = COALESCE((SELECT isUnlocked FROM achievements WHERE name = '$legacyName'), isUnlocked), " +
                "isClaimed = COALESCE((SELECT isClaimed FROM achievements WHERE name = '$legacyName'), isClaimed), " +
                "unlockedDate = COALESCE((SELECT unlockedDate FROM achievements WHERE name = '$legacyName'), unlockedDate) " +
                "WHERE id = '$configId'"
        )
    }
}
