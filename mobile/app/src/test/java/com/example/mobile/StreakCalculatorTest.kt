package com.example.mobile

import com.example.mobile.domain.StreakCalculator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class StreakCalculatorTest {
    @Test
    fun currentDayIncomplete_returnsZero() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { false }

        assertEquals(0, streak)
    }

    @Test
    fun consecutiveCompleteDays_returnsFullStreak() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)
        val completedDays = setOf(
            dayStart(2026, Calendar.JUNE, 4),
            dayStart(2026, Calendar.JUNE, 3),
            dayStart(2026, Calendar.JUNE, 2)
        )

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        assertEquals(3, streak)
    }

    @Test
    fun missedPreviousDay_stopsStreakAtCurrentDay() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)
        val completedDays = setOf(
            dayStart(2026, Calendar.JUNE, 4),
            dayStart(2026, Calendar.JUNE, 2)
        )

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        assertEquals(1, streak)
    }

    @Test
    fun `empty completed days — returns 0`() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)
        val completedDays = emptySet<Long>()

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        assertEquals(0, streak)
    }

    @Test
    fun `single day complete — returns 1`() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)
        val completedDays = setOf(dayStart(2026, Calendar.JUNE, 4))

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        assertEquals(1, streak)
    }

    @Test
    fun `gap in middle — stops at gap`() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 6)
        val completedDays = setOf(
            dayStart(2026, Calendar.JUNE, 6),
            dayStart(2026, Calendar.JUNE, 5),
            // June 4 is missing
            dayStart(2026, Calendar.JUNE, 3),
            dayStart(2026, Calendar.JUNE, 2)
        )

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        assertEquals(2, streak)
    }

    @Test
    fun `very long streak — 365 days`() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)
        val completedDays = (0..364).map { offset ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = today
            cal.add(Calendar.DAY_OF_MONTH, -offset)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet()

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        assertEquals(365, streak)
    }

    @Test
    fun `streak with only yesterday complete — today incomplete returns 0`() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)
        val completedDays = setOf(dayStart(2026, Calendar.JUNE, 3))

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        // Today is not complete, so streak = 0
        assertEquals(0, streak)
    }

    @Test
    fun `streak with today and yesterday complete — returns 2`() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)
        val completedDays = setOf(
            dayStart(2026, Calendar.JUNE, 4),
            dayStart(2026, Calendar.JUNE, 3)
        )

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        assertEquals(2, streak)
    }

    @Test
    fun `streak with only day before yesterday — today incomplete returns 0`() = runBlocking {
        val today = dayStart(2026, Calendar.JUNE, 4)
        val completedDays = setOf(dayStart(2026, Calendar.JUNE, 2))

        val streak = StreakCalculator.calculateConsecutiveStreak(today) { day ->
            day in completedDays
        }

        // Today is not complete, so streak = 0
        assertEquals(0, streak)
    }

    private fun dayStart(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
