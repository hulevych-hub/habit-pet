package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.InventoryItemDao
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.domain.repository.InventoryItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryItemRepositoryImpl @Inject constructor(
    private val inventoryItemDao: InventoryItemDao
) : InventoryItemRepository {
    override fun getItemsByType(type: String): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getItemsByType(type)

    override fun getItemById(itemId: Long): Flow<InventoryItemEntity?> =
        inventoryItemDao.getItemById(itemId)

    override suspend fun addItem(item: InventoryItemEntity): Long =
        inventoryItemDao.insertItem(item)

    override suspend fun updateItem(item: InventoryItemEntity): Int =
        inventoryItemDao.updateItem(item)

    override suspend fun deleteItem(item: InventoryItemEntity): Int =
        inventoryItemDao.deleteItem(item)
}