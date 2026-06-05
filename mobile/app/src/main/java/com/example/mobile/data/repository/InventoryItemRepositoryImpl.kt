package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.InventoryItemDao
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.domain.repository.InventoryItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class InventoryItemRepositoryImpl @Inject constructor(
    private val inventoryItemDao: InventoryItemDao,
    private val statisticsRepository: com.example.mobile.domain.repository.StatisticsRepository
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

    override suspend fun purchaseItem(itemId: Long): Int {
        // Get the item details
        val item = inventoryItemDao.getItemById(itemId).firstOrNull()
            ?: return -1 // Item not found

        // Check if already purchased
        if (item.isPurchased) {
            return -2 // Already purchased
        }

        // Get current statistics
        val currentStats = statisticsRepository.getStatistics().firstOrNull()
            ?: return -3 // Unable to get statistics

        // Check if player has enough coins
        if (currentStats.totalCoins < item.price) {
            return -4 // Not enough coins
        }

        // Deduct coins and update statistics
        val updatedStats = currentStats.copy(
            totalCoins = currentStats.totalCoins - item.price,
            lastUpdated = System.currentTimeMillis()
        )
        statisticsRepository.updateStatistics(updatedStats)

        // Mark item as purchased
        val updatedItem = item.copy(isPurchased = true)
        inventoryItemDao.updateItem(updatedItem)

        return 1 // Success
    }
}