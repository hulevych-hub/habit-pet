package com.example.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pet ADD COLUMN equipped_outfit TEXT")
        db.execSQL("ALTER TABLE pet ADD COLUMN equipped_aura TEXT")

        db.execSQL("ALTER TABLE inventory_items ADD COLUMN item_id TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE inventory_items ADD COLUMN unlock_source TEXT NOT NULL DEFAULT 'SHOP'")
        db.execSQL(
            "UPDATE inventory_items " +
                "SET item_id = lower(replace(name, ' ', '_')) " +
                "WHERE item_id = ''"
        )

        db.execSQL("UPDATE pet SET equipped_outfit = equipped_hat WHERE equipped_hat IS NOT NULL")
        db.execSQL("UPDATE pet SET equipped_aura = NULL")

        db.execSQL("ALTER TABLE pet DROP COLUMN equipped_hat")
        db.execSQL("ALTER TABLE pet DROP COLUMN equipped_glasses")
        db.execSQL("ALTER TABLE pet DROP COLUMN equipped_scarf")
    }
}
