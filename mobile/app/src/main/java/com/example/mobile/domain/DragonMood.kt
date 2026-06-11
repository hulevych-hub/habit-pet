package com.example.mobile.domain

enum class DragonMood(val value: String, val displayName: String) {
    HAPPY(value = "Happy", displayName = "Happy"),
    CALM(value = "Calm", displayName = "Calm"),
    EXCITED(value = "Excited", displayName = "Excited"),
    PROUD(value = "Proud", displayName = "Proud"),
    LONELY(value = "Lonely", displayName = "Lonely");

    companion object {
        fun from(value: String): DragonMood {
            return values().firstOrNull { it.value.equals(value, ignoreCase = true) } ?: CALM
        }

        fun calculate(
            currentStreak: Int,
            lastActivityTimestamp: Long,
            recentHabitCompletions: Int
        ): DragonMood {
            val now = System.currentTimeMillis()
            val hoursSinceLastActivity = if (lastActivityTimestamp > 0L) {
                ((now - lastActivityTimestamp).coerceAtLeast(0L) / ONE_HOUR_MILLIS)
            } else {
                0L
            }

            return when {
                lastActivityTimestamp > 0L && hoursSinceLastActivity >= LONELY_AFTER_HOURS -> LONELY
                currentStreak >= PROUD_STREAK_THRESHOLD -> PROUD
                recentHabitCompletions >= EXCITED_COMPLETION_THRESHOLD -> EXCITED
                currentStreak > 0 -> HAPPY
                else -> CALM
            }
        }

        private const val ONE_HOUR_MILLIS = 60L * 60L * 1000L
        private const val LONELY_AFTER_HOURS = 36L
        private const val PROUD_STREAK_THRESHOLD = 7
        private const val EXCITED_COMPLETION_THRESHOLD = 3
    }
}
