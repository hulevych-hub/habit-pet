package com.example.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Pet titles and avatar frames
        db.execSQL("ALTER TABLE pet ADD COLUMN active_title_id TEXT")
        db.execSQL("ALTER TABLE pet ADD COLUMN unlocked_title_ids_json TEXT NOT NULL DEFAULT '[]'")
        db.execSQL("ALTER TABLE pet ADD COLUMN equipped_frame TEXT")
        db.execSQL("ALTER TABLE pet ADD COLUMN unlocked_frames_json TEXT NOT NULL DEFAULT '[]'")

        // Daily login streak
        db.execSQL("ALTER TABLE statistics ADD COLUMN daily_login_streak INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE statistics ADD COLUMN last_daily_login_date INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE statistics ADD COLUMN last_daily_login_reward_day INTEGER NOT NULL DEFAULT 0")
    }
}
