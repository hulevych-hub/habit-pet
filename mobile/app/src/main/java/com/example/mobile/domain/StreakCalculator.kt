package com.example.mobile.domain

import java.util.Calendar

object StreakCalculator {
    suspend fun calculateConsecutiveStreak(
        fromDayStart: Long,
        isCompleteDay: suspend (Long) -> Boolean
    ): Int {
        if (!isCompleteDay(fromDayStart)) return 0

        var streak = 0
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fromDayStart
        normalize(calendar)

        while (isCompleteDay(calendar.timeInMillis)) {
            streak++
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            normalize(calendar)
        }

        return streak
    }

    private fun normalize(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
}
