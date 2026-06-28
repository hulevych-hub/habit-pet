package com.example.mobile.domain

import com.example.mobile.data.local.entities.AchievementEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsConfigTest {

    // ==================== achievementById ====================

    @Test
    fun `achievementById — unknown id returns null`() {
        assertNull(AchievementsConfig.achievementById("does_not_exist"))
    }

    @Test
    fun `achievementById — known id returns definition`() {
        val definition = AchievementsConfig.achievementById(AchievementsConfig.FIRST_HABIT)

        assertNotNull(definition)
        assertEquals(AchievementsConfig.FIRST_HABIT, definition!!.id)
        assertEquals("First Habit", definition.name)
    }

    // ==================== toEntity ====================

    @Test
    fun `toEntity — normal target starts locked`() {
        val definition = AchievementsConfig.achievementById(AchievementsConfig.FIRST_HABIT)!!
        val entity = AchievementsConfig.toEntity(definition)

        assertEquals(AchievementsConfig.FIRST_HABIT, entity.id)
        assertEquals(0, entity.progress)
        assertFalse(entity.isUnlocked)
        assertFalse(entity.isClaimed)
        assertNull(entity.unlockedDate)
    }

    @Test
    fun `toEntity — null target starts unlocked with date`() {
        val definition = AchievementsConfig.AchievementDefinition(
            id = "instant",
            name = "Instant",
            description = "Instant unlock",
            icon = "instant",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = null,
            rewards = listOf(AchievementReward.CoinReward(10))
        )

        val entity = AchievementsConfig.toEntity(definition)

        assertTrue(entity.isUnlocked)
        assertNotNull(entity.unlockedDate)
    }

    // ==================== achievements list integrity ====================

    @Test
    fun `achievements — list is not empty`() {
        assertTrue(AchievementsConfig.achievements.isNotEmpty())
    }

    @Test
    fun `achievements — all ids are unique`() {
        val ids = AchievementsConfig.achievements.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `achievements — every id resolves back through achievementById`() {
        AchievementsConfig.achievements.forEach { definition ->
            val resolved = AchievementsConfig.achievementById(definition.id)
            assertNotNull("achievementById missing for ${definition.id}", resolved)
        }
    }

    @Test
    fun `achievements — every difficultyRank is unique`() {
        val ranks = AchievementsConfig.achievements.map { it.difficultyRank }
        assertEquals(ranks.size, ranks.toSet().size)
    }

    @Test
    fun `achievements — customization rewards reference valid equipable ids`() {
        AchievementsConfig.achievements.forEach { definition ->
            definition.rewards.forEach { reward ->
                if (reward is AchievementReward.CustomizationReward) {
                    assertNotNull(
                        "CustomizationReward ${reward.equipableId} in ${definition.id} is not in EquipableConfig",
                        EquipableConfig.definition(reward.equipableId)
                    )
                }
            }
        }
    }

    @Test
    fun `achievements — customization rewards use ACHIEVEMENT unlock source`() {
        AchievementsConfig.achievements.forEach { definition ->
            definition.rewards.forEach { reward ->
                if (reward is AchievementReward.CustomizationReward) {
                    val equipable = EquipableConfig.definition(reward.equipableId)
                    assertEquals(
                        "CustomizationReward ${reward.equipableId} in ${definition.id} must be ACHIEVEMENT source",
                        UnlockSources.ACHIEVEMENT,
                        equipable?.unlockSource
                    )
                }
            }
        }
    }

    @Test
    fun `achievements — every definition has a non-empty name and icon`() {
        AchievementsConfig.achievements.forEach { definition ->
            assertTrue("name empty for ${definition.id}", definition.name.isNotBlank())
            assertTrue("icon empty for ${definition.id}", definition.icon.isNotBlank())
        }
    }

    @Test
    fun `achievements — timed sources have a non-null target`() {
        val timedSources = setOf(
            AchievementProgressSource.HABIT_COUNT,
            AchievementProgressSource.CURRENT_STREAK,
            AchievementProgressSource.TOTAL_COMPLETIONS,
            AchievementProgressSource.TOTAL_XP,
            AchievementProgressSource.PET_LEVEL,
            AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            AchievementProgressSource.BEST_STREAK,
            AchievementProgressSource.BEST_COMBO,
            AchievementProgressSource.DAYS_ACTIVE,
            AchievementProgressSource.PET_AGE_DAYS,
            AchievementProgressSource.TOTAL_COINS,
            AchievementProgressSource.FREEZES_USED,
            AchievementProgressSource.CHALLENGES_COMPLETED,
            AchievementProgressSource.CHESTS_OPENED,
            AchievementProgressSource.DAILY_LOGINS,
            AchievementProgressSource.EVOLUTIONS,
            AchievementProgressSource.ACHIEVEMENTS_CLAIMED
        )

        AchievementsConfig.achievements.forEach { definition ->
            if (definition.progressSource in timedSources) {
                assertNotNull("targetValue null for timed achievement ${definition.id}", definition.targetValue)
                assertTrue("targetValue must be positive for ${definition.id}", definition.targetValue!! > 0)
            }
        }
    }
}
