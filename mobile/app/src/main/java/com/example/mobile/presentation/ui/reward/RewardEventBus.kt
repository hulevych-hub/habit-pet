package com.example.mobile.presentation.ui.reward

import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton event bus for reward events.
 */
@Singleton
class RewardEventBus @Inject constructor() {

    private val _rewardEvents = MutableSharedFlow<RewardUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )

    val rewardEvents: SharedFlow<RewardUiEvent> = _rewardEvents

    /**
     * Emit a reward event.
     */
    fun emit(event: RewardUiEvent) {
        _rewardEvents.tryEmit(event)
    }
}