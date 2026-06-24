package com.example.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DragonMoodTest {

    // ==================== DragonMood.from ====================

    @Test
    fun `DragonMood from — valid value returns correct enum`() {
        assertEquals(DragonMood.HAPPY, DragonMood.from("Happy"))
        assertEquals(DragonMood.CALM, DragonMood.from("Calm"))
        assertEquals(DragonMood.EXCITED, DragonMood.from("Excited"))
        assertEquals(DragonMood.PROUD, DragonMood.from("Proud"))
        assertEquals(DragonMood.LONELY, DragonMood.from("Lonely"))
    }

    @Test
    fun `DragonMood from — invalid value returns CALM default`() {
        assertEquals(DragonMood.CALM, DragonMood.from("Unknown"))
        assertEquals(DragonMood.CALM, DragonMood.from(""))
        assertEquals(DragonMood.CALM, DragonMood.from("HAPPY_"))
    }

    @Test
    fun `DragonMood from — case insensitive`() {
        assertEquals(DragonMood.HAPPY, DragonMood.from("happy"))
        assertEquals(DragonMood.HAPPY, DragonMood.from("HAPPY"))
        assertEquals(DragonMood.HAPPY, DragonMood.from("hApPy"))
        assertEquals(DragonMood.CALM, DragonMood.from("CALM"))
        assertEquals(DragonMood.LONELY, DragonMood.from("lonely"))
    }

    // ==================== DragonMood.calculate ====================

    @Test
    fun `calculate — no activity, no streak = CALM`() {
        val mood = DragonMood.calculate(
            currentStreak = 0,
            lastActivityTimestamp = 0L,
            recentHabitCompletions = 0
        )
        assertEquals(DragonMood.CALM, mood)
    }

    @Test
    fun `calculate — streak above 0, no recent activity within 36h = HAPPY`() {
        val now = System.currentTimeMillis()
        val twelveHoursAgo = now - (12L * 60L * 60L * 1000L)
        val mood = DragonMood.calculate(
            currentStreak = 3,
            lastActivityTimestamp = twelveHoursAgo,
            recentHabitCompletions = 0
        )
        assertEquals(DragonMood.HAPPY, mood)
    }

    @Test
    fun `calculate — streak at least 7 = PROUD`() {
        val now = System.currentTimeMillis()
        val twelveHoursAgo = now - (12L * 60L * 60L * 1000L)
        val mood = DragonMood.calculate(
            currentStreak = 7,
            lastActivityTimestamp = twelveHoursAgo,
            recentHabitCompletions = 0
        )
        assertEquals(DragonMood.PROUD, mood)
    }

    @Test
    fun `calculate — recent completions at least 3 = EXCITED`() {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60L * 60L * 1000L)
        val mood = DragonMood.calculate(
            currentStreak = 2,
            lastActivityTimestamp = oneHourAgo,
            recentHabitCompletions = 3
        )
        assertEquals(DragonMood.EXCITED, mood)
    }

    @Test
    fun `calculate — last activity at least 36h ago = LONELY`() {
        val now = System.currentTimeMillis()
        val fortyHoursAgo = now - (40L * 60L * 60L * 1000L)
        val mood = DragonMood.calculate(
            currentStreak = 0,
            lastActivityTimestamp = fortyHoursAgo,
            recentHabitCompletions = 0
        )
        assertEquals(DragonMood.LONELY, mood)
    }

    @Test
    fun `calculate — LONELY takes priority over PROUD when both conditions met`() {
        val now = System.currentTimeMillis()
        val fortyHoursAgo = now - (40L * 60L * 60L * 1000L)
        val mood = DragonMood.calculate(
            currentStreak = 10,
            lastActivityTimestamp = fortyHoursAgo,
            recentHabitCompletions = 0
        )
        assertEquals(DragonMood.LONELY, mood)
    }

    @Test
    fun `calculate — PROUD takes priority over EXCITED`() {
        val now = System.currentTimeMillis()
        val twelveHoursAgo = now - (12L * 60L * 60L * 1000L)
        val mood = DragonMood.calculate(
            currentStreak = 8,
            lastActivityTimestamp = twelveHoursAgo,
            recentHabitCompletions = 5
        )
        assertEquals(DragonMood.PROUD, mood)
    }

    @Test
    fun `calculate — zero lastActivityTimestamp = 0 hours (not LONELY)`() {
        val mood = DragonMood.calculate(
            currentStreak = 0,
            lastActivityTimestamp = 0L,
            recentHabitCompletions = 0
        )
        assertEquals(DragonMood.CALM, mood)
    }
}
