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
