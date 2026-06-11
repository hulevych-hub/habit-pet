package com.example.mobile.presentation.ui.feedback

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobile.presentation.ui.events.MicroFeedbackEvent
import kotlinx.coroutines.delay

@Composable
fun MicroFeedbackOverlay(
    manager: MicroFeedbackManager
) {
    var currentEvent by remember { mutableStateOf<MicroFeedbackEvent?>(null) }
    var xpPulseProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(manager) {
        manager.events.collect { event ->
            currentEvent = event
            if (event is MicroFeedbackEvent.XpGained) {
                xpPulseProgress = 1f
                delay(420)
                xpPulseProgress = 0f
            }

            delay(950)
            currentEvent = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (xpPulseProgress > 0f) {
                MicroFeedbackXpPulse(progress = xpPulseProgress)
            }

            currentEvent?.let { event ->
                MicroFeedbackPulse(event = event)
            }
        }
    }
}

@Composable
private fun MicroFeedbackPulse(event: MicroFeedbackEvent) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.86f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "micro feedback scale"
    )
    val glow by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "micro feedback glow"
    )

    LaunchedEffect(event) {
        visible = true
        delay(900)
        visible = false
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .background(
                color = colorForEvent(event).copy(alpha = 0.14f + (0.08f * glow)),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = labelForEvent(event),
            style = MaterialTheme.typography.labelLarge,
            color = colorForEvent(event),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MicroFeedbackXpPulse(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 420,
            easing = FastOutSlowInEasing
        ),
        label = "micro feedback xp pulse"
    )

    LinearProgressIndicator(
        progress = { animatedProgress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp),
        color = MaterialTheme.colorScheme.tertiary,
        trackColor = MaterialTheme.colorScheme.tertiaryContainer
    )

    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
private fun labelForEvent(event: MicroFeedbackEvent): String = when (event) {
    is MicroFeedbackEvent.HabitCompleted -> "Habit complete +${event.xp} XP +${event.coins} coins"
    is MicroFeedbackEvent.XpGained -> "+${event.amount} EXP"
    is MicroFeedbackEvent.CoinGained -> "+${event.amount} coins"
    MicroFeedbackEvent.TabSwitched -> "Nice choice"
}

@Composable
private fun colorForEvent(event: MicroFeedbackEvent): Color = when (event) {
    is MicroFeedbackEvent.HabitCompleted -> MaterialTheme.colorScheme.primary
    is MicroFeedbackEvent.XpGained -> Color(0xFF10B981)
    is MicroFeedbackEvent.CoinGained -> Color(0xFFF59E0B)
    MicroFeedbackEvent.TabSwitched -> MaterialTheme.colorScheme.secondary
}
