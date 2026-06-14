package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import kotlinx.coroutines.flow.Flow

interface InventoryItemRepository {
    fun getItemsByType(type: String): Flow<List<InventoryItemEntity>>
    fun getItemById(itemId: Long): Flow<InventoryItemEntity?>
    fun getItemByItemId(itemId: String): Flow<InventoryItemEntity?>
    fun getItemsByRarity(rarity: Rarity): Flow<List<InventoryItemEntity>>
    suspend fun addItem(item: InventoryItemEntity): Long
    suspend fun updateItem(item: InventoryItemEntity): Int
    suspend fun deleteItem(item: InventoryItemEntity): Int
    suspend fun purchaseItem(itemId: Long): Int
    suspend fun grantItem(itemId: Long): Int
    suspend fun grantItemByItemId(itemId: String): Int
    fun getUnownedItemsByType(type: String): Flow<List<InventoryItemEntity>>
    fun getUnownedItemsByRarity(rarity: Rarity): Flow<List<InventoryItemEntity>>

    suspend fun deleteAll()
}