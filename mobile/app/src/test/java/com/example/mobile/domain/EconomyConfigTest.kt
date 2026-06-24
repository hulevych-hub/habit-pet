package com.example.mobile.domain

import com.example.mobile.data.local.entities.Rarity
import org.junit.Assert.assertEquals
import org.junit.Test

class EconomyConfigTest {

    // ==================== customizationPrice ====================

    @Test
    fun `customizationPrice — NORMAL rarity returns 120`() {
        // 100 * 1.2 = 120
        assertEquals(120, EconomyConfig.customizationPrice(Rarity.NORMAL))
    }

    @Test
    fun `customizationPrice — RARE rarity returns 400`() {
        // 100 * 4.0 = 400
        assertEquals(400, EconomyConfig.customizationPrice(Rarity.RARE))
    }

    @Test
    fun `customizationPrice — EPIC rarity returns 1000`() {
        // 100 * 10.0 = 1000
        assertEquals(1000, EconomyConfig.customizationPrice(Rarity.EPIC))
    }

    @Test
    fun `customizationPrice — LEGENDARY rarity returns 3000`() {
        // 100 * 30.0 = 3000
        assertEquals(3000, EconomyConfig.customizationPrice(Rarity.LEGENDARY))
    }

    // ==================== ceilDiv ====================

    @Test
    fun `ceilDiv — exact division`() {
        // Accessing private function via copy of logic
        // ceilDiv(10, 5) = 2
        assertEquals(2, ceilDiv(10, 5))
        assertEquals(3, ceilDiv(9, 3))
        assertEquals(5, ceilDiv(25, 5))
    }

    @Test
    fun `ceilDiv — remainder rounds up`() {
        assertEquals(4, ceilDiv(10, 3))
        assertEquals(4, ceilDiv(11, 3))
        assertEquals(2, ceilDiv(7, 4))
    }

    @Test
    fun `ceilDiv — zero value returns 0`() {
        assertEquals(0, ceilDiv(0, 5))
        assertEquals(0, ceilDiv(0, 1))
    }

    // ==================== DAYS_FOR_*_CUSTOMIZATION ====================

    @Test
    fun `DAYS_FOR_NORMAL_CUSTOMIZATION — computed value matches target`() {
        // TARGET_NORMAL_CUSTOMIZATION_COST = 120, TARGET_DAILY_COINS = 75
        // ceilDiv(120, 75) = 2
        assertEquals(2, EconomyConfig.DAYS_FOR_NORMAL_CUSTOMIZATION)
    }

    @Test
    fun `DAYS_FOR_RARE_CUSTOMIZATION — computed value matches target`() {
        // ceilDiv(400, 75) = 6
        assertEquals(6, EconomyConfig.DAYS_FOR_RARE_CUSTOMIZATION)
    }

    @Test
    fun `DAYS_FOR_EPIC_CUSTOMIZATION — computed value matches target`() {
        // ceilDiv(1000, 75) = 14
        assertEquals(14, EconomyConfig.DAYS_FOR_EPIC_CUSTOMIZATION)
    }

    @Test
    fun `DAYS_FOR_LEGENDARY_CUSTOMIZATION — computed value matches target`() {
        // ceilDiv(3000, 75) = 40
        assertEquals(40, EconomyConfig.DAYS_FOR_LEGENDARY_CUSTOMIZATION)
    }

    // Helper mirroring private ceilDiv for direct testing
    private fun ceilDiv(value: Int, divisor: Int): Int {
        require(divisor > 0) { "Divisor must be positive" }
        return (value + divisor - 1) / divisor
    }
}
