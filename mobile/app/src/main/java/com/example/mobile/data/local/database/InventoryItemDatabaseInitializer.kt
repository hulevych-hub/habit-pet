package com.example.mobile.data.local.database

import com.example.mobile.data.local.database.AppDatabase
import com.example.mobile.domain.EquipableConfig
import com.example.mobile.domain.repository.InventoryItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Keeps configured equipables synchronized with the inventory table.
 * Existing player-owned, purchased, and equipped states are preserved while catalog metadata is refreshed.
 */
class InventoryItemDatabaseInitializer @Inject constructor(
    private val database: AppDatabase,
    private val inventoryItemRepository: InventoryItemRepository
) {

    suspend fun initializeCustomizationItems() {
        val inventoryItemDao = database.inventoryItemDao()
        EquipableConfig.inventoryEntities().forEach { configuredItem ->
            val existingItem = inventoryItemDao.getItemByItemId(configuredItem.itemId).first()
            if (existingItem == null) {
                inventoryItemDao.insertItem(configuredItem)
            } else {
                inventoryItemRepository.updateItem(
                    existingItem.copy(
                        name = configuredItem.name,
                        type = configuredItem.type,
                        imageUrl = configuredItem.imageUrl,
                        isUnlocked = existingItem.isUnlocked || existingItem.isPurchased || configuredItem.isUnlocked,
                        price = configuredItem.price,
                        rarity = configuredItem.rarity,
                        unlockSource = configuredItem.unlockSource
                    )
                )
            }
        }
    }

    fun initializeCustomizationItemsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            initializeCustomizationItems()
        }
    }
}
