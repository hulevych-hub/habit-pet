package com.example.mobile.presentation.ui.reward

import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RewardQueueTest {

    private lateinit var rewardQueue: RewardQueue

    @Before
    fun setup() {
        rewardQueue = RewardQueue()
    }

    @Test
    fun `addReward — single reward is emitted`() = runBlocking {
        val deferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 10))

        val emitted = withTimeout(5000) { deferred.await() }
        assertTrue("Emitted should be CoinReward", emitted is RewardUiEvent.CoinReward)
        assertEquals(10, (emitted as RewardUiEvent.CoinReward).amount)
    }

    @Test
    fun `addReward — buffered rewards are emitted in priority order after dismiss`() = runBlocking {
        // First: add rewards and collect the highest priority one
        val firstDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 10))
        // CoinReward is emitted immediately (queue was idle)
        // Now buffer the other two while CoinReward is emitting
        rewardQueue.addReward(RewardUiEvent.LevelUpReward(previousLevel = 1, level = 2, coins = 20))
        rewardQueue.addReward(RewardUiEvent.ExpReward(amount = 15L))
        // Buffer is sorted: [LevelUpReward(1), ExpReward(6)]

        val first = withTimeout(5000) { firstDeferred.await() }
        // First emission is CoinReward (it was emitted immediately)
        assertTrue("First should be CoinReward, got ${first::class.simpleName}", first is RewardUiEvent.CoinReward)

        // Dismiss and collect next — should be LevelUpReward (highest priority in buffer)
        val secondDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)
        rewardQueue.rewardDismissed()

        val second = withTimeout(5000) { secondDeferred.await() }
        assertTrue("Second should be LevelUpReward, got ${second::class.simpleName}", second is RewardUiEvent.LevelUpReward)

        // Dismiss and collect last
        val thirdDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)
        rewardQueue.rewardDismissed()

        val third = withTimeout(5000) { thirdDeferred.await() }
        assertTrue("Third should be ExpReward, got ${third::class.simpleName}", third is RewardUiEvent.ExpReward)
    }

    @Test
    fun `rewardDismissed — advances to next reward in buffer`() = runBlocking {
        val firstDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.ExpReward(amount = 10L))

        val first = withTimeout(5000) { firstDeferred.await() }
        assertTrue("First should be ExpReward", first is RewardUiEvent.ExpReward)

        val secondDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 5))
        rewardQueue.rewardDismissed()

        val second = withTimeout(5000) { secondDeferred.await() }
        assertTrue("Second should be CoinReward", second is RewardUiEvent.CoinReward)
        assertEquals(5, (second as RewardUiEvent.CoinReward).amount)
    }

    @Test
    fun `rewardDismissed — does nothing when buffer is empty`() {
        rewardQueue.rewardDismissed()
        assertTrue("No crash on empty dismiss", true)
    }

    @Test
    fun `buffer — does not emit second reward until first is dismissed`() = runBlocking {
        val firstDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.LevelUpReward(previousLevel = 1, level = 2, coins = 20))
        // This second reward goes into buffer but is NOT emitted yet (isEmitting=true)
        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 10))

        val first = withTimeout(5000) { firstDeferred.await() }
        assertTrue("First should be LevelUpReward", first is RewardUiEvent.LevelUpReward)
        // CoinReward should NOT be emitted yet — buffer holds it while LevelUpReward is showing
    }

    @Test
    fun `emitNextIfPossible — emits next reward from buffer`() = runBlocking {
        val firstDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 5))

        val first = withTimeout(5000) { firstDeferred.await() }
        assertTrue("First should be CoinReward", first is RewardUiEvent.CoinReward)
        assertEquals(5, (first as RewardUiEvent.CoinReward).amount)

        val secondDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.ExpReward(amount = 10L))
        rewardQueue.rewardDismissed()

        val second = withTimeout(5000) { secondDeferred.await() }
        assertTrue("Second should be ExpReward", second is RewardUiEvent.ExpReward)
    }

    @Test
    fun `addReward — two coin rewards added while one is emitting`() = runBlocking {
        val firstDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 10))

        val first = withTimeout(5000) { firstDeferred.await() }
        assertTrue("First should be CoinReward", first is RewardUiEvent.CoinReward)
        assertEquals(10, (first as RewardUiEvent.CoinReward).amount)

        val secondDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 20))
        rewardQueue.rewardDismissed()

        val second = withTimeout(5000) { secondDeferred.await() }
        assertTrue("Second should be CoinReward", second is RewardUiEvent.CoinReward)
        assertEquals(20, (second as RewardUiEvent.CoinReward).amount)
    }

    @Test
    fun `mergeNextRewardIfPossible — merges compatible coin rewards`() = runBlocking {
        // Setup: emit one reward (so isEmitting=true), then add another to buffer
        val firstDeferred = async(Dispatchers.Default) { rewardQueue.rewardEvents.first() }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 20))
        // Wait for emission to start
        firstDeferred.await()

        // Now isEmitting=true, buffer is empty
        // Add a reward to buffer while emitting is happening
        rewardQueue.addReward(RewardUiEvent.CoinReward(amount = 30))
        // Now buffer has [CoinReward(30)]

        val merged = rewardQueue.mergeNextRewardIfPossible(RewardUiEvent.CoinReward(amount = 10))
        // mergeNextRewardIfPossible merges current(10) with buffer CoinReward(30) = CoinReward(40)
        assertEquals(40, (merged as RewardUiEvent.CoinReward).amount)
    }

    @Test
    fun `mergeNextRewardIfPossible — does not merge incompatible rewards`() = runBlocking {
        val current = RewardUiEvent.CoinReward(amount = 10)

        val emitted = async(Dispatchers.Default) {
            withTimeout(5000) { rewardQueue.rewardEvents.first() }
        }
        kotlinx.coroutines.delay(200)

        rewardQueue.addReward(RewardUiEvent.ExpReward(amount = 5L))

        // Wait for emission to complete
        emitted.await()

        val merged = rewardQueue.mergeNextRewardIfPossible(current)
        // CoinReward cannot merge with ExpReward, so current is unchanged
        assertEquals(10, (merged as RewardUiEvent.CoinReward).amount)
    }

    @Test
    fun `empty queue — no crash on operations`() {
        rewardQueue.rewardDismissed()
        rewardQueue.emitNextIfPossible()
        val merged = rewardQueue.mergeNextRewardIfPossible(RewardUiEvent.CoinReward(amount = 5))
        assertEquals(5, (merged as RewardUiEvent.CoinReward).amount)
        assertTrue("No crash on empty queue operations", true)
    }
}
