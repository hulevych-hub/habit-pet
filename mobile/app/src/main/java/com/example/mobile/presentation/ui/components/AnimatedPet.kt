package com.example.mobile.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.mobile.R
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.presentation.ui.animations.PetAnimations
import com.example.mobile.util.PetTransitionPrefs

/**
 * Animated pet display using actual drawable assets with proper animations and equipped items
 */
@Composable
fun AnimatedPet(
    pet: PetEntity,
    modifier: Modifier = Modifier,
    showNameOverlay: Boolean = true
) {
    val evolutionStage = pet.evolutionStage.takeIf { it in 0..4 } ?: 0

    // Set background to transparent to prevent layer-blend bleeding on screens
    Box(
        modifier = modifier.background(Color.Transparent)
    ) {
        // Layered rendering: Aura -> Background -> Pet -> Outfit

        AuraLayer(
            equippedAura = pet.equippedAura,
            modifier = Modifier.size(220.dp).align(Alignment.Center)
        )

        // Background (if equipped)
        if (pet.equippedBackground != null) {
            Image(
                painter = painterResource(backgroundImageForItemId(pet.equippedBackground)),
                contentDescription = "Pet background",
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.Center)
            )
        }

        PetPhaseTransition(
            pet = pet,
            fromStage = evolutionStage - 1,
            toStage = evolutionStage,
            modifier = Modifier.align(Alignment.Center)
        )

        if (showNameOverlay) {
            // Pet name and level overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                    )
            ) {
                Text(
                    text = "${pet.name}\nLv.${pet.level}",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun PetPhaseTransition(
    pet: PetEntity,
    fromStage: Int,
    toStage: Int,
    modifier: Modifier = Modifier,
    size: IntSize = IntSize(180, 180),
    onTransitionCompleted: (() -> Unit)? = null
) {
    val normalizedToStage = toStage.takeIf { it in 0..4 } ?: 0
    val normalizedFromStage = fromStage.takeIf { it in 0..3 && it == normalizedToStage - 1 }
        ?: (normalizedToStage - 1).takeIf { it in 0..3 }
        ?: normalizedToStage
    val context = LocalContext.current
    val density = LocalDensity.current
    val transition = rememberInfiniteTransition(label = "pet idle")

    val hasPlayedTransition = normalizedToStage > 0 &&
            PetTransitionPrefs.hasPlayedTransition(context, normalizedToStage - 1, normalizedToStage)
    val shouldTransition = normalizedToStage > 0 && !hasPlayedTransition

    var transitionFinished by remember { mutableStateOf(false) }

    LaunchedEffect(normalizedToStage, hasPlayedTransition) {
        transitionFinished = false
    }

    val transitionProgress = animateFloatAsState(
        targetValue = if (shouldTransition || transitionFinished) 1f else 0f,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "pet phase transition progress"
    )

    val transitionScale = animateFloatAsState(
        targetValue = if (shouldTransition) 1.06f else 1f,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "pet phase transition scale"
    )

    val transitionShift = animateFloatAsState(
        targetValue = if (shouldTransition) 10f else 0f,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "pet phase transition shift"
    )

    LaunchedEffect(transitionProgress.value) {
        if (shouldTransition && !transitionFinished && transitionProgress.value >= 0.995f) {
            PetTransitionPrefs.markTransitionPlayed(
                context,
                normalizedToStage - 1,
                normalizedToStage
            )
            transitionFinished = true
            onTransitionCompleted?.invoke()
        }
    }

    val breathingScale = transition.animateFloat(
        initialValue = 1f - petIdleScaleAmplitude(normalizedToStage, pet.mood),
        targetValue = 1f + petIdleScaleAmplitude(normalizedToStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = petIdleBreathingDuration(normalizedToStage, pet.mood),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet breathing scale"
    )

    val breathingY = transition.animateFloat(
        initialValue = -petIdleVerticalAmplitude(normalizedToStage, pet.mood),
        targetValue = petIdleVerticalAmplitude(normalizedToStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = petIdleBreathingDuration(normalizedToStage, pet.mood),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet breathing translation"
    )

    val rotation = transition.animateFloat(
        initialValue = -petIdleRotationAmplitude(normalizedToStage, pet.mood),
        targetValue = petIdleRotationAmplitude(normalizedToStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = petIdleSwayDuration(normalizedToStage, pet.mood),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet idle sway"
    )

    val softBounce = transition.animateFloat(
        initialValue = -petIdleBounceAmplitude(normalizedToStage, pet.mood),
        targetValue = petIdleBounceAmplitude(normalizedToStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = petIdleBounceDuration(normalizedToStage, pet.mood),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet soft bounce"
    )

    val showTransition = shouldTransition || transitionFinished
    val transitionFromAlpha = 1f - transitionProgress.value
    val transitionToAlpha = transitionProgress.value
    val transitionScaleValue = transitionScale.value
    val transitionShiftPx = transitionShift.value * density.density
    val idleScale = breathingScale.value
    val idleRotation = rotation.value
    val idleTranslationY = (breathingY.value + softBounce.value) * density.density

    val transitionFromModifier = modifier
        .size(size.width.dp, size.height.dp)
        .graphicsLayer(
            alpha = transitionFromAlpha,
            scaleX = idleScale * transitionScaleValue,
            scaleY = idleScale * transitionScaleValue,
            rotationZ = idleRotation,
            translationY = idleTranslationY - transitionShiftPx
        )

    val transitionToModifier = modifier
        .size(size.width.dp, size.height.dp)
        .graphicsLayer(
            alpha = transitionToAlpha,
            scaleX = idleScale * transitionScaleValue,
            scaleY = idleScale * transitionScaleValue,
            rotationZ = idleRotation,
            translationY = idleTranslationY + transitionShiftPx
        )

    val finalModifier = modifier
        .size(size.width.dp, size.height.dp)
        .graphicsLayer(
            scaleX = idleScale,
            scaleY = idleScale,
            rotationZ = idleRotation,
            translationY = idleTranslationY
        )

    if (showTransition) {
        PetImageLayer(
            pet = pet,
            evolutionStage = normalizedFromStage,
            modifier = transitionFromModifier
        )
        PetImageLayer(
            pet = pet,
            evolutionStage = normalizedToStage,
            modifier = transitionToModifier
        )
    } else {
        PetImageLayer(
            pet = pet,
            evolutionStage = normalizedToStage,
            modifier = finalModifier
        )
    }
}

@Composable
private fun PetImageLayer(
    pet: PetEntity,
    evolutionStage: Int,
    modifier: Modifier = Modifier
) {
    // FIXED: Removed the dynamic mood intensity alpha modifier to force crisp 100% solid rendering
    Image(
        painter = painterResource(petImageForStage(evolutionStage)),
        contentDescription = "Pet image",
        modifier = modifier
    )

    OutfitLayer(
        equippedOutfit = pet.equippedOutfit,
        modifier = modifier
    )
}

@Composable
private fun AuraLayer(
    equippedAura: String?,
    modifier: Modifier = Modifier
) {
    if (equippedAura == null) return

    Image(
        painter = painterResource(auraImageForItemId(equippedAura)),
        contentDescription = "Pet aura",
        modifier = modifier.graphicsLayer(alpha = 0.38f)
    )
}

@Composable
private fun OutfitLayer(
    equippedOutfit: String?,
    modifier: Modifier = Modifier
) {
    if (equippedOutfit == null) return

    Image(
        painter = painterResource(outfitImageForItemId(equippedOutfit)),
        contentDescription = "Pet outfit",
        modifier = modifier.graphicsLayer(alpha = 0.62f)
    )
}

private fun backgroundImageForItemId(itemId: String?): Int = when (itemId) {
    "background_forest", "sunny_meadow" -> R.drawable.background_forest
    "background_beach", "crystal_cave" -> R.drawable.background_beach
    "background_mountains", "floating_islands" -> R.drawable.background_mountains
    "background_night_sky", "celestial_realm" -> R.drawable.background_night_sky
    else -> R.drawable.background_forest
}

private fun outfitImageForItemId(itemId: String?): Int = when (itemId) {
    "royal_scarf", "crystal_crown", "mystic_cloak", "starlight_armor" -> R.drawable.outfit_placeholder
    else -> R.drawable.outfit_placeholder
}

private fun auraImageForItemId(itemId: String?): Int = when (itemId) {
    "soft_glow", "crystal_aura", "dragonfire_aura", "celestial_aura" -> R.drawable.aura_placeholder
    else -> R.drawable.aura_placeholder
}

private fun petImageForStage(stage: Int): Int {
    return when (stage) {
        0 -> R.drawable.egg
        1 -> R.drawable.hatchling
        2 -> R.drawable.young_dragon
        3 -> R.drawable.adult_dragon
        4 -> R.drawable.ancient_dragon
        else -> R.drawable.egg
    }
}

private fun petIdleScaleAmplitude(stage: Int, mood: String): Float {
    val base = when (stage) {
        0 -> 0.02f
        1 -> 0.024f
        2 -> 0.026f
        3 -> 0.023f
        4 -> 0.02f
        else -> 0.02f
    }
    return moodAdjusted(base, mood)
}

private fun petIdleVerticalAmplitude(stage: Int, mood: String): Float {
    val base = when (stage) {
        0 -> 1.0f
        1 -> 2.0f
        2 -> 2.6f
        3 -> 2.3f
        4 -> 2.0f
        else -> 1.0f
    }
    return moodAdjusted(base, mood)
}

private fun petIdleRotationAmplitude(stage: Int, mood: String): Float {
    val base = when (stage) {
        0 -> 1.4f
        1 -> 1.0f
        2 -> 0.8f
        3 -> 0.65f
        4 -> 0.5f
        else -> 1.0f
    }
    return moodAdjusted(base, mood)
}

private fun petIdleBounceAmplitude(stage: Int, mood: String): Float {
    val base = when (stage) {
        0 -> 1.2f
        1 -> 0.7f
        2 -> 0.5f
        3 -> 0.35f
        4 -> 0.25f
        else -> 0.5f
    }
    return moodAdjusted(base, mood)
}

private fun petIdleBreathingDuration(stage: Int, mood: String): Int {
    val base = when (stage) {
        0 -> 2600
        1 -> 3000
        2 -> 3400
        3 -> 3800
        4 -> 4200
        else -> 3000
    }
    return (base * PetAnimations.moodDurationMultiplier(mood)).toInt().coerceAtLeast(1200)
}

private fun petIdleSwayDuration(stage: Int, mood: String): Int {
    val base = when (stage) {
        0 -> 4600
        1 -> 5600
        2 -> 6400
        3 -> 7200
        4 -> 8000
        else -> 5600
    }
    return (base * PetAnimations.moodDurationMultiplier(mood)).toInt().coerceAtLeast(1800)
}

private fun petIdleBounceDuration(stage: Int, mood: String): Int {
    val base = when (stage) {
        0 -> 3600
        1 -> 4800
        2 -> 5600
        3 -> 6400
        4 -> 7200
        else -> 4800
    }
    return (base * PetAnimations.moodDurationMultiplier(mood)).toInt().coerceAtLeast(1600)
}

private fun moodAdjusted(value: Float, mood: String): Float {
    return PetAnimations.applyMoodModifier(value, mood).coerceIn(value * 0.65f, value * 1.35f)
}