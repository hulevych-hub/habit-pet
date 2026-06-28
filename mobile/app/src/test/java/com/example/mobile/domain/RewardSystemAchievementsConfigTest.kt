package com.example.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardSystemAchievementsConfigTest {

    @Test
    fun `logins_100 achievement exists with correct source`() {
        val def = AchievementsConfig.achievementById("logins_100")
        assertNotNull(def)
        assertEquals(AchievementProgressSource.DAILY_LOGINS, def?.progressSource)
        assertEquals(100, def?.targetValue)
    }

    @Test
    fun `logins_365 achievement exists with correct source`() {
        val def = AchievementsConfig.achievementById("logins_365")
        assertNotNull(def)
        assertEquals(AchievementProgressSource.DAILY_LOGINS, def?.progressSource)
        assertEquals(365, def?.targetValue)
    }

    @Test
    fun `titles_1 achievement exists with correct source`() {
        val def = AchievementsConfig.achievementById("titles_1")
        assertNotNull(def)
        assertEquals(AchievementProgressSource.TITLES_UNLOCKED, def?.progressSource)
        assertEquals(1, def?.targetValue)
    }

    @Test
    fun `titles_3 achievement exists with correct source`() {
        val def = AchievementsConfig.achievementById("titles_3")
        assertNotNull(def)
        assertEquals(AchievementProgressSource.TITLES_UNLOCKED, def?.progressSource)
        assertEquals(3, def?.targetValue)
    }

    @Test
    fun `frames_1 achievement exists with correct source`() {
        val def = AchievementsConfig.achievementById("frames_1")
        assertNotNull(def)
        assertEquals(AchievementProgressSource.FRAMES_UNLOCKED, def?.progressSource)
        assertEquals(1, def?.targetValue)
    }

    @Test
    fun `frames_3 achievement exists with correct source`() {
        val def = AchievementsConfig.achievementById("frames_3")
        assertNotNull(def)
        assertEquals(AchievementProgressSource.FRAMES_UNLOCKED, def?.progressSource)
        assertEquals(3, def?.targetValue)
    }

    @Test
    fun `sets_completed_1 achievement exists with correct source`() {
        val def = AchievementsConfig.achievementById("sets_completed_1")
        assertNotNull(def)
        assertEquals(AchievementProgressSource.SETS_COMPLETED, def?.progressSource)
        assertEquals(1, def?.targetValue)
    }

    @Test
    fun `sets_completed_3 achievement exists with correct source`() {
        val def = AchievementsConfig.achievementById("sets_completed_3")
        assertNotNull(def)
        assertEquals(AchievementProgressSource.SETS_COMPLETED, def?.progressSource)
        assertEquals(3, def?.targetValue)
    }

    @Test
    fun `all new achievements have non-empty rewards`() {
        val newIds = listOf(
            "logins_100", "logins_365",
            "titles_1", "titles_3",
            "frames_1", "frames_3",
            "sets_completed_1", "sets_completed_3"
        )
        for (id in newIds) {
            val def = AchievementsConfig.achievementById(id)
            assertNotNull("Achievement $id should exist", def)
            assertTrue("Achievement $id should have rewards", !def!!.rewards.isEmpty())
        }
    }

    @Test
    fun `total achievement count is at least 86`() {
        assertTrue(
            "Should have at least 86 achievements",
            AchievementsConfig.achievements.size >= 86
        )
    }
}
