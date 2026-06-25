package com.example.mobile.domain

import com.example.mobile.data.local.dao.ChallengeDao
import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.InventoryItemDao
import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.ChallengeEntity
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.data.repository.ChallengeRepositoryImpl
import com.example.mobile.domain.repository.ChallengeClaimResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Integration test exercising the challenge progress + claim flow:
 * ChallengeRepositoryImpl -> ChallengeDao -> ChallengeConfig
 *
 * Verifies:
 *   - recordHabitCompleted increments progress for HABIT_COMPLETION challenge
 *   - recordXpEarned increments progress for XP_EARNED challenge
 *   - recordCoinsEarned increments progress for COINS_EARNED challenge
 *   - Progress is capped at target value
 *   - Reaching target sets isCompleted = true
 *   - claimActiveChallenge returns rewards and creates next challenge
 *   - claimActiveChallenge on incomplete challenge returns empty rewards
 *   - record on claimed challenge is no-op
 */
class ChallengeProgressFlowIntegrationTest {

    private lateinit var challengeDao: ChallengeDao
    private lateinit var statisticsDao: StatisticsDao
    private lateinit var habitCompletionDao: HabitCompletionDao
    private lateinit var habitDao: HabitDao
    private lateinit var inventoryItemDao: InventoryItemDao
    private lateinit var petDao: PetDao
    private lateinit var challengeRepository: ChallengeRepositoryImpl

    private val challengeFlow = MutableStateFlow<ChallengeEntity?>(null)
    private val statsFlow = MutableStateFlow<StatisticsEntity>(StatisticsEntity())
    private val inventoryFlow = MutableStateFlow<List<InventoryItemEntity>>(emptyList())
    private val petFlow = MutableStateFlow<PetEntity>(PetEntity())

    // Track DAO call counts
    private var upsertCallCount = 0
    private var upsertLastEntity: ChallengeEntity? = null
    private var getActiveChallengeOnceCallCount = 0

    // Predefined challenge entities
    private val habit1Entity = ChallengeEntity(
        id = 1,
        challengeId = "habit_1",
        title = "One tiny win",
        description = "Complete 1 habit and give your dragon a small cheer.",
        icon = "habit",
        type = "HABIT_COMPLETION",
        targetValue = 1,
        progressValue = 0,
        rewardIdsJson = "[{\"type\":\"COIN\",\"amount\":15},{\"type\":\"EXP\",\"amount\":10}]",
        createdAt = System.currentTimeMillis()
    )

    private val habit3Entity = ChallengeEntity(
        id = 1,
        challengeId = "habit_3",
        title = "Three habit rhythm",
        description = "Complete 3 habits at an easy pace.",
        icon = "habit",
        type = "HABIT_COMPLETION",
        targetValue = 3,
        progressValue = 0,
        rewardIdsJson = "[{\"type\":\"COIN\",\"amount\":25},{\"type\":\"EXP\",\"amount\":25}]",
        createdAt = System.currentTimeMillis()
    )

    private val xp25Entity = ChallengeEntity(
        id = 1,
        challengeId = "xp_25",
        title = "XP spark",
        description = "Earn 25 XP through small wins.",
        icon = "xp",
        type = "XP_EARNED",
        targetValue = 25,
        progressValue = 0,
        rewardIdsJson = "[{\"type\":\"COIN\",\"amount\":15},{\"type\":\"EXP\",\"amount\":10}]",
        createdAt = System.currentTimeMillis()
    )

    private val coins20Entity = ChallengeEntity(
        id = 1,
        challengeId = "coins_20",
        title = "Coin crumbs",
        description = "Collect 20 coins from gentle progress.",
        icon = "coins",
        type = "COINS_EARNED",
        targetValue = 20,
        progressValue = 0,
        rewardIdsJson = "[{\"type\":\"EXP\",\"amount\":20},{\"type\":\"COIN\",\"amount\":20}]",
        createdAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        challengeDao = mock()
        statisticsDao = mock()
        habitCompletionDao = mock()
        habitDao = mock()
        inventoryItemDao = mock()
        petDao = mock()

        upsertCallCount = 0
        upsertLastEntity = null
        getActiveChallengeOnceCallCount = 0

        runTest {
            whenever(challengeDao.getActiveChallenge()).thenReturn(challengeFlow)
            whenever(challengeDao.getActiveChallengeOnce()).thenAnswer {
                getActiveChallengeOnceCallCount++
                challengeFlow.value
            }
            whenever(challengeDao.upsert(any())).thenAnswer { invocation ->
                upsertCallCount++
                upsertLastEntity = invocation.getArgument(0) as ChallengeEntity
                // Update the flow to reflect the upsert
                challengeFlow.value = upsertLastEntity
                1L
            }
            whenever(challengeDao.count()).thenReturn(1)
        }

        whenever(statisticsDao.getStatistics()).thenReturn(statsFlow)
        whenever(inventoryItemDao.getAllItems()).thenReturn(inventoryFlow)
        whenever(petDao.getPet()).thenReturn(petFlow)

        challengeRepository = ChallengeRepositoryImpl(
            challengeDao = challengeDao,
            statisticsDao = statisticsDao,
            habitCompletionDao = habitCompletionDao,
            habitDao = habitDao,
            inventoryItemDao = inventoryItemDao,
            petDao = petDao
        )
    }

    private fun setChallenge(entity: ChallengeEntity?) {
        challengeFlow.value = entity
    }

    @Test
    fun `progress — recordHabitCompleted increments progress for HABIT_COMPLETION challenge`() = runTest {
        setChallenge(habit3Entity)

        challengeRepository.recordHabitCompleted(habitId = 1L, xpEarned = 10L, coinsEarned = 10)

        assertEquals("upsert should be called once", 1, upsertCallCount)
        val updated = upsertLastEntity!!
        assertEquals("Progress should be 1", 1, updated.progressValue)
        assertEquals("Challenge ID should remain", "habit_3", updated.challengeId)
    }

    @Test
    fun `progress — recordHabitCompleted twice increments to 2`() = runTest {
        setChallenge(habit3Entity)

        challengeRepository.recordHabitCompleted(habitId = 1L, xpEarned = 10L, coinsEarned = 10)
        challengeRepository.recordHabitCompleted(habitId = 2L, xpEarned = 10L, coinsEarned = 10)

        assertEquals("upsert should be called twice", 2, upsertCallCount)
        val updated = upsertLastEntity!!
        assertEquals("Progress should be 2", 2, updated.progressValue)
        assertFalse("Should not be completed yet", updated.isCompleted)
    }

    @Test
    fun `progress — reaching target sets isCompleted true`() = runTest {
        setChallenge(habit1Entity) // target = 1

        challengeRepository.recordHabitCompleted(habitId = 1L, xpEarned = 10L, coinsEarned = 10)

        assertEquals("upsert should be called once", 1, upsertCallCount)
        val updated = upsertLastEntity!!
        assertEquals("Progress should be 1", 1, updated.progressValue)
        assertTrue("Should be completed when target reached", updated.isCompleted)
        assertNotNull("completedAt should be set", updated.completedAt)
    }

    @Test
    fun `progress — XP_EARNED challenge increments by xp amount`() = runTest {
        setChallenge(xp25Entity)

        challengeRepository.recordXpEarned(15L)

        assertEquals("upsert should be called once", 1, upsertCallCount)
        val updated = upsertLastEntity!!
        assertEquals("Progress should be 15", 15, updated.progressValue)
        assertFalse("Should not be completed at 15/25", updated.isCompleted)
    }

    @Test
    fun `progress — XP_EARNED challenge reaches target`() = runTest {
        setChallenge(xp25Entity)

        challengeRepository.recordXpEarned(25L)

        val updated = upsertLastEntity!!
        assertEquals("Progress should be 25", 25, updated.progressValue)
        assertTrue("Should be completed at 25/25", updated.isCompleted)
    }

    @Test
    fun `progress — COINS_EARNED challenge increments by coins amount`() = runTest {
        setChallenge(coins20Entity)

        challengeRepository.recordCoinsEarned(12)

        val updated = upsertLastEntity!!
        assertEquals("Progress should be 12", 12, updated.progressValue)
        assertFalse("Should not be completed at 12/20", updated.isCompleted)
    }

    @Test
    fun `progress — progress is capped at target value`() = runTest {
        setChallenge(habit1Entity) // target = 1

        // Record 5 habits — should cap at 1
        repeat(5) {
            challengeRepository.recordHabitCompleted(habitId = 1L, xpEarned = 10L, coinsEarned = 10)
        }

        val updated = upsertLastEntity!!
        assertEquals("Progress should be capped at target (1)", 1, updated.progressValue)
        assertTrue("Should be completed", updated.isCompleted)
    }

    @Test
    fun `progress — recordHabitCompleted on claimed challenge is no-op`() = runTest {
        setChallenge(habit1Entity.copy(isClaimed = true))

        challengeRepository.recordHabitCompleted(habitId = 1L, xpEarned = 10L, coinsEarned = 10)

        assertEquals("upsert should NOT be called for claimed challenge", 0, upsertCallCount)
    }

    @Test
    fun `progress — recordXpEarned on non-XP challenge does not change progress`() = runTest {
        setChallenge(habit3Entity) // HABIT_COMPLETION, not XP_EARNED

        challengeRepository.recordXpEarned(50L)

        // upsert is still called (to persist state), but progress value stays the same
        assertEquals("upsert should be called once", 1, upsertCallCount)
        val updated = upsertLastEntity!!
        assertEquals("Progress should remain 0 (type mismatch)", 0, updated.progressValue)
        assertFalse("Should not be completed", updated.isCompleted)
    }

    @Test
    fun `claim — completed challenge returns rewards and advances`() = runTest {
        setChallenge(habit1Entity.copy(isCompleted = true, progressValue = 1))

        val result = challengeRepository.claimActiveChallenge()

        assertNotNull("Result should not be null", result)
        assertEquals("habit_1", result.challenge.challengeId)
        assertEquals("Should have 2 rewards", 2, result.rewards.size)

        // Verify rewards match ChallengeConfig
        val definition = ChallengeConfig.definition("habit_1")!!
        assertEquals(definition.rewards, result.rewards)

        // Should have created next challenge via upsert
        assertEquals("upsert should be called for next challenge", 1, upsertCallCount)
    }

    @Test
    fun `claim — incomplete challenge returns empty rewards`() = runTest {
        setChallenge(habit3Entity) // progress 0, target 3, not completed

        val result = challengeRepository.claimActiveChallenge()

        assertNotNull("Result should not be null", result)
        assertTrue("Rewards should be empty for incomplete challenge", result.rewards.isEmpty())
        assertEquals("Should return the current challenge", "habit_3", result.challenge.challengeId)

        // Should NOT have created next challenge
        assertEquals("upsert should NOT be called for incomplete claim", 0, upsertCallCount)
    }

    @Test
    fun `claim — already claimed challenge returns empty rewards`() = runTest {
        setChallenge(
            habit1Entity.copy(
                isCompleted = true,
                isClaimed = true,
                progressValue = 1,
                claimedAt = System.currentTimeMillis()
            )
        )

        val result = challengeRepository.claimActiveChallenge()

        assertNotNull("Result should not be null", result)
        assertTrue("Rewards should be empty for claimed challenge", result.rewards.isEmpty())

        // Should NOT have created next challenge
        assertEquals("upsert should NOT be called for claimed challenge", 0, upsertCallCount)
    }

    @Test
    fun `claim — full lifecycle from progress to claim`() = runTest {
        // Start: habit_1 challenge at 0 progress, target 1
        setChallenge(habit1Entity)

        // Step 1: Record a habit completion
        challengeRepository.recordHabitCompleted(habitId = 1L, xpEarned = 10L, coinsEarned = 10)

        // Verify recordHabitCompleted triggered upsert
        assertEquals("upsert should be called once after record", 1, upsertCallCount)

        // Verify progress via the entity that was passed to upsert
        val afterProgress = upsertLastEntity!!
        assertEquals(1, afterProgress.progressValue)
        assertTrue("Should be completed after upsert", afterProgress.isCompleted)

        // Step 2: Claim
        val result = challengeRepository.claimActiveChallenge()
        assertTrue("Claim should succeed", result.rewards.isNotEmpty())
        assertEquals(2, result.rewards.size)

        // Step 3: Verify next challenge was created (upsert called again for next challenge)
        assertEquals("upsert should be called twice (progress + next)", 2, upsertCallCount)
        val nextChallenge = upsertLastEntity!!
        assertFalse("Next challenge should not be the same", nextChallenge.challengeId == "habit_1")
        assertEquals("Next challenge should start at 0 progress", 0, nextChallenge.progressValue)
    }

    @Test
    fun `progress — XP_EARNED with multiple records accumulates`() = runTest {
        setChallenge(xp25Entity)

        challengeRepository.recordXpEarned(10L)
        challengeRepository.recordXpEarned(10L)
        challengeRepository.recordXpEarned(5L)

        val updated = upsertLastEntity!!
        assertEquals("Progress should be 25 (10+10+5)", 25, updated.progressValue)
        assertTrue("Should be completed at 25/25", updated.isCompleted)
    }

    @Test
    fun `progress — XP_EARNED caps at target`() = runTest {
        setChallenge(xp25Entity)

        challengeRepository.recordXpEarned(30L)

        val updated = upsertLastEntity!!
        assertEquals("Progress should be capped at 25", 25, updated.progressValue)
        assertTrue("Should be completed", updated.isCompleted)
    }

    @Test
    fun `progress — COINS_EARNED caps at target`() = runTest {
        setChallenge(coins20Entity)

        challengeRepository.recordCoinsEarned(25)

        val updated = upsertLastEntity!!
        assertEquals("Progress should be capped at 20", 20, updated.progressValue)
        assertTrue("Should be completed", updated.isCompleted)
    }
}
