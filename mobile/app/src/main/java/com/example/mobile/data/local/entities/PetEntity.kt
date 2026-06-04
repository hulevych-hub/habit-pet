package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "Luna",
    var level: Int = 0,
    var xp: Long = 0,
    var evolutionStage: Int = 0, // 0: Egg, 1: Hatchling, 2: Young Dragon, 3: Adult Dragon, 4: Ancient Dragon
    var equippedHat: String? = null,
    var equippedGlasses: String? = null,
    var equippedScarf: String? = null,
    var equippedBackground: String? = null
)