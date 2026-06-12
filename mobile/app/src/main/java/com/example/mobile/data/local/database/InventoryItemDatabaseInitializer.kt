package com.example.mobile.data.local.database

import com.example.mobile.data.local.database.AppDatabase
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.EconomyConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Initializes the database with default customization items on first launch.
 * Prices are balanced according to EconomyConfig.
 */
class InventoryItemDatabaseInitializer(private val database: AppDatabase) {

    suspend fun initializeCustomizationItems() {
        val inventoryItemDao = database.inventoryItemDao()

        val existingCount = inventoryItemDao.getAllItems().first().size

        if (existingCount == 0) {
            val defaultItems = buildDefaultCustomizationItems()
            for (item in defaultItems) {
                inventoryItemDao.insertItem(item)
            }
        }
    }

    fun initializeCustomizationItemsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            initializeCustomizationItems()
        }
    }

    private fun buildDefaultCustomizationItems(): List<InventoryItemEntity> {
        return listOf(
            InventoryItemEntity(
                itemId = "royal_scarf",
                name = "Royal Scarf",
                type = CustomizationTypes.OUTFIT,
                imageUrl = "outfits/royal_scarf.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.NORMAL),
                rarity = Rarity.NORMAL,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "crystal_crown",
                name = "Crystal Crown",
                type = CustomizationTypes.OUTFIT,
                imageUrl = "outfits/crystal_crown.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.RARE),
                rarity = Rarity.RARE,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "mystic_cloak",
                name = "Mystic Cloak",
                type = CustomizationTypes.OUTFIT,
                imageUrl = "outfits/mystic_cloak.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.EPIC),
                rarity = Rarity.EPIC,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "starlight_armor",
                name = "Starlight Armor",
                type = CustomizationTypes.OUTFIT,
                imageUrl = "outfits/starlight_armor.png",
                isUnlocked = false,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.LEGENDARY),
                rarity = Rarity.LEGENDARY,
                unlockSource = "CHEST"
            ),

            InventoryItemEntity(
                itemId = "background_forest",
                name = "Sunny Meadow",
                type = CustomizationTypes.BACKGROUND,
                imageUrl = "background_forest.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.NORMAL),
                rarity = Rarity.NORMAL,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "background_beach",
                name = "Crystal Cave",
                type = CustomizationTypes.BACKGROUND,
                imageUrl = "background_beach.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.RARE),
                rarity = Rarity.RARE,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "background_mountains",
                name = "Floating Islands",
                type = CustomizationTypes.BACKGROUND,
                imageUrl = "background_mountains.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.EPIC),
                rarity = Rarity.EPIC,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "background_night_sky",
                name = "Celestial Realm",
                type = CustomizationTypes.BACKGROUND,
                imageUrl = "background_night_sky.png",
                isUnlocked = false,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.LEGENDARY),
                rarity = Rarity.LEGENDARY,
                unlockSource = "CHEST"
            ),

            InventoryItemEntity(
                itemId = "soft_glow",
                name = "Soft Glow",
                type = CustomizationTypes.AURA,
                imageUrl = "auras/soft_glow.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.NORMAL),
                rarity = Rarity.NORMAL,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "crystal_aura",
                name = "Crystal Aura",
                type = CustomizationTypes.AURA,
                imageUrl = "auras/crystal_aura.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.RARE),
                rarity = Rarity.RARE,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "dragonfire_aura",
                name = "Dragonfire Aura",
                type = CustomizationTypes.AURA,
                imageUrl = "auras/dragonfire_aura.png",
                isUnlocked = true,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.EPIC),
                rarity = Rarity.EPIC,
                unlockSource = "SHOP"
            ),
            InventoryItemEntity(
                itemId = "celestial_aura",
                name = "Celestial Aura",
                type = CustomizationTypes.AURA,
                imageUrl = "auras/celestial_aura.png",
                isUnlocked = false,
                isPurchased = false,
                isEquipped = false,
                price = EconomyConfig.customizationPrice(Rarity.LEGENDARY),
                rarity = Rarity.LEGENDARY,
                unlockSource = "CHEST"
            )
        )
    }
}
