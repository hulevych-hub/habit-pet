package com.example.mobile.presentation.ui.events

sealed class MicroFeedbackEvent {
    data class HabitCompleted(
        val xp: Long,
        val coins: Int
    ) : MicroFeedbackEvent()

    data class XpGained(
        val amount: Long
    ) : MicroFeedbackEvent()

    data class CoinGained(
        val amount: Int
    ) : MicroFeedbackEvent()

    object TabSwitched : MicroFeedbackEvent()
}
