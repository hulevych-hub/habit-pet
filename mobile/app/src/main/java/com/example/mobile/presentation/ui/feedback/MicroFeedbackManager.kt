package com.example.mobile.presentation.ui.feedback

import com.example.mobile.presentation.ui.events.MicroFeedbackEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MicroFeedbackManager @Inject constructor() {

    private val _events = MutableSharedFlow<MicroFeedbackEvent>(
        extraBufferCapacity = 8
    )

    val events: SharedFlow<MicroFeedbackEvent> = _events

    fun triggerHabitCompleted(
        xp: Long,
        coins: Int,
        combo: Int = 0,
        comboMultiplier: Float = 1f
    ) {
        _events.tryEmit(
            MicroFeedbackEvent.HabitCompleted(
                xp = xp,
                coins = coins,
                combo = combo,
                comboMultiplier = comboMultiplier
            )
        )
    }

    fun triggerXpGained(amount: Long) {
        if (amount <= 0L) return
        _events.tryEmit(MicroFeedbackEvent.XpGained(amount = amount))
    }

    fun triggerCoinGained(amount: Int) {
        if (amount <= 0) return
        _events.tryEmit(MicroFeedbackEvent.CoinGained(amount = amount))
    }

    fun triggerTabSwitched() {
        _events.tryEmit(MicroFeedbackEvent.TabSwitched)
    }
}
