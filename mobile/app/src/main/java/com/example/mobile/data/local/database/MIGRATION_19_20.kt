package com.example.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habit_progress ADD COLUMN startedAt INTEGER")
        db.execSQL("ALTER TABLE habit_progress ADD COLUMN lastSessionSeconds INTEGER NOT NULL DEFAULT 0")
    }
}
