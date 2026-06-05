package com.example.mobile.presentation.ui.animations

/**
 * Utility class for pet animations
 */
object PetAnimations {

    /**
     * Applies a mood modifier to animation speed/intensity
     */
    fun applyMoodModifier(baseValue: Float, mood: String): Float {
        return when (mood) {
            "Happy" -> baseValue * 1.3f
            "Sad" -> baseValue * 0.7f
            else -> baseValue // Neutral
        }
    }
}