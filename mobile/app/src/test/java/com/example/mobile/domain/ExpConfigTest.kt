package com.example.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpConfigTest {

    // ==================== xpRequiredForLevel ====================

    @Test
    fun `xpRequiredForLevel — level 1 returns 30`() {
        assertEquals(30L, ExpConfig.xpRequiredForLevel(1))
    }

    @Test
    fun `xpRequiredForLevel — level 2 returns 60`() {
        assertEquals(60L, ExpConfig.xpRequiredForLevel(2))
    }

    @Test
    fun `xpRequiredForLevel — level N follows linear formula`() {
        // Formula: 30 + (N-1) * 30
        assertEquals(90L, ExpConfig.xpRequiredForLevel(3))
        assertEquals(120L, ExpConfig.xpRequiredForLevel(4))
        assertEquals(150L, ExpConfig.xpRequiredForLevel(5))
        assertEquals(300L, ExpConfig.xpRequiredForLevel(10))
    }

    // ==================== totalXpRequiredForLevel ====================

    @Test
    fun `totalXpRequiredForLevel — level 0 returns 0`() {
        assertEquals(0L, ExpConfig.totalXpRequiredForLevel(0))
    }

    @Test
    fun `totalXpRequiredForLevel — level 1 returns 30`() {
        assertEquals(30L, ExpConfig.totalXpRequiredForLevel(1))
    }

    @Test
    fun `totalXpRequiredForLevel — level 2 returns 90 (30+60)`() {
        assertEquals(90L, ExpConfig.totalXpRequiredForLevel(2))
    }

    @Test
    fun `totalXpRequiredForLevel — matches 15*L*(L+1) formula`() {
        // 15 * 3 * 4 = 180
        assertEquals(180L, ExpConfig.totalXpRequiredForLevel(3))
        // 15 * 5 * 6 = 450
        assertEquals(450L, ExpConfig.totalXpRequiredForLevel(5))
        // 15 * 10 * 11 = 1650
        assertEquals(1650L, ExpConfig.totalXpRequiredForLevel(10))
    }

    // ==================== calculateLevelFromXp ====================

    @Test
    fun `calculateLevelFromXp — 0 XP = level 0`() {
        assertEquals(0, ExpConfig.calculateLevelFromXp(0))
    }

    @Test
    fun `calculateLevelFromXp — exactly at level boundary`() {
        // Level 1 boundary = 30 XP
        assertEquals(1, ExpConfig.calculateLevelFromXp(30))
        // Level 2 boundary = 90 XP
        assertEquals(2, ExpConfig.calculateLevelFromXp(90))
        // Level 3 boundary = 180 XP
        assertEquals(3, ExpConfig.calculateLevelFromXp(180))
    }

    @Test
    fun `calculateLevelFromXp — between levels returns lower level`() {
        assertEquals(0, ExpConfig.calculateLevelFromXp(29))
        assertEquals(1, ExpConfig.calculateLevelFromXp(31))
        assertEquals(1, ExpConfig.calculateLevelFromXp(89))
        assertEquals(2, ExpConfig.calculateLevelFromXp(100))
    }

    @Test
    fun `calculateLevelFromXp — very high XP`() {
        // Level 10 boundary = 1650
        assertEquals(10, ExpConfig.calculateLevelFromXp(1650))
        // Level 20: total = 15 * 20 * 21 = 6300
        assertEquals(20, ExpConfig.calculateLevelFromXp(6300))
    }

    // ==================== xpProgressInCurrentLevel ====================

    @Test
    fun `xpProgressInCurrentLevel — at level start returns 0`() {
        assertEquals(0L, ExpConfig.xpProgressInCurrentLevel(0))
        assertEquals(0L, ExpConfig.xpProgressInCurrentLevel(30))
        assertEquals(0L, ExpConfig.xpProgressInCurrentLevel(90))
    }

    @Test
    fun `xpProgressInCurrentLevel — at level end returns full`() {
        // At 89 XP, level 1, progress = 89 - 30 = 59
        assertEquals(59L, ExpConfig.xpProgressInCurrentLevel(89))
        // At 179 XP, level 2, progress = 179 - 90 = 89
        assertEquals(89L, ExpConfig.xpProgressInCurrentLevel(179))
    }

    // ==================== xpRequiredForNextLevel ====================

    @Test
    fun `xpRequiredForNextLevel — returns correct delta`() {
        // At 0 XP, need 30 for level 1
        assertEquals(30L, ExpConfig.xpRequiredForNextLevel(0))
        // At 30 XP (level 1), need 60 more for level 2
        assertEquals(60L, ExpConfig.xpRequiredForNextLevel(30))
        // At 45 XP (level 1, 15 progress), need 45 more for level 2
        assertEquals(45L, ExpConfig.xpRequiredForNextLevel(45))
    }

    // ==================== calculateEvolutionStageFromXp ====================

    @Test
    fun `calculateEvolutionStageFromXp — 0 XP = Egg (stage 0)`() {
        assertEquals(0, ExpConfig.calculateEvolutionStageFromXp(0))
    }

    @Test
    fun `calculateEvolutionStageFromXp — 74 XP = Egg`() {
        assertEquals(0, ExpConfig.calculateEvolutionStageFromXp(74))
    }

    @Test
    fun `calculateEvolutionStageFromXp — 75 XP = Hatchling (stage 1)`() {
        assertEquals(1, ExpConfig.calculateEvolutionStageFromXp(75))
    }

    @Test
    fun `calculateEvolutionStageFromXp — 300 XP = Young Dragon (stage 2)`() {
        assertEquals(2, ExpConfig.calculateEvolutionStageFromXp(300))
    }

    @Test
    fun `calculateEvolutionStageFromXp — 900 XP = Adult Dragon (stage 3)`() {
        assertEquals(3, ExpConfig.calculateEvolutionStageFromXp(900))
    }

    @Test
    fun `calculateEvolutionStageFromXp — 2500 XP = Ancient Dragon (stage 4)`() {
        assertEquals(4, ExpConfig.calculateEvolutionStageFromXp(2500))
    }

    @Test
    fun `calculateEvolutionStageFromXp — very high XP stays at Ancient`() {
        assertEquals(4, ExpConfig.calculateEvolutionStageFromXp(10000))
        assertEquals(4, ExpConfig.calculateEvolutionStageFromXp(100000))
    }

    // ==================== evolutionStageName ====================

    @Test
    fun `evolutionStageName — all 5 stages return correct names`() {
        assertEquals("Egg", ExpConfig.evolutionStageName(0))
        assertEquals("Hatchling", ExpConfig.evolutionStageName(1))
        assertEquals("Young Dragon", ExpConfig.evolutionStageName(2))
        assertEquals("Adult Dragon", ExpConfig.evolutionStageName(3))
        assertEquals("Ancient Dragon", ExpConfig.evolutionStageName(4))
    }

    // ==================== xpThresholdForStage ====================

    @Test
    fun `xpThresholdForStage — returns correct thresholds`() {
        assertEquals(0L, ExpConfig.xpThresholdForStage(0))
        assertEquals(75L, ExpConfig.xpThresholdForStage(1))
        assertEquals(300L, ExpConfig.xpThresholdForStage(2))
        assertEquals(900L, ExpConfig.xpThresholdForStage(3))
        assertEquals(2500L, ExpConfig.xpThresholdForStage(4))
    }

    // ==================== levelUpCoins ====================

    @Test
    fun `levelUpCoins — level 1 returns 10`() {
        assertEquals(10, ExpConfig.levelUpCoins(1))
    }

    @Test
    fun `levelUpCoins — level 5 returns 50`() {
        assertEquals(50, ExpConfig.levelUpCoins(5))
    }

    // ==================== isComboActive ====================

    @Test
    fun `isComboActive — within 2 hour window returns true`() {
        val now = 1_000_000_000L
        val oneHourAgo = now - (60L * 60L * 1000L)
        assertTrue(ExpConfig.isComboActive(oneHourAgo, now))
    }

    @Test
    fun `isComboActive — outside 2 hour window returns false`() {
        val now = 1_000_000_000L
        val threeHoursAgo = now - (3L * 60L * 60L * 1000L)
        assertFalse(ExpConfig.isComboActive(threeHoursAgo, now))
    }

    @Test
    fun `isComboActive — lastTimestamp = 0 returns false`() {
        val now = 1_000_000_000L
        assertFalse(ExpConfig.isComboActive(0L, now))
    }

    // ==================== comboBonusXp ====================

    @Test
    fun `comboBonusXp — combo 1 returns 0`() {
        assertEquals(0L, ExpConfig.comboBonusXp(1))
    }

    @Test
    fun `comboBonusXp — combo 2 returns 1`() {
        assertEquals(1L, ExpConfig.comboBonusXp(2))
    }

    @Test
    fun `comboBonusXp — combo 5 returns 4 (capped)`() {
        assertEquals(4L, ExpConfig.comboBonusXp(5))
    }

    @Test
    fun `comboBonusXp — combo 10 returns 4 (capped)`() {
        assertEquals(4L, ExpConfig.comboBonusXp(10))
    }

    // ==================== comboMultiplier ====================

    @Test
    fun `comboMultiplier — combo 1 returns 1_0f`() {
        assertEquals(1.0f, ExpConfig.comboMultiplier(1), 0.01f)
    }

    @Test
    fun `comboMultiplier — combo 5 returns 1_4f`() {
        // combo 5: bonus = 4, multiplier = 1 + 4/10 = 1.4
        assertEquals(1.4f, ExpConfig.comboMultiplier(5), 0.01f)
    }

    // ==================== comboMilestoneReached ====================

    @Test
    fun `comboMilestoneReached — 3, 5, 10 return true`() {
        assertTrue(ExpConfig.comboMilestoneReached(3))
        assertTrue(ExpConfig.comboMilestoneReached(5))
        assertTrue(ExpConfig.comboMilestoneReached(10))
    }

    @Test
    fun `comboMilestoneReached — 1, 2, 4, 6 return false`() {
        assertFalse(ExpConfig.comboMilestoneReached(1))
        assertFalse(ExpConfig.comboMilestoneReached(2))
        assertFalse(ExpConfig.comboMilestoneReached(4))
        assertFalse(ExpConfig.comboMilestoneReached(6))
    }
}
