package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.InventoryItemDao
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.UnlockSources
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventoryItemRepositoryImpl @Inject constructor(
    private val inventoryItemDao: InventoryItemDao,
    private val statisticsRepository: com.example.mobile.domain.repository.StatisticsRepository,
    private val challengeRepository: ChallengeRepository
) : InventoryItemRepository {
    override fun getItemsByType(type: String): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getItemsByType(type)

    override fun getItemById(itemId: Long): Flow<InventoryItemEntity?> =
        inventoryItemDao.getItemById(itemId)

    override fun getItemByItemId(itemId: String): Flow<InventoryItemEntity?> =
        inventoryItemDao.getItemByItemId(itemId)

    override fun getItemsByRarity(rarity: Rarity): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getItemsByRarity(rarity)

    override suspend fun addItem(item: InventoryItemEntity): Long =
        inventoryItemDao.insertItem(item)

    override suspend fun updateItem(item: InventoryItemEntity): Int =
        inventoryItemDao.updateItem(item)

    override suspend fun deleteItem(item: InventoryItemEntity): Int =
        inventoryItemDao.deleteItem(item)

    override suspend fun deleteAll() {
        inventoryItemDao.deleteAll()
    }

    override suspend fun purchaseItem(itemId: Long): Int {
        // Get the item details
        val item = inventoryItemDao.getItemById(itemId).firstOrNull()
            ?: return -1 // Item not found

        // Check if already purchased
        if (item.isPurchased) {
            return -2 // Already purchased
        }

        // Non-shop items cannot be bought with coins
        if (item.unlockSource != UnlockSources.SHOP || item.price <= 0) {
            return -3 // Not purchasable
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

        // Mark item as purchased (not equipped by default)
        val updatedItem = item.copy(isPurchased = true, isEquipped = false)
        inventoryItemDao.updateItem(updatedItem)
        challengeRepository.recordCustomizationUnlocked(item.itemId)

        return 1 // Success
    }

    override suspend fun grantItem(itemId: Long): Int {
        // Get the item details
        val item = inventoryItemDao.getItemById(itemId).firstOrNull()
            ?: return -1 // Item not found

        // Check if already granted/purchased
        if (item.isPurchased) {
            return -2 // Already purchased
        }

        // Mark item as granted/purchased (not equipped by default)
        val updatedItem = item.copy(isPurchased = true, isEquipped = false)
        inventoryItemDao.updateItem(updatedItem)
        challengeRepository.recordCustomizationUnlocked(item.itemId)

        return 1 // Success
    }

    override suspend fun grantItemByItemId(itemId: String): Int {
        val item = inventoryItemDao.getItemByItemId(itemId).firstOrNull()
            ?: return -1

        return grantItem(item.id)
    }

    override fun getUnownedItemsByType(type: String): Flow<List<InventoryItemEntity>> {
        return inventoryItemDao.getItemsByType(type)
            .map { items ->
                items.filter { !it.isPurchased }
            }
    }

    override fun getUnownedItemsByRarity(rarity: Rarity): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getUnownedItemsByRarity(rarity)
}