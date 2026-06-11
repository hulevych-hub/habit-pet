package com.example.mobile.data.local.database

import android.content.Context
import com.example.mobile.data.local.database.AppDatabase
import com.example.mobile.data.local.dao.InventoryItemDao
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.EconomyConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Initializes the database with default accessories on first launch.
 * Prices are balanced according to EconomyConfig.
 */
class InventoryItemDatabaseInitializer(private val database: AppDatabase) {

    suspend fun initializeAccessories() {
        val inventoryItemDao = database.inventoryItemDao()

        // Check if accessories are already populated
        val existingCount = inventoryItemDao.getAllItems().first().size

        if (existingCount == 0) {
            val defaultAccessories = buildDefaultAccessories()
            for (accessory in defaultAccessories) {
                inventoryItemDao.insertItem(accessory)
            }
        }
    }

    fun initializeAccessoriesAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            initializeAccessories()
        }
    }

    private fun buildDefaultAccessories(): List<InventoryItemEntity> {
        return listOf(
            // =========================
            // HATS (4 items)
            // =========================

            // Normal Hat - 100 coins
            InventoryItemEntity(
                name = "Simple Cap",
                type = "HAT",
                imageUrl = "accessories/hat/simple_cap.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.NORMAL),
                rarity = Rarity.NORMAL
            ),

            // Rare Hat - 300 coins
            InventoryItemEntity(
                name = "Top Hat",
                type = "HAT",
                imageUrl = "accessories/hat/top_hat.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.RARE),
                rarity = Rarity.RARE
            ),

            // Epic Hat - 800 coins
            InventoryItemEntity(
                name = "Wizard Hat",
                type = "HAT",
                imageUrl = "accessories/hat/wizard_hat.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.EPIC),
                rarity = Rarity.EPIC
            ),

            // Legendary Hat - 2000 coins
            InventoryItemEntity(
                name = "Crown of Stars",
                type = "HAT",
                imageUrl = "accessories/hat/crown_of_stars.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.LEGENDARY),
                rarity = Rarity.LEGENDARY
            ),

            // =========================
            // GLASSES (4 items)
            // =========================

            // Normal Glasses - 100 coins
            InventoryItemEntity(
                name = "Round Glasses",
                type = "GLASSES",
                imageUrl = "accessories/glasses/round_glasses.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.NORMAL),
                rarity = Rarity.NORMAL
            ),

            // Rare Glasses - 300 coins
            InventoryItemEntity(
                name = "Aviator Shades",
                type = "GLASSES",
                imageUrl = "accessories/glasses/aviator_shades.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.RARE),
                rarity = Rarity.RARE
            ),

            // Epic Glasses - 800 coins
            InventoryItemEntity(
                name = "Neon Visor",
                type = "GLASSES",
                imageUrl = "accessories/glasses/neon_visor.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.EPIC),
                rarity = Rarity.EPIC
            ),

            // Legendary Glasses - 2000 coins
            InventoryItemEntity(
                name = "Ethereal Lens",
                type = "GLASSES",
                imageUrl = "accessories/glasses/ethereal_lens.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.LEGENDARY),
                rarity = Rarity.LEGENDARY
            ),

            // =========================
            // SCARVES (4 items)
            // =========================

            // Normal Scarf - 100 coins
            InventoryItemEntity(
                name = "Red Scarf",
                type = "SCARF",
                imageUrl = "accessories/scarf/red_scarf.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.NORMAL),
                rarity = Rarity.NORMAL
            ),

            // Rare Scarf - 300 coins
            InventoryItemEntity(
                name = "Silk Scarf",
                type = "SCARF",
                imageUrl = "accessories/scarf/silk_scarf.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.RARE),
                rarity = Rarity.RARE
            ),

            // Epic Scarf - 800 coins
            InventoryItemEntity(
                name = "Mystic Shawl",
                type = "SCARF",
                imageUrl = "accessories/scarf/mystic_shawl.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.EPIC),
                rarity = Rarity.EPIC
            ),

            // Legendary Scarf - 2000 coins
            InventoryItemEntity(
                name = "Dragon's Breath",
                type = "SCARF",
                imageUrl = "accessories/scarf/dragons_breath.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.LEGENDARY),
                rarity = Rarity.LEGENDARY
            ),

            // =========================
            // BACKGROUNDS (4 items)
            // =========================

            // Normal Background - 100 coins
            InventoryItemEntity(
                name = "Sunny Meadow",
                type = "BACKGROUND",
                imageUrl = "accessories/background/sunny_meadow.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.NORMAL),
                rarity = Rarity.NORMAL
            ),

            // Rare Background - 300 coins
            InventoryItemEntity(
                name = "Crystal Cave",
                type = "BACKGROUND",
                imageUrl = "accessories/background/crystal_cave.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.RARE),
                rarity = Rarity.RARE
            ),

            // Epic Background - 800 coins
            InventoryItemEntity(
                name = "Floating Islands",
                type = "BACKGROUND",
                imageUrl = "accessories/background/floating_islands.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.EPIC),
                rarity = Rarity.EPIC
            ),

            // Legendary Background - 2000 coins
            InventoryItemEntity(
                name = "Celestial Realm",
                type = "BACKGROUND",
                imageUrl = "accessories/background/celestial_realm.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.accessoryPrice(Rarity.LEGENDARY),
                rarity = Rarity.LEGENDARY
            )
        )
    }
}
