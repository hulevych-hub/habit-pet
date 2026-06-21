package com.example.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE statistics ADD COLUMN lastStreakFreezeDate INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE statistics ADD COLUMN lastFrozenStreakDate INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE statistics ADD COLUMN streakFreezeDatesJson TEXT NOT NULL DEFAULT '[]'")
    }
}
