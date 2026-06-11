package com.example.mobile.presentation.ui.animations

import com.example.mobile.domain.DragonMood

/**
 * Utility class for pet animations
 */
object PetAnimations {

    /**
     * Applies a mood modifier to animation intensity.
     */
    fun applyMoodModifier(baseValue: Float, mood: String): Float {
        val moodValue = DragonMood.from(mood)
        val multiplier = when (moodValue) {
            DragonMood.HAPPY -> 1.25f
            DragonMood.CALM -> 0.9f
            DragonMood.EXCITED -> 1.35f
            DragonMood.PROUD -> 1.22f
            DragonMood.LONELY -> 0.68f
        }
        return (baseValue * multiplier).coerceIn(baseValue * 0.65f, baseValue * 1.35f)
    }

    fun moodDurationMultiplier(mood: String): Float {
        val moodValue = DragonMood.from(mood)
        return when (moodValue) {
            DragonMood.HAPPY -> 0.86f
            DragonMood.CALM -> 1.0f
            DragonMood.EXCITED -> 0.78f
            DragonMood.PROUD -> 0.9f
            DragonMood.LONELY -> 1.25f
        }
    }

    fun moodIntensity(mood: String): Float {
        val moodValue = DragonMood.from(mood)
        return when (moodValue) {
            DragonMood.HAPPY -> 0.98f
            DragonMood.CALM -> 0.94f
            DragonMood.EXCITED -> 1f
            DragonMood.PROUD -> 0.96f
            DragonMood.LONELY -> 0.86f
        }
    }
}