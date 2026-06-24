package com.example.mobile.domain

import com.example.mobile.data.local.entities.Rarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChestRewardConfigProviderTest {

    // ==================== getConfig ====================

    @Test
    fun `getConfig — NORMAL returns correct config`() {
        val config = ChestRewardConfigProvider.getConfig(ChestType.NORMAL)

        assertEquals(ChestType.NORMAL, config.chestType)
        assertEquals(EconomyConfig.NORMAL_CHEST_COIN_RANGE, config.coinRange)
        assertEquals(EconomyConfig.NORMAL_CHEST_EXP_RANGE, config.expRange)
        assertNull("NORMAL has no customization", config.customizationRarity)
        assertEquals(0.0, config.customizationDropChance, 0.001)
        assertTrue("NORMAL is guaranteed", config.guaranteedReward)
    }

    @Test
    fun `getConfig — RARE returns correct config`() {
        val config = ChestRewardConfigProvider.getConfig(ChestType.RARE)

        assertEquals(ChestType.RARE, config.chestType)
        assertEquals(EconomyConfig.RARE_CHEST_COIN_RANGE, config.coinRange)
        assertEquals(EconomyConfig.RARE_CHEST_EXP_RANGE, config.expRange)
        assertEquals(Rarity.RARE, config.customizationRarity)
        assertEquals(EconomyConfig.RARE_CHEST_CUSTOMIZATION_DROP_CHANCE, config.customizationDropChance, 0.001)
        assertTrue("RARE is guaranteed", config.guaranteedReward)
    }

    @Test
    fun `getConfig — EPIC returns correct config`() {
        val config = ChestRewardConfigProvider.getConfig(ChestType.EPIC)

        assertEquals(ChestType.EPIC, config.chestType)
        assertEquals(EconomyConfig.EPIC_CHEST_COIN_RANGE, config.coinRange)
        assertEquals(EconomyConfig.EPIC_CHEST_EXP_RANGE, config.expRange)
        assertEquals(Rarity.EPIC, config.customizationRarity)
        assertEquals(EconomyConfig.EPIC_CHEST_CUSTOMIZATION_DROP_CHANCE, config.customizationDropChance, 0.001)
        assertTrue("EPIC is guaranteed", config.guaranteedReward)
    }

    @Test
    fun `getConfig — LEGENDARY returns correct config`() {
        val config = ChestRewardConfigProvider.getConfig(ChestType.LEGENDARY)

        assertEquals(ChestType.LEGENDARY, config.chestType)
        assertEquals(EconomyConfig.LEGENDARY_CHEST_COIN_RANGE, config.coinRange)
        assertEquals(EconomyConfig.LEGENDARY_CHEST_EXP_RANGE, config.expRange)
        assertEquals(Rarity.LEGENDARY, config.customizationRarity)
        assertEquals(EconomyConfig.LEGENDARY_CHEST_CUSTOMIZATION_DROP_CHANCE, config.customizationDropChance, 0.001)
        assertTrue("LEGENDARY is guaranteed", config.guaranteedReward)
    }

    @Test
    fun `getConfig — coin ranges are ordered by rarity`() {
        val normal = ChestRewardConfigProvider.getConfig(ChestType.NORMAL)
        val rare = ChestRewardConfigProvider.getConfig(ChestType.RARE)
        val epic = ChestRewardConfigProvider.getConfig(ChestType.EPIC)
        val legendary = ChestRewardConfigProvider.getConfig(ChestType.LEGENDARY)

        assertTrue("Normal max <= Rare min", normal.coinRange.last <= rare.coinRange.first)
        assertTrue("Rare max <= Epic min", rare.coinRange.last <= epic.coinRange.first)
        assertTrue("Epic max <= Legendary min", epic.coinRange.last <= legendary.coinRange.first)
    }

    @Test
    fun `getConfig — exp ranges are ordered by rarity`() {
        val normal = ChestRewardConfigProvider.getConfig(ChestType.NORMAL)
        val rare = ChestRewardConfigProvider.getConfig(ChestType.RARE)
        val epic = ChestRewardConfigProvider.getConfig(ChestType.EPIC)
        val legendary = ChestRewardConfigProvider.getConfig(ChestType.LEGENDARY)

        assertEquals("NORMAL has 0 exp", 0, normal.expRange.first)
        assertTrue("RARE has positive exp", rare.expRange.first > 0)
        assertTrue("EPIC exp > RARE exp", epic.expRange.first >= rare.expRange.first)
        assertTrue("LEGENDARY exp > EPIC exp", legendary.expRange.first >= epic.expRange.first)
    }

    @Test
    fun `getConfig — drop chances increase with rarity`() {
        val rare = ChestRewardConfigProvider.getConfig(ChestType.RARE)
        val epic = ChestRewardConfigProvider.getConfig(ChestType.EPIC)
        val legendary = ChestRewardConfigProvider.getConfig(ChestType.LEGENDARY)

        assertTrue("EPIC drop > RARE drop", epic.customizationDropChance > rare.customizationDropChance)
        assertTrue("LEGENDARY drop > EPIC drop", legendary.customizationDropChance > epic.customizationDropChance)
    }

    // ==================== getRandomCoins ====================

    @Test
    fun `getRandomCoins — stays within NORMAL range`() {
        val config = ChestRewardConfigProvider.getConfig(ChestType.NORMAL)
        repeat(100) {
            val coins = config.getRandomCoins()
            assertTrue("Coins $coins in range ${config.coinRange}", coins in config.coinRange)
        }
    }

    @Test
    fun `getRandomCoins — stays within LEGENDARY range`() {
        val config = ChestRewardConfigProvider.getConfig(ChestType.LEGENDARY)
        repeat(100) {
            val coins = config.getRandomCoins()
            assertTrue("Coins $coins in range ${config.coinRange}", coins in config.coinRange)
        }
    }

    // ==================== getRandomExp ====================

    @Test
    fun `getRandomExp — stays within NORMAL range`() {
        val config = ChestRewardConfigProvider.getConfig(ChestType.NORMAL)
        repeat(100) {
            val exp = config.getRandomExp()
            assertTrue("Exp $exp in range ${config.expRange}", exp in config.expRange)
        }
    }

    @Test
    fun `getRandomExp — stays within EPIC range`() {
        val config = ChestRewardConfigProvider.getConfig(ChestType.EPIC)
        repeat(100) {
            val exp = config.getRandomExp()
            assertTrue("Exp $exp in range ${config.expRange}", exp in config.expRange)
        }
    }

    // ==================== getRandomChestType ====================

    @Test
    fun `getRandomChestType — returns valid ChestType`() {
        repeat(200) {
            val chestType = ChestRewardConfigProvider.getRandomChestType()
            assertNotNull("Chest type should not be null", chestType)
            assertTrue("Should be a valid ChestType", chestType in ChestType.entries)
        }
    }

    @Test
    fun `getRandomChestType — distribution is weighted toward NORMAL`() {
        val trials = 10000
        var normalCount = 0
        var rareCount = 0
        var epicCount = 0
        var legendaryCount = 0

        repeat(trials) {
            when (ChestRewardConfigProvider.getRandomChestType()) {
                ChestType.NORMAL -> normalCount++
                ChestType.RARE -> rareCount++
                ChestType.EPIC -> epicCount++
                ChestType.LEGENDARY -> legendaryCount++
            }
        }

        // NORMAL should be most common (~65%)
        assertTrue("NORMAL should be most common", normalCount > rareCount)
        assertTrue("RARE should be second most common", rareCount > epicCount)
        // EPIC and LEGENDARY are both rare; just verify they appear at least once
        assertTrue("EPIC should appear at least once", epicCount > 0)
        assertTrue("LEGENDARY should appear at least once", legendaryCount > 0)
    }

    // ==================== ChestRewardConfig default values ====================

    @Test
    fun `ChestRewardConfig — default values are sensible`() {
        val config = ChestRewardConfig(chestType = ChestType.NORMAL)

        assertEquals(0..0, config.coinRange)
        assertEquals(0..0, config.expRange)
        assertNull(config.customizationRarity)
        assertEquals(0.0, config.customizationDropChance, 0.001)
        assertFalse(config.guaranteedReward)
    }

    @Test
    fun `getRandomCoins — returns 0 for empty range`() {
        val config = ChestRewardConfig(chestType = ChestType.NORMAL, coinRange = 0..0)
        assertEquals(0, config.getRandomCoins())
    }

    @Test
    fun `getRandomExp — returns 0 for empty range`() {
        val config = ChestRewardConfig(chestType = ChestType.NORMAL, expRange = 0..0)
        assertEquals(0, config.getRandomExp())
    }
}
