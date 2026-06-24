package com.example.mobile.domain

import com.example.mobile.data.local.entities.ChallengeEntity
import com.example.mobile.domain.repository.ChallengeClaimResult
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChallengeEngineTest {

    private lateinit var challengeEngine: ChallengeEngine
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var activityTimelineEngine: ActivityTimelineEngine
    private lateinit var rewardQueue: RewardQueue

    private val testChallenge = ChallengeEntity(
        id = 1,
        challengeId = "habit_3",
        title = "Three habit rhythm",
        description = "Complete 3 habits at an easy pace.",
        icon = "habit",
        type = "HABIT_COMPLETION",
        targetValue = 3,
        progressValue = 3,
        rewardIdsJson = "[]",
        isCompleted = true,
        isClaimed = false
    )

    @Before
    fun setup() {
        challengeRepository = mock()
        inventoryItemRepository = mock()
        activityTimelineEngine = mock()
        rewardQueue = mock()

        challengeEngine = ChallengeEngine(
            challengeRepository = challengeRepository,
            inventoryItemRepository = inventoryItemRepository,
            activityTimelineEngine = activityTimelineEngine,
            rewardQueue = rewardQueue
        )
    }

    // ==================== delegation tests ====================

    @Test
    fun `recordHabitCompleted — delegates to repository`() = runTest {
        challengeEngine.recordHabitCompleted(habitId = 1L, xpEarned = 10L, coinsEarned = 10)
        verify(challengeRepository).recordHabitCompleted(1L, 10L, 10)
    }

    @Test
    fun `recordXpEarned — delegates to repository`() = runTest {
        challengeEngine.recordXpEarned(25L)
        verify(challengeRepository).recordXpEarned(25L)
    }

    @Test
    fun `recordCoinsEarned — delegates to repository`() = runTest {
        challengeEngine.recordCoinsEarned(20)
        verify(challengeRepository).recordCoinsEarned(20)
    }

    @Test
    fun `recordChestOpened — delegates to repository`() = runTest {
        challengeEngine.recordChestOpened()
        verify(challengeRepository).recordChestOpened()
    }

    @Test
    fun `recordStreak — delegates to repository`() = runTest {
        challengeEngine.recordStreak(5)
        verify(challengeRepository).recordStreak(5)
    }

    // ==================== claimActiveChallenge ====================

    @Test
    fun `claimActiveChallenge — empty rewards returns result`() = runTest {
        val claimResult = ChallengeClaimResult(
            challenge = testChallenge,
            rewards = emptyList()
        )
        whenever(challengeRepository.claimActiveChallenge()).thenReturn(claimResult)

        val result = challengeEngine.claimActiveChallenge()

        assertEquals(emptyList<Any>(), result.rewards)
        verify(rewardQueue, never()).addReward(any())
        verify(activityTimelineEngine, never()).logChallengeCompleted(any(), any())
    }

    @Test
    fun `claimActiveChallenge — coin reward adds to queue`() = runTest {
        val claimResult = ChallengeClaimResult(
            challenge = testChallenge,
            rewards = listOf(ChallengeRewardDefinition.CoinReward(25))
        )
        whenever(challengeRepository.claimActiveChallenge()).thenReturn(claimResult)

        challengeEngine.claimActiveChallenge()

        val captor = argumentCaptor<RewardUiEvent>()
        verify(rewardQueue).addReward(captor.capture())
        val reward = captor.firstValue
        assertTrue(reward is RewardUiEvent.CoinReward)
        assertEquals(25, (reward as RewardUiEvent.CoinReward).amount)
    }

    @Test
    fun `claimActiveChallenge — exp reward adds to queue`() = runTest {
        val claimResult = ChallengeClaimResult(
            challenge = testChallenge,
            rewards = listOf(ChallengeRewardDefinition.ExpReward(30L))
        )
        whenever(challengeRepository.claimActiveChallenge()).thenReturn(claimResult)

        challengeEngine.claimActiveChallenge()

        val captor = argumentCaptor<RewardUiEvent>()
        verify(rewardQueue).addReward(captor.capture())
        val reward = captor.firstValue
        assertTrue(reward is RewardUiEvent.ExpReward)
        assertEquals(30L, (reward as RewardUiEvent.ExpReward).amount)
    }

    @Test
    fun `claimActiveChallenge — chest reward builds and adds to queue`() = runTest {
        val claimResult = ChallengeClaimResult(
            challenge = testChallenge,
            rewards = listOf(ChallengeRewardDefinition.ChestReward(ChestType.NORMAL.name))
        )
        whenever(challengeRepository.claimActiveChallenge()).thenReturn(claimResult)

        challengeEngine.claimActiveChallenge()

        val captor = argumentCaptor<RewardUiEvent>()
        verify(rewardQueue).addReward(captor.capture())
        val reward = captor.firstValue
        assertTrue("Expected ChestReward", reward is RewardUiEvent.ChestReward)
    }

    @Test
    fun `claimActiveChallenge — customization reward adds to queue`() = runTest {
        val claimResult = ChallengeClaimResult(
            challenge = testChallenge,
            rewards = listOf(ChallengeRewardDefinition.CustomizationReward("outfit_robe"))
        )
        whenever(challengeRepository.claimActiveChallenge()).thenReturn(claimResult)

        challengeEngine.claimActiveChallenge()

        val captor = argumentCaptor<RewardUiEvent>()
        verify(rewardQueue).addReward(captor.capture())
        val reward = captor.firstValue
        assertTrue("Expected CustomizationReward", reward is RewardUiEvent.CustomizationReward)
        assertEquals("outfit_robe", (reward as RewardUiEvent.CustomizationReward).equipableId)
    }

    @Test
    fun `claimActiveChallenge — logs challenge completed to timeline`() = runTest {
        val claimResult = ChallengeClaimResult(
            challenge = testChallenge,
            rewards = listOf(ChallengeRewardDefinition.CoinReward(25))
        )
        whenever(challengeRepository.claimActiveChallenge()).thenReturn(claimResult)

        challengeEngine.claimActiveChallenge()

        verify(activityTimelineEngine).logChallengeCompleted(
            challengeName = "Three habit rhythm",
            rewards = claimResult.rewards
        )
    }

    @Test
    fun `claimActiveChallenge — maps chest type string to ChestType enum`() = runTest {
        val claimResult = ChallengeClaimResult(
            challenge = testChallenge,
            rewards = listOf(ChallengeRewardDefinition.ChestReward("RARE"))
        )
        whenever(challengeRepository.claimActiveChallenge()).thenReturn(claimResult)

        challengeEngine.claimActiveChallenge()

        val captor = argumentCaptor<RewardUiEvent>()
        verify(rewardQueue).addReward(captor.capture())
        val reward = captor.firstValue
        assertTrue("Expected ChestReward", reward is RewardUiEvent.ChestReward)
    }
}
