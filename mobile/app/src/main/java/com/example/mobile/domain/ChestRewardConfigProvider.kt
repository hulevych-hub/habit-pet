package com.example.mobile.domain

import com.example.mobile.data.local.entities.Rarity

/**
 * Provider for chest reward configurations.
 * Contains default configurations for each chest type.
 */
object ChestRewardConfigProvider {

    /**
     * Get the default configuration for a chest type.
     * These values can be overridden or loaded from configuration files in the future.
     */
    fun getConfig(chestType: ChestType): ChestRewardConfig {
        return when (chestType) {
            ChestType.NORMAL -> ChestRewardConfig(
                chestType = ChestType.NORMAL,
                coinRange = 10..50, // Normal chests give 10-50 coins
                expRange = 0..0,    // No EXP by default
                accessoryRarity = null,
                accessoryDropChance = 0.0, // No accessory drop by default
                guaranteedReward = true
            )
            ChestType.RARE -> ChestRewardConfig(
                chestType = ChestType.RARE,
                coinRange = 50..150, // Rare chests give 50-150 coins
                expRange = 100..300, // Rare chests give 100-300 EXP
                accessoryRarity = Rarity.RARE,
                accessoryDropChance = 0.2, // 20% chance for rare accessory
                guaranteedReward = true
            )
            ChestType.EPIC -> ChestRewardConfig(
                chestType = ChestType.EPIC,
                coinRange = 150..300, // Epic chests give 150-300 coins
                expRange = 300..600,  // Epic chests give 300-600 EXP
                accessoryRarity = Rarity.EPIC,
                accessoryDropChance = 0.35, // 35% chance for epic accessory
                guaranteedReward = true
            )
            ChestType.LEGENDARY -> ChestRewardConfig(
                chestType = ChestType.LEGENDARY,
                coinRange = 300..600, // Legendary chests give 300-600 coins
                expRange = 600..1200, // Legendary chests give 600-1200 EXP
                accessoryRarity = Rarity.LEGENDARY,
                accessoryDropChance = 0.5, // 50% chance for legendary accessory
                guaranteedReward = true
            )
        }
    }

    /**
     * Get a random chest type based on rarity probabilities.
     * In the future, this could be influenced by player level, game progression, etc.
     */
    fun getRandomChestType(): ChestType {
        val roll = Math.random() // 0.0 to 1.0
        return when {
            roll < 0.5 -> ChestType.NORMAL        // 50% chance
            roll < 0.8 -> ChestType.RARE          // 30% chance
            roll < 0.95 -> ChestType.EPIC         // 15% chance
            else -> ChestType.LEGENDARY           // 5% chance
        }
    }
}