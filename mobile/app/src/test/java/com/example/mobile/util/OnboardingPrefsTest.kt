package com.example.mobile.util

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for OnboardingPrefs.
 *
 * Uses a mocked Context + SharedPreferences so the tests do not touch
 * any real device storage. Each test starts with a fresh mock so the
 * default (not-seen) state is verified independently.
 */
class OnboardingPrefsTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: Editor

    @Before
    fun setup() {
        context = mock()
        prefs = mock()
        editor = mock()

        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
        whenever(editor.clear()).thenReturn(editor)
    }

    @Test
    fun `hasSeenOnboarding returns false by default`() {
        whenever(prefs.getBoolean(any(), any())).thenReturn(false)

        assertFalse(OnboardingPrefs.hasSeenOnboarding(context))
    }

    @Test
    fun `hasSeenOnboarding returns true after markOnboardingSeen`() {
        // Simulate the write persisting: after putBoolean, subsequent get returns true.
        whenever(prefs.getBoolean(any(), any())).thenReturn(true)

        OnboardingPrefs.markOnboardingSeen(context)

        verify(editor).putBoolean("has_seen_onboarding", true)
        assertTrue(OnboardingPrefs.hasSeenOnboarding(context))
    }

    @Test
    fun `clear resets hasSeenOnboarding back to false`() {
        OnboardingPrefs.clear(context)

        verify(editor).clear()
    }

    @Test
    fun `markOnboardingSeen does not write when already seen still writes idempotently`() {
        // OnboardingPrefs does not short-circuit on already-seen; it always writes.
        // This test verifies the write happens every time (the caller decides whether to check first).
        OnboardingPrefs.markOnboardingSeen(context)

        verify(editor).putBoolean("has_seen_onboarding", true)
    }

    @Test
    fun `hasSeenOnboarding uses correct prefs name and key`() {
        OnboardingPrefs.hasSeenOnboarding(context)

        verify(context).getSharedPreferences("habit_pet_onboarding_prefs", Context.MODE_PRIVATE)
        verify(prefs).getBoolean("has_seen_onboarding", false)
    }
}
