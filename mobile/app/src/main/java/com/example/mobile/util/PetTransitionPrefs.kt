package com.example.mobile.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Lightweight persistence for one-time pet evolution transition animations.
 */
object PetTransitionPrefs {

    private const val PREFS_NAME = "habit_pet_phase_transition_prefs"
    private const val KEY_PREFIX = "played_transition_"

    fun hasPlayedTransition(context: Context, fromStage: Int, toStage: Int): Boolean {
        if (!isValidTransition(fromStage, toStage)) return true
        return prefs(context).getBoolean(keyFor(fromStage, toStage), false)
    }

    fun markTransitionPlayed(context: Context, fromStage: Int, toStage: Int) {
        if (!isValidTransition(fromStage, toStage)) return

        prefs(context)
            .edit()
            .putBoolean(keyFor(fromStage, toStage), true)
            .apply()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun keyFor(fromStage: Int, toStage: Int): String {
        return "$KEY_PREFIX${fromStage}_to_${toStage}"
    }

    private fun isValidTransition(fromStage: Int, toStage: Int): Boolean {
        return fromStage in 0..3 && toStage == fromStage + 1
    }
}
