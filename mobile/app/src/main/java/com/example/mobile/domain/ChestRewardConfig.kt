package com.example.mobile.domain

import com.example.mobile.data.local.entities.Rarity

/**
 * Configuration for chest rewards defining what items can be obtained from each chest type.
 */
data class ChestRewardConfig(
    val chestType: ChestType,
    val coinRange: IntRange = 0..0,
    val expRange: IntRange = 0..0,
    val accessoryRarity: Rarity? = null,
    val accessoryDropChance: Double = 0.0, // 0.0 to 1.0
    val guaranteedReward: Boolean = false // if true, guarantees a reward of the specified type
) {
    /**
     * Gets a random coin amount within the configured range
     */
    fun getRandomCoins(): Int {
        return if (coinRange.isEmpty()) 0 else coinRange.random()
    }

    /**
     * Gets a random EXP amount within the configured range
     */
    fun getRandomExp(): Int {
        return if (expRange.isEmpty()) 0 else expRange.random()
    }
}