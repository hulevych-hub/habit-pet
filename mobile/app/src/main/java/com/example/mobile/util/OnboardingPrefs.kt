package com.example.mobile.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Lightweight persistence for the onboarding / first-launch experience.
 */
object OnboardingPrefs {

    private const val PREFS_NAME = "habit_pet_onboarding_prefs"
    private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"

    fun hasSeenOnboarding(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
    }

    fun markOnboardingSeen(context: Context) {
        prefs(context)
            .edit()
            .putBoolean(KEY_HAS_SEEN_ONBOARDING, true)
            .apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
