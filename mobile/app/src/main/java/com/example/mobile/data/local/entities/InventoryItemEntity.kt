package com.example.mobile.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Rarity shared by all customization items. */
enum class Rarity {
    NORMAL, RARE, EPIC, LEGENDARY
}

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "item_id") var itemId: String = "",
    var name: String = "",
    var type: String = "",
    var imageUrl: String = "",
    var isUnlocked: Boolean = false,
    var isPurchased: Boolean = false,
    var isEquipped: Boolean = false,
    var price: Int = 0, // in coins
    var rarity: Rarity = Rarity.NORMAL,
    @ColumnInfo(name = "unlock_source") var unlockSource: String = "SHOP"
)