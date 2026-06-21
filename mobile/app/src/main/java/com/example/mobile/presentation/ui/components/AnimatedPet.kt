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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.presentation.ui.animations.PetAnimations
import com.example.mobile.util.AssetResolver
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.util.PetTransitionPrefs

private const val PET_SCALE_FRACTION = 0.86f

@Composable
fun AnimatedPet(
    pet: PetEntity,
    modifier: Modifier = Modifier,
    showNameOverlay: Boolean = true,
    backgroundContentScale: ContentScale = ContentScale.Crop
) {
    val evolutionStage = pet.evolutionStage.takeIf { it in 0..4 } ?: 0

    // The Box fills the size provided by the parent screen.
    Box(
        modifier = modifier
            .background(Color.Transparent)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        val assetManager = LocalContext.current.assets
        val backgroundAssetPath = remember(pet.equippedBackground) {
            AssetResolver.backgroundAssetPath(assetManager, pet.equippedBackground)
        }

        // BACKGROUND: Render first.
        val backgroundPainter = rememberAssetPainter(backgroundAssetPath, "pet background")
        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = "Pet background",
                modifier = Modifier.fillMaxSize(),
                contentScale = backgroundContentScale
            )
        }

        // PET: Keep the pet smaller than the full-width background so idle animations do not clip.
        PetPhaseTransition(
            pet = pet,
            fromStage = evolutionStage - 1,
            toStage = evolutionStage,
            modifier = Modifier.fillMaxSize(PET_SCALE_FRACTION)
        )

        if (showNameOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(AppTheme.current.background.copy(alpha = 0.7f))
            ) {
                Text(
                    text = "${pet.name}\nLv.${pet.level}",
                    color = AppTheme.current.onBackground,
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
    onTransitionCompleted: (() -> Unit)? = null
) {
    val normalizedToStage = toStage.takeIf { it in 0..4 } ?: 0
    val normalizedFromStage = fromStage.takeIf { it in 0..3 && it == normalizedToStage - 1 }
        ?: (normalizedToStage - 1).takeIf { it in 0..3 } ?: normalizedToStage

    val context = LocalContext.current
    val density = LocalDensity.current
    val transition = rememberInfiniteTransition(label = "pet idle")

    var hasPlayedTransition by remember {
        mutableStateOf(
            normalizedToStage > 0 &&
                PetTransitionPrefs.hasPlayedTransition(context, normalizedToStage - 1, normalizedToStage)
        )
    }
    val shouldTransition = normalizedToStage > 0 && !hasPlayedTransition

    LaunchedEffect(normalizedToStage) {
        hasPlayedTransition = normalizedToStage > 0 &&
            PetTransitionPrefs.hasPlayedTransition(context, normalizedToStage - 1, normalizedToStage)
    }

    val transitionProgress = animateFloatAsState(
        targetValue = if (shouldTransition) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "pet phase transition progress"
    )

    LaunchedEffect(transitionProgress.value, shouldTransition, hasPlayedTransition) {
        if (shouldTransition && !hasPlayedTransition && transitionProgress.value >= 1f) {
            hasPlayedTransition = true
            PetTransitionPrefs.markTransitionPlayed(context, normalizedFromStage, normalizedToStage)
            onTransitionCompleted?.invoke()
        }
    }

    val transitionScale = animateFloatAsState(
        targetValue = if (shouldTransition) 1.06f else 1f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "pet phase transition scale"
    )

    val transitionShift = animateFloatAsState(
        targetValue = if (shouldTransition) 10f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "pet phase transition shift"
    )

    // Breathing and Idle Animations
    val breathingScale = transition.animateFloat(
        initialValue = 1f - petIdleScaleAmplitude(normalizedToStage, pet.mood),
        targetValue = 1f + petIdleScaleAmplitude(normalizedToStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(petIdleBreathingDuration(normalizedToStage, pet.mood), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pet breathing scale"
    )

    val breathingY = transition.animateFloat(
        initialValue = -petIdleVerticalAmplitude(normalizedToStage, pet.mood),
        targetValue = petIdleVerticalAmplitude(normalizedToStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(petIdleBreathingDuration(normalizedToStage, pet.mood), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pet breathing translation"
    )

    val rotation = transition.animateFloat(
        initialValue = -petIdleRotationAmplitude(normalizedToStage, pet.mood),
        targetValue = petIdleRotationAmplitude(normalizedToStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(petIdleSwayDuration(normalizedToStage, pet.mood), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pet idle sway"
    )

    val softBounce = transition.animateFloat(
        initialValue = -petIdleBounceAmplitude(normalizedToStage, pet.mood),
        targetValue = petIdleBounceAmplitude(normalizedToStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(petIdleBounceDuration(normalizedToStage, pet.mood), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pet soft bounce"
    )

    val showTransition = shouldTransition || hasPlayedTransition
    val transitionScaleValue = transitionScale.value
    val transitionShiftPx = transitionShift.value * density.density
    val idleScale = breathingScale.value
    val idleRotation = rotation.value
    val idleTranslationY = (breathingY.value + softBounce.value) * density.density

    // Modifiers using the passed parent 'modifier'
    val transitionFromModifier = modifier.graphicsLayer(
        alpha = 1f - transitionProgress.value,
        scaleX = idleScale * transitionScaleValue,
        scaleY = idleScale * transitionScaleValue,
        rotationZ = idleRotation,
        translationY = idleTranslationY - transitionShiftPx
    )

    val transitionToModifier = modifier.graphicsLayer(
        alpha = transitionProgress.value,
        scaleX = idleScale * transitionScaleValue,
        scaleY = idleScale * transitionScaleValue,
        rotationZ = idleRotation,
        translationY = idleTranslationY + transitionShiftPx
    )

    val finalModifier = modifier.graphicsLayer(
        scaleX = idleScale,
        scaleY = idleScale,
        rotationZ = idleRotation,
        translationY = idleTranslationY
    )

    if (showTransition) {
        PetImageLayer(pet, normalizedFromStage, transitionFromModifier)
        PetImageLayer(pet, normalizedToStage, transitionToModifier)
    } else {
        PetImageLayer(pet, normalizedToStage, finalModifier)
    }
}

@Composable
private fun PetImageLayer(pet: PetEntity, evolutionStage: Int, modifier: Modifier = Modifier) {
    val assetManager = LocalContext.current.assets
    val baseAssetPath = remember(evolutionStage, pet.equippedAura) {
        AssetResolver.dragonBaseAssetPath(assetManager, evolutionStage, pet.equippedAura)
    }
    val basePainter = rememberAssetPainter(baseAssetPath, "dragon base")

    if (basePainter != null) {
        Image(
            painter = basePainter,
            contentDescription = "Pet image",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }

    OutfitLayer(pet, evolutionStage, modifier)
}

@Composable
private fun OutfitLayer(pet: PetEntity, evolutionStage: Int, modifier: Modifier = Modifier) {
    val assetManager = LocalContext.current.assets
    val outfitAssetPath = remember(evolutionStage, pet.equippedOutfit) {
        AssetResolver.outfitAssetPath(assetManager, evolutionStage, pet.equippedOutfit)
    }
    val outfitPainter = rememberAssetPainter(outfitAssetPath, "outfit overlay")

    if (outfitPainter != null) {
        Image(
            painter = outfitPainter,
            contentDescription = "Outfit",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
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