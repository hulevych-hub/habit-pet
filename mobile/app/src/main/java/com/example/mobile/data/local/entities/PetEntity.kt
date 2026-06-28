package com.example.mobile.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "name") var name: String = "Luna",
    @ColumnInfo(name = "level") var level: Int = 0,
    @ColumnInfo(name = "xp") var xp: Long = 0,
    @Deprecated("Coins now live in StatisticsEntity.totalCoins.")
    @ColumnInfo(name = "coins") var coins: Int = 0,
    @ColumnInfo(name = "evolution_stage") var evolutionStage: Int = 0, // 0: Egg, 1: Hatchling, 2: Young Dragon, 3: Adult Dragon, 4: Ancient Dragon
    @ColumnInfo(name = "equipped_outfit") var equippedOutfit: String? = null,
    @ColumnInfo(name = "equipped_background") var equippedBackground: String? = null,
    @ColumnInfo(name = "equipped_aura") var equippedAura: String? = null,
    @ColumnInfo(name = "mood") var mood: String = "Calm",
    @ColumnInfo(name = "creation_date") var creationDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "active_title_id") var activeTitleId: String? = null,
    @ColumnInfo(name = "unlocked_title_ids_json") var unlockedTitleIdsJson: String = "[]",
    @ColumnInfo(name = "equipped_frame") var equippedFrame: String? = null,
    @ColumnInfo(name = "unlocked_frames_json") var unlockedFramesJson: String = "[]",
    @ColumnInfo(name = "completed_sets_json") var completedSetsJson: String = "[]"
) {
    companion object {
        fun parseUnlockedIds(value: String): Set<String> = value
            .trim()
            .trim('[', ']')
            .split(',')
            .mapNotNull { it.trim().trim('"').takeIf { it.isNotEmpty() } }
            .toSet()

        fun unlockedIdsToJson(ids: Set<String>): String = if (ids.isEmpty()) {
            "[]"
        } else {
            ids.sorted().joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        }
    }
}