package com.example.mobile.presentation.ui.animations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Centralized animation system for Habit Pet.
 * Provides consistent timing, easing, and transition patterns across all screens.
 */
object HabitPetAnimations {

    // ── Duration Constants ─────────────────────────────────────────
    object Duration {
        const val INSTANT = 100
        const val FAST = 150
        const val QUICK = 200
        const val NORMAL = 300
        const val SLOW = 450
        const val EMPHASIS = 600
        const val REWARD = 900
    }

    // ── Easing Constants ───────────────────────────────────────────
    object Easing {
        val STANDARD = FastOutSlowInEasing
        val ACCELERATE = androidx.compose.animation.core.FastOutLinearInEasing
        val DECELERATE = androidx.compose.animation.core.LinearOutSlowInEasing

        fun spring(
            stiffness: Float = androidx.compose.animation.core.Spring.StiffnessMediumLow,
            dampingRatio: Float = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
        ): androidx.compose.animation.core.SpringSpec<Float> =
            androidx.compose.animation.core.spring(stiffness = stiffness, dampingRatio = dampingRatio)
    }

    // ── Navigation Transitions ─────────────────────────────────────

    /** Standard slide + fade transition for screen navigation. */
    val screenEnterTransition: EnterTransition
        get() = slideInHorizontally(
            animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD),
            initialOffsetX = { fullWidth -> fullWidth / 4 }
        ) + fadeIn(animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD))

    val screenExitTransition: ExitTransition
        get() = slideOutHorizontally(
            animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD),
            targetOffsetX = { fullWidth -> -fullWidth / 4 }
        ) + fadeOut(animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD))

    val screenPopEnterTransition: EnterTransition
        get() = slideInHorizontally(
            animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD),
            initialOffsetX = { fullWidth -> -fullWidth / 4 }
        ) + fadeIn(animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD))

    val screenPopExitTransition: ExitTransition
        get() = slideOutHorizontally(
            animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD),
            targetOffsetX = { fullWidth -> fullWidth / 4 }
        ) + fadeOut(animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD))

    // ── Bottom Sheet Transitions ───────────────────────────────────

    val bottomSheetEnterTransition: EnterTransition
        get() = slideInVertically(
            animationSpec = tween(Duration.SLOW, easing = Easing.STANDARD),
            initialOffsetY = { it }
        ) + fadeIn(animationSpec = tween(Duration.SLOW, easing = Easing.STANDARD))

    val bottomSheetExitTransition: ExitTransition
        get() = slideOutVertically(
            animationSpec = tween(Duration.QUICK, easing = Easing.ACCELERATE),
            targetOffsetY = { it }
        ) + fadeOut(animationSpec = tween(Duration.QUICK, easing = Easing.ACCELERATE))

    // ── Overlay Transitions ────────────────────────────────────────

    val overlayEnterTransition: EnterTransition
        get() = scaleIn(
            animationSpec = Easing.spring(),
            initialScale = 0.85f
        ) + fadeIn(animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD))

    val overlayExitTransition: ExitTransition
        get() = scaleOut(
            animationSpec = tween(Duration.QUICK, easing = Easing.ACCELERATE),
            targetScale = 0.9f
        ) + fadeOut(animationSpec = tween(Duration.QUICK, easing = Easing.ACCELERATE))

    // ── List Item Animations ───────────────────────────────────────

    /** Stagger delay for list items based on index. */
    fun staggerDelay(index: Int, baseDelay: Int = 50): Int = index * baseDelay

    /** Standard list item enter transition with stagger. */
    fun listItemEnterTransition(index: Int): EnterTransition {
        val delay = staggerDelay(index)
        return slideInVertically(
            animationSpec = tween(
                durationMillis = Duration.NORMAL,
                delayMillis = delay,
                easing = Easing.STANDARD
            ),
            initialOffsetY = { it / 4 }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = Duration.NORMAL,
                delayMillis = delay,
                easing = Easing.STANDARD
            )
        )
    }

    // ── State Content Transitions ─────────────────────────────────

    /** Standard transition between loading/content/error states. */
    val stateContentTransition: EnterTransition
        get() = fadeIn(animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD)) +
            scaleIn(
                animationSpec = tween(Duration.NORMAL, easing = Easing.STANDARD),
                initialScale = 0.98f
            )

    val stateExitTransition: ExitTransition
        get() = fadeOut(animationSpec = tween(Duration.FAST, easing = Easing.STANDARD)) +
            scaleOut(
                animationSpec = tween(Duration.FAST, easing = Easing.STANDARD),
                targetScale = 0.98f
            )

    // ── Reward Transitions ─────────────────────────────────────────

    val rewardEnterTransition: EnterTransition
        get() = scaleIn(
            animationSpec = spring(
                stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
            ),
            initialScale = 0.7f
        ) + fadeIn(animationSpec = tween(Duration.REWARD, easing = Easing.STANDARD))

    val rewardExitTransition: ExitTransition
        get() = scaleOut(
            animationSpec = tween(Duration.SLOW, easing = Easing.ACCELERATE),
            targetScale = 1.1f
        ) + fadeOut(animationSpec = tween(Duration.SLOW, easing = Easing.ACCELERATE))
}

// ── Modifier Extensions ─────────────────────────────────────────────

/**
 * Adds a press scale animation to any composable.
 * Provides tactile feedback when tapping interactive elements.
 */
fun Modifier.pressableScale(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(
            durationMillis = HabitPetAnimations.Duration.FAST,
            easing = HabitPetAnimations.Easing.STANDARD
        ),
        label = "pressable scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { /* handled by caller */ }
        )
}

/**
 * Adds a subtle press alpha animation to any composable.
 */
fun Modifier.pressableAlpha(
    pressedAlpha: Float = 0.85f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) pressedAlpha else 1f,
        animationSpec = tween(
            durationMillis = HabitPetAnimations.Duration.FAST,
            easing = HabitPetAnimations.Easing.STANDARD
        ),
        label = "pressable alpha"
    )

    this
        .graphicsLayer { this.alpha = alpha }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { /* handled by caller */ }
        )
}

/**
 * Fades and slides content in from below when visible.
 * Useful for revealing content after loading state.
 */
fun Modifier.fadeSlideInVisible(
    visible: Boolean,
    delayMs: Int = 0
): Modifier = composed {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = HabitPetAnimations.Duration.NORMAL,
            delayMillis = delayMs,
            easing = HabitPetAnimations.Easing.STANDARD
        ),
        label = "fade slide in alpha"
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 12f,
        animationSpec = tween(
            durationMillis = HabitPetAnimations.Duration.NORMAL,
            delayMillis = delayMs,
            easing = HabitPetAnimations.Easing.STANDARD
        ),
        label = "fade slide in translation"
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }
}

/**
 * Staggered list item animation - fades and slides in from below with staggered delay.
 */
fun Modifier.staggeredListItem(
    index: Int,
    visible: Boolean = true
): Modifier = composed {
    val delayMs = HabitPetAnimations.staggerDelay(index)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = HabitPetAnimations.Duration.NORMAL,
            delayMillis = delayMs,
            easing = HabitPetAnimations.Easing.STANDARD
        ),
        label = "staggered list alpha"
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 16f,
        animationSpec = tween(
            durationMillis = HabitPetAnimations.Duration.NORMAL,
            delayMillis = delayMs,
            easing = HabitPetAnimations.Easing.STANDARD
        ),
        label = "staggered list translation"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.97f,
        animationSpec = tween(
            durationMillis = HabitPetAnimations.Duration.NORMAL,
            delayMillis = delayMs,
            easing = HabitPetAnimations.Easing.STANDARD
        ),
        label = "staggered list scale"
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Animated visibility wrapper that fades and scales content in/out.
 * Use this for switching between loading/content/error states.
 */
@Composable
fun <T> AnimatedStateContent(
    state: T?,
    modifier: Modifier = Modifier,
    enter: EnterTransition = HabitPetAnimations.overlayEnterTransition,
    exit: ExitTransition = HabitPetAnimations.overlayExitTransition,
    content: @Composable (T) -> Unit
) {
    AnimatedVisibility(
        visible = state != null,
        enter = enter,
        exit = exit,
        modifier = modifier
    ) {
        state?.let { content(it) }
    }
}
