package com.example.mobile.domain

import com.example.mobile.data.local.entities.Rarity

/**
 * Provider for chest reward configurations.
 * Contains default configurations for each chest type.
 * Values are sourced from EconomyConfig for centralized balancing.
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
                coinRange = EconomyConfig.NORMAL_CHEST_COIN_RANGE,
                expRange = EconomyConfig.NORMAL_CHEST_EXP_RANGE,
                accessoryRarity = null,
                accessoryDropChance = 0.0,
                guaranteedReward = true
            )
            ChestType.RARE -> ChestRewardConfig(
                chestType = ChestType.RARE,
                coinRange = EconomyConfig.RARE_CHEST_COIN_RANGE,
                expRange = EconomyConfig.RARE_CHEST_EXP_RANGE,
                accessoryRarity = Rarity.RARE,
                accessoryDropChance = EconomyConfig.RARE_CHEST_ACCESSORY_DROP_CHANCE,
                guaranteedReward = true
            )
            ChestType.EPIC -> ChestRewardConfig(
                chestType = ChestType.EPIC,
                coinRange = EconomyConfig.EPIC_CHEST_COIN_RANGE,
                expRange = EconomyConfig.EPIC_CHEST_EXP_RANGE,
                accessoryRarity = Rarity.EPIC,
                accessoryDropChance = EconomyConfig.EPIC_CHEST_ACCESSORY_DROP_CHANCE,
                guaranteedReward = true
            )
            ChestType.LEGENDARY -> ChestRewardConfig(
                chestType = ChestType.LEGENDARY,
                coinRange = EconomyConfig.LEGENDARY_CHEST_COIN_RANGE,
                expRange = EconomyConfig.LEGENDARY_CHEST_EXP_RANGE,
                accessoryRarity = Rarity.LEGENDARY,
                accessoryDropChance = EconomyConfig.LEGENDARY_CHEST_ACCESSORY_DROP_CHANCE,
                guaranteedReward = true
            )
        }
    }

    /**
     * Get a random chest type based on rarity probabilities.
     * In the future, this could be influenced by player level, game progression, etc.
     * Probabilities are sourced from EconomyConfig for centralized balancing.
     */
    fun getRandomChestType(): ChestType {
        val roll = Math.random() // 0.0 to 1.0
        return when {
            roll < EconomyConfig.CHEST_NORMAL_PROBABILITY -> ChestType.NORMAL
            roll < EconomyConfig.CHEST_NORMAL_PROBABILITY + EconomyConfig.CHEST_RARE_PROBABILITY -> ChestType.RARE
            roll < EconomyConfig.CHEST_NORMAL_PROBABILITY + EconomyConfig.CHEST_RARE_PROBABILITY + EconomyConfig.CHEST_EPIC_PROBABILITY -> ChestType.EPIC
            else -> ChestType.LEGENDARY
        }
    }
}