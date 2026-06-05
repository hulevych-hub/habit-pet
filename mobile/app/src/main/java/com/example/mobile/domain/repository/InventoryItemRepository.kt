package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

interface InventoryItemRepository {
    fun getItemsByType(type: String): Flow<List<InventoryItemEntity>>
    fun getItemById(itemId: Long): Flow<InventoryItemEntity?>
    suspend fun addItem(item: InventoryItemEntity): Long
    suspend fun updateItem(item: InventoryItemEntity): Int
    suspend fun deleteItem(item: InventoryItemEntity): Int
    suspend fun purchaseItem(itemId: Long): Int
}