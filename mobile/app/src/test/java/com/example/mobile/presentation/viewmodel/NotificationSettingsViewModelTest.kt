package com.example.mobile.presentation.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationSettingsViewModelTest {

    private lateinit var viewModel: NotificationSettingsViewModel

    @Before
    fun setup() {
        viewModel = NotificationSettingsViewModel()
    }

    @Test
    fun `init — daily reminder enabled by default`() {
        assertTrue("Daily reminder should be enabled by default", viewModel.dailyReminderEnabled.value)
    }

    @Test
    fun `init — streak reminder enabled by default`() {
        assertTrue("Streak reminder should be enabled by default", viewModel.streakReminderEnabled.value)
    }

    @Test
    fun `init — pet reminder enabled by default`() {
        assertTrue("Pet reminder should be enabled by default", viewModel.petReminderEnabled.value)
    }

    @Test
    fun `setDailyReminderEnabled — toggles state`() {
        viewModel.setDailyReminderEnabled(false)
        assertFalse("Daily reminder should be disabled", viewModel.dailyReminderEnabled.value)

        viewModel.setDailyReminderEnabled(true)
        assertTrue("Daily reminder should be enabled", viewModel.dailyReminderEnabled.value)
    }

    @Test
    fun `setStreakReminderEnabled — toggles state`() {
        viewModel.setStreakReminderEnabled(false)
        assertFalse("Streak reminder should be disabled", viewModel.streakReminderEnabled.value)

        viewModel.setStreakReminderEnabled(true)
        assertTrue("Streak reminder should be enabled", viewModel.streakReminderEnabled.value)
    }

    @Test
    fun `setPetReminderEnabled — toggles state`() {
        viewModel.setPetReminderEnabled(false)
        assertFalse("Pet reminder should be disabled", viewModel.petReminderEnabled.value)

        viewModel.setPetReminderEnabled(true)
        assertTrue("Pet reminder should be enabled", viewModel.petReminderEnabled.value)
    }
}
