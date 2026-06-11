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
    @ColumnInfo(name = "coins") var coins: Int = 0,
    @ColumnInfo(name = "evolution_stage") var evolutionStage: Int = 0, // 0: Egg, 1: Hatchling, 2: Young Dragon, 3: Adult Dragon, 4: Ancient Dragon
    @ColumnInfo(name = "equipped_outfit") var equippedOutfit: String? = null,
    @ColumnInfo(name = "equipped_background") var equippedBackground: String? = null,
    @ColumnInfo(name = "equipped_aura") var equippedAura: String? = null,
    @ColumnInfo(name = "mood") var mood: String = "Calm",
    @ColumnInfo(name = "creation_date") var creationDate: Long = System.currentTimeMillis()
)