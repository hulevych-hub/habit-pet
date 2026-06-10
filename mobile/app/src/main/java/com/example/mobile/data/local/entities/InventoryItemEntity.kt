package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Rarity of an accessory */
enum class Rarity {
    NORMAL, RARE, EPIC, LEGENDARY
}

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var type: String = "",
    var imageUrl: String = "",
    var isUnlocked: Boolean = false,
    var isPurchased: Boolean = false,
    var isEquipped: Boolean = false,
    var price: Int = 0, // in coins
    var rarity: Rarity = Rarity.NORMAL
)