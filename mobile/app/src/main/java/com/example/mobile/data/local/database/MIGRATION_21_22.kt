package com.example.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Collection set completion tracking
        db.execSQL("ALTER TABLE pet ADD COLUMN completed_sets_json TEXT NOT NULL DEFAULT '[]'")
    }
}
