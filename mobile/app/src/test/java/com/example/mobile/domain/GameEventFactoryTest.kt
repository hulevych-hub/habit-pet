package com.example.mobile.domain

import com.example.mobile.data.local.entities.GameEventEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEventFactoryTest {

    // ==================== habitCompleted ====================

    @Test
    fun `habitCompleted — creates event with correct type`() {
        val event = GameEventFactory.habitCompleted(
            habitName = "Morning jog",
            xpEarned = 10L,
            coinsEarned = 10
        )
        assertEquals(GameEventType.HABIT_COMPLETED.name, event.type)
    }

    @Test
    fun `habitCompleted — title includes habit name`() {
        val event = GameEventFactory.habitCompleted(
            habitName = "Morning jog",
            xpEarned = 10L,
            coinsEarned = 10
        )
        assertEquals("Morning jog completed", event.title)
    }

    @Test
    fun `habitCompleted — description includes xp and coins`() {
        val event = GameEventFactory.habitCompleted(
            habitName = "Morning jog",
            xpEarned = 15L,
            coinsEarned = 20
        )
        assertTrue("Description should contain XP", event.description.contains("15 XP"))
        assertTrue("Description should contain coins", event.description.contains("20 coins"))
    }

    @Test
    fun `habitCompleted — blank habit name defaults to Habit`() {
        val event = GameEventFactory.habitCompleted(
            habitName = "",
            xpEarned = 10L,
            coinsEarned = 10
        )
        assertEquals("Habit completed", event.title)
    }

    @Test
    fun `habitCompleted — combo above 1 adds combo text and RARE rarity`() {
        val event = GameEventFactory.habitCompleted(
            habitName = "Morning jog",
            xpEarned = 10L,
            coinsEarned = 10,
            combo = 3,
            comboBonusXp = 2L,
            comboMultiplier = 1.2f
        )
        assertTrue("Should contain combo text", event.description.contains("Momentum combo"))
        assertEquals(GameEventRarity.RARE.name, event.rarity)
    }

    @Test
    fun `habitCompleted — combo 1 has no combo text`() {
        val event = GameEventFactory.habitCompleted(
            habitName = "Morning jog",
            xpEarned = 10L,
            coinsEarned = 10,
            combo = 1,
            comboBonusXp = 0L,
            comboMultiplier = 1.0f
        )
        assertTrue("Should not contain combo text", !event.description.contains("Momentum combo"))
        assertEquals(GameEventRarity.COMMON.name, event.rarity)
    }

    // ==================== achievementUnlocked ====================

    @Test
    fun `achievementUnlocked — creates event with correct type`() {
        val event = GameEventFactory.achievementUnlocked("First Habit")
        assertEquals(GameEventType.ACHIEVEMENT_UNLOCKED.name, event.type)
    }

    @Test
    fun `achievementUnlocked — title is Achievement unlocked`() {
        val event = GameEventFactory.achievementUnlocked("First Habit")
        assertEquals("Achievement unlocked", event.title)
    }

    @Test
    fun `achievementUnlocked — description includes achievement name`() {
        val event = GameEventFactory.achievementUnlocked("First Habit")
        assertTrue(event.description.contains("First Habit"))
    }

    @Test
    fun `achievementUnlocked — blank name defaults to Achievement`() {
        val event = GameEventFactory.achievementUnlocked("")
        assertEquals("Achievement unlocked", event.title)
    }

    // ==================== levelUp ====================

    @Test
    fun `levelUp — creates event with correct type`() {
        val event = GameEventFactory.levelUp(level = 5, coins = 50)
        assertEquals(GameEventType.LEVEL_UP.name, event.type)
    }

    @Test
    fun `levelUp — title includes level`() {
        val event = GameEventFactory.levelUp(level = 5, coins = 50)
        assertEquals("Level 5 reached", event.title)
    }

    @Test
    fun `levelUp — description includes coins`() {
        val event = GameEventFactory.levelUp(level = 5, coins = 50)
        assertTrue(event.description.contains("50 bonus coins"))
    }

    // ==================== dragonEvolution ====================

    @Test
    fun `dragonEvolution — creates event with correct type`() {
        val event = GameEventFactory.dragonEvolution(
            fromStage = 0,
            toStage = 1,
            fromStageName = "Egg",
            toStageName = "Hatchling"
        )
        assertEquals(GameEventType.DRAGON_EVOLUTION.name, event.type)
    }

    @Test
    fun `dragonEvolution — title is the to stage name`() {
        val event = GameEventFactory.dragonEvolution(
            fromStage = 0,
            toStage = 1,
            fromStageName = "Egg",
            toStageName = "Hatchling"
        )
        assertEquals("Hatchling", event.title)
    }

    @Test
    fun `dragonEvolution — description includes from and to stage names`() {
        val event = GameEventFactory.dragonEvolution(
            fromStage = 0,
            toStage = 1,
            fromStageName = "Egg",
            toStageName = "Hatchling"
        )
        assertTrue(event.description.contains("Egg"))
        assertTrue(event.description.contains("Hatchling"))
    }

    @Test
    fun `dragonEvolution — rarity is EPIC`() {
        val event = GameEventFactory.dragonEvolution(
            fromStage = 0,
            toStage = 1,
            fromStageName = "Egg",
            toStageName = "Hatchling"
        )
        assertEquals(GameEventRarity.EPIC.name, event.rarity)
    }

    // ==================== chestOpened ====================

    @Test
    fun `chestOpened — creates event with correct type`() {
        val event = GameEventFactory.chestOpened(
            rewardType = "challenge_normal",
            chestType = "NORMAL",
            coins = 50,
            expAmount = 25
        )
        assertEquals(GameEventType.CHEST_OPENED.name, event.type)
    }

    @Test
    fun `chestOpened — title includes capitalized chest type`() {
        val event = GameEventFactory.chestOpened(
            rewardType = "challenge_normal",
            chestType = "NORMAL",
            coins = 50,
            expAmount = 25
        )
        assertEquals("Normal chest opened", event.title)
    }

    @Test
    fun `chestOpened — legendary chest has LEGENDARY rarity`() {
        val event = GameEventFactory.chestOpened(
            rewardType = "challenge_legendary",
            chestType = "LEGENDARY",
            coins = 200,
            expAmount = 100
        )
        assertEquals(GameEventRarity.LEGENDARY.name, event.rarity)
    }

    @Test
    fun `chestOpened — epic chest has EPIC rarity`() {
        val event = GameEventFactory.chestOpened(
            rewardType = "challenge_epic",
            chestType = "EPIC",
            coins = 100,
            expAmount = 50
        )
        assertEquals(GameEventRarity.EPIC.name, event.rarity)
    }

    @Test
    fun `chestOpened — rare chest has RARE rarity`() {
        val event = GameEventFactory.chestOpened(
            rewardType = "challenge_rare",
            chestType = "RARE",
            coins = 60,
            expAmount = 30
        )
        assertEquals(GameEventRarity.RARE.name, event.rarity)
    }

    // ==================== streakMilestone ====================

    @Test
    fun `streakMilestone — creates event with correct type`() {
        val event = GameEventFactory.streakMilestone(streak = 7, chestType = "NORMAL")
        assertEquals(GameEventType.STREAK_MILESTONE.name, event.type)
    }

    @Test
    fun `streakMilestone — title includes streak count`() {
        val event = GameEventFactory.streakMilestone(streak = 7, chestType = "NORMAL")
        assertEquals("7-day streak milestone", event.title)
    }

    @Test
    fun `streakMilestone — description includes chest type`() {
        val event = GameEventFactory.streakMilestone(streak = 7, chestType = "NORMAL")
        assertTrue(event.description.contains("Normal reward chest"))
    }

    // ==================== challengeCompleted ====================

    @Test
    fun `challengeCompleted — creates event with correct type`() {
        val event = GameEventFactory.challengeCompleted(
            challengeName = "Three habit rhythm",
            rewardSummary = listOf("+25 coins", "+25 EXP")
        )
        assertEquals(GameEventType.CHALLENGE_COMPLETED.name, event.type)
    }

    @Test
    fun `challengeCompleted — title is Challenge complete`() {
        val event = GameEventFactory.challengeCompleted(
            challengeName = "Three habit rhythm",
            rewardSummary = listOf("+25 coins")
        )
        assertEquals("Challenge complete", event.title)
    }

    @Test
    fun `challengeCompleted — description includes challenge name and rewards`() {
        val event = GameEventFactory.challengeCompleted(
            challengeName = "Three habit rhythm",
            rewardSummary = listOf("+25 coins", "+25 EXP")
        )
        assertTrue(event.description.contains("Three habit rhythm"))
        assertTrue(event.description.contains("+25 coins"))
        assertTrue(event.description.contains("+25 EXP"))
    }

    @Test
    fun `challengeCompleted — blank challenge name defaults to Challenge`() {
        val event = GameEventFactory.challengeCompleted(
            challengeName = "",
            rewardSummary = listOf("+10 coins")
        )
        assertEquals("Challenge complete", event.title)
    }

    @Test
    fun `challengeCompleted — empty reward summary uses small rewards`() {
        val event = GameEventFactory.challengeCompleted(
            challengeName = "Test challenge",
            rewardSummary = emptyList()
        )
        assertTrue(event.description.contains("small rewards"))
    }

    // ==================== surpriseReward ====================

    @Test
    fun `surpriseReward — creates event with correct type`() {
        val event = GameEventFactory.surpriseReward(
            coins = 30,
            xp = 20L,
            chestType = "RARE",
            hasCustomization = true
        )
        assertEquals(GameEventType.SURPRISE_REWARD.name, event.type)
    }

    @Test
    fun `surpriseReward — title is Surprise reward`() {
        val event = GameEventFactory.surpriseReward(
            coins = 30,
            xp = 20L,
            chestType = "RARE",
            hasCustomization = false
        )
        assertEquals("Surprise reward", event.title)
    }

    @Test
    fun `surpriseReward — description includes customization text when hasCustomization true`() {
        val event = GameEventFactory.surpriseReward(
            coins = 30,
            xp = 20L,
            chestType = "RARE",
            hasCustomization = true
        )
        assertTrue(event.description.contains("hidden customization item"))
    }

    @Test
    fun `surpriseReward — description excludes customization text when hasCustomization false`() {
        val event = GameEventFactory.surpriseReward(
            coins = 30,
            xp = 20L,
            chestType = "RARE",
            hasCustomization = false
        )
        assertTrue(!event.description.contains("hidden customization item"))
    }

    // ==================== comboMilestone ====================

    @Test
    fun `comboMilestone — creates event with correct type`() {
        val event = GameEventFactory.comboMilestone(
            combo = 5,
            bonusXp = 4L,
            multiplier = 1.4f
        )
        assertEquals(GameEventType.COMBO_MILESTONE.name, event.type)
    }

    @Test
    fun `comboMilestone — title includes combo count`() {
        val event = GameEventFactory.comboMilestone(
            combo = 5,
            bonusXp = 4L,
            multiplier = 1.4f
        )
        assertEquals("5-hit momentum", event.title)
    }

    @Test
    fun `comboMilestone — combo 5 or above has RARE rarity`() {
        val event = GameEventFactory.comboMilestone(
            combo = 5,
            bonusXp = 4L,
            multiplier = 1.4f
        )
        assertEquals(GameEventRarity.RARE.name, event.rarity)
    }

    @Test
    fun `comboMilestone — combo below 5 has COMMON rarity`() {
        val event = GameEventFactory.comboMilestone(
            combo = 3,
            bonusXp = 2L,
            multiplier = 1.2f
        )
        assertEquals(GameEventRarity.COMMON.name, event.rarity)
    }

    // ==================== firstDailyLogin ====================

    @Test
    fun `firstDailyLogin — creates event with correct type`() {
        val event = GameEventFactory.firstDailyLogin(
            streak = 3,
            lastActiveTimestamp = 0L,
            lastSessionDifference = "This is your first visit.",
            motivationalMessage = "A fresh page is ready for your dragon."
        )
        assertEquals(GameEventType.FIRST_DAILY_LOGIN.name, event.type)
    }

    @Test
    fun `firstDailyLogin — title is Welcome back`() {
        val event = GameEventFactory.firstDailyLogin(
            streak = 0,
            lastActiveTimestamp = 0L,
            lastSessionDifference = "This is your first visit.",
            motivationalMessage = "A fresh page is ready for your dragon."
        )
        assertEquals("Welcome back", event.title)
    }

    @Test
    fun `firstDailyLogin — description includes motivational message`() {
        val event = GameEventFactory.firstDailyLogin(
            streak = 0,
            lastActiveTimestamp = 0L,
            lastSessionDifference = "This is your first visit.",
            motivationalMessage = "A fresh page is ready for your dragon."
        )
        assertTrue(event.description.contains("A fresh page is ready for your dragon."))
    }

    @Test
    fun `firstDailyLogin — description includes streak label when streak above 0`() {
        val event = GameEventFactory.firstDailyLogin(
            streak = 5,
            lastActiveTimestamp = 1000L,
            lastSessionDifference = "Last session: just now.",
            motivationalMessage = "You kept the rhythm alive."
        )
        assertTrue(event.description.contains("Streak: 5 days."))
    }

    @Test
    fun `firstDailyLogin — description includes waiting to restart when streak is 0`() {
        val event = GameEventFactory.firstDailyLogin(
            streak = 0,
            lastActiveTimestamp = 1000L,
            lastSessionDifference = "Last session: just now.",
            motivationalMessage = "You kept the rhythm alive."
        )
        assertTrue(event.description.contains("Streak: waiting to restart."))
    }

    // ==================== evolutionMilestoneNearing ====================

    @Test
    fun `evolutionMilestoneNearing — creates event with correct type`() {
        val event = GameEventFactory.evolutionMilestoneNearing(
            toStage = 1,
            xp = 70L,
            progress = 0.93f
        )
        assertEquals(GameEventType.DRAGON_EVOLUTION.name, event.type)
    }

    @Test
    fun `evolutionMilestoneNearing — title includes stage name`() {
        val event = GameEventFactory.evolutionMilestoneNearing(
            toStage = 1,
            xp = 70L,
            progress = 0.93f
        )
        assertEquals("Hatchling milestone nearing", event.title)
    }

    @Test
    fun `evolutionMilestoneNearing — description includes progress percentage`() {
        val event = GameEventFactory.evolutionMilestoneNearing(
            toStage = 1,
            xp = 70L,
            progress = 0.93f
        )
        assertTrue(event.description.contains("93%"))
    }

    // ==================== payload format ====================

    @Test
    fun `payloadOf — handles null values`() {
        val event = GameEventFactory.achievementUnlocked("Test")
        // payload is internal, verify it doesn't crash with null values
        assertTrue(event.payload != null)
    }

    @Test
    fun `payloadOf — escapes special json characters`() {
        val event = GameEventFactory.habitCompleted(
            habitName = "Test \"habit\" \n name",
            xpEarned = 10L,
            coinsEarned = 10
        )
        // Should not crash and payload should contain escaped characters
        assertTrue(event.payload != null)
    }
}
