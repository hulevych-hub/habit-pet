package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var type: String = "",
    var imageUrl: String = "",
    var isUnlocked: Boolean = false,
    var isPurchased: Boolean = false,
    var price: Int = 0 // in coins
)