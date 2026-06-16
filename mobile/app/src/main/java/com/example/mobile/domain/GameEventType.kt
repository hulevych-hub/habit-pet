package com.example.mobile.domain

enum class GameEventType(val displayName: String) {
    HABIT_COMPLETED("Habit Completed"),
    ACHIEVEMENT_UNLOCKED("Achievement Unlocked"),
    LEVEL_UP("Level Up"),
    DRAGON_EVOLUTION("Dragon Evolution"),
    CHEST_OPENED("Chest Opened"),
    STREAK_MILESTONE("Streak Milestone"),
    CHALLENGE_COMPLETED("Challenge Complete"),
    FIRST_DAILY_LOGIN("Daily Welcome"),
    SURPRISE_REWARD("Surprise Reward"),
    COMBO_MILESTONE("Combo Milestone")
}
