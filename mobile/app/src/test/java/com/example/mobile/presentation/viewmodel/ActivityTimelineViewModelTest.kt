package com.example.mobile.presentation.viewmodel

import com.example.mobile.data.local.entities.GameEventEntity
import com.example.mobile.domain.repository.GameEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ActivityTimelineViewModelTest {

    private lateinit var gameEventRepository: GameEventRepository
    private lateinit var viewModel: ActivityTimelineViewModel
    private val testDispatcher = StandardTestDispatcher()

    private fun event(id: Long, timestamp: Long = id * 1000L) = GameEventEntity(
        id = id,
        type = "HABIT_COMPLETED",
        title = "Event $id",
        description = "Description $id",
        timestamp = timestamp,
        icon = "check",
        rarity = "COMMON",
        payload = null
    )

    private fun events(count: Long, startId: Long = 1L): List<GameEventEntity> =
        (startId until startId + count).map { event(it) }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gameEventRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init — loads events on creation`() = runTest {
        val firstPage = events(100)
        whenever(gameEventRepository.getRecentEvents(any(), any()))
            .thenReturn(flowOf(firstPage))

        viewModel = ActivityTimelineViewModel(gameEventRepository)
        advanceUntilIdle()

        verify(gameEventRepository).getRecentEvents(100, 0)
        assertEquals(100, viewModel.events.value.size)
    }

    @Test
    fun `init — events sorted by timestamp descending`() = runTest {
        val unsorted = listOf(
            event(3, timestamp = 1000L),
            event(4, timestamp = 3000L),
            event(5, timestamp = 2000L)
        )
        whenever(gameEventRepository.getRecentEvents(any(), any()))
            .thenReturn(flowOf(unsorted))

        viewModel = ActivityTimelineViewModel(gameEventRepository)
        advanceUntilIdle()

        val result = viewModel.events.value
        assertTrue("Events should be sorted by timestamp descending", result[0].timestamp >= result[1].timestamp)
        assertTrue("Events should be sorted by timestamp descending", result[1].timestamp >= result[2].timestamp)
    }

    @Test
    fun `init — empty events list`() = runTest {
        whenever(gameEventRepository.getRecentEvents(any(), any()))
            .thenReturn(flowOf(emptyList()))

        viewModel = ActivityTimelineViewModel(gameEventRepository)
        advanceUntilIdle()

        assertEquals(0, viewModel.events.value.size)
        assertFalse("No more when empty", viewModel.hasMore.value)
    }

    @Test
    fun `loadMore — appends new events`() = runTest {
        val firstPage = events(100)
        val secondPage = events(50, startId = 101)
        whenever(gameEventRepository.getRecentEvents(100, 0))
            .thenReturn(flowOf(firstPage))
        whenever(gameEventRepository.getRecentEvents(100, 100))
            .thenReturn(flowOf(secondPage))

        viewModel = ActivityTimelineViewModel(gameEventRepository)
        advanceUntilIdle()
        assertEquals(100, viewModel.events.value.size)

        viewModel.loadMore()
        advanceUntilIdle()

        verify(gameEventRepository).getRecentEvents(100, 100)
        assertEquals(150, viewModel.events.value.size)
    }

    @Test
    fun `loadMore — deduplicates events`() = runTest {
        val firstPage = events(100)
        // Second page has overlap: events 99-102 (id 99 and 100 overlap with first page)
        val secondPage = (99L..102L).map { event(it) }
        whenever(gameEventRepository.getRecentEvents(100, 0))
            .thenReturn(flowOf(firstPage))
        whenever(gameEventRepository.getRecentEvents(100, 100))
            .thenReturn(flowOf(secondPage))

        viewModel = ActivityTimelineViewModel(gameEventRepository)
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        // 100 from first page + 2 new (id 101, 102) = 102
        assertEquals(102, viewModel.events.value.size)
    }

    @Test
    fun `loadMore — sets hasMore to false when fewer than limit returned`() = runTest {
        val singleEvent = listOf(event(1))
        whenever(gameEventRepository.getRecentEvents(100, 0))
            .thenReturn(flowOf(singleEvent))

        viewModel = ActivityTimelineViewModel(gameEventRepository)
        advanceUntilIdle()

        assertFalse("hasMore should be false when page < limit", viewModel.hasMore.value)
    }

    @Test
    fun `loadMore — handles error gracefully`() = runTest {
        val firstPage = events(100)
        whenever(gameEventRepository.getRecentEvents(100, 0))
            .thenReturn(flowOf(firstPage))
        whenever(gameEventRepository.getRecentEvents(100, 100))
            .thenThrow(RuntimeException("Database error"))

        viewModel = ActivityTimelineViewModel(gameEventRepository)
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertNotNull("Error should be set", viewModel.error.value)
        assertTrue("Error message should contain details", viewModel.error.value!!.contains("Database error"))
    }
}
