package com.example.mobile.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.presentation.ui.animations.PetAnimations
import com.example.mobile.R

/**
 * Animated pet display using actual drawable assets with proper animations and equipped items
 */
@Composable
fun AnimatedPet(
    pet: PetEntity,
    modifier: Modifier = Modifier,
    size: IntSize = IntSize(200, 200)
) {
    val evolutionStage = pet.evolutionStage.takeIf { it in 0..4 } ?: 0
    val transition = rememberInfiniteTransition(label = "pet idle")
    val density = LocalDensity.current

    val breathingScale = transition.animateFloat(
        initialValue = 1f - petIdleScaleAmplitude(evolutionStage, pet.mood),
        targetValue = 1f + petIdleScaleAmplitude(evolutionStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = petIdleBreathingDuration(evolutionStage),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet breathing scale"
    )

    val breathingY = transition.animateFloat(
        initialValue = -petIdleVerticalAmplitude(evolutionStage, pet.mood),
        targetValue = petIdleVerticalAmplitude(evolutionStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = petIdleBreathingDuration(evolutionStage),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet breathing translation"
    )

    val rotation = transition.animateFloat(
        initialValue = -petIdleRotationAmplitude(evolutionStage, pet.mood),
        targetValue = petIdleRotationAmplitude(evolutionStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = petIdleSwayDuration(evolutionStage),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet idle sway"
    )

    val softBounce = transition.animateFloat(
        initialValue = -petIdleBounceAmplitude(evolutionStage, pet.mood),
        targetValue = petIdleBounceAmplitude(evolutionStage, pet.mood),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = petIdleBounceDuration(evolutionStage),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet soft bounce"
    )

    val animatedPetModifier = Modifier
        .size(180.dp)
        .graphicsLayer(
            scaleX = breathingScale.value,
            scaleY = breathingScale.value,
            rotationZ = rotation.value,
            translationY = (breathingY.value + softBounce.value) * density.density
        )

    // Get the appropriate image resource for the evolution stage
    val petImage = when (evolutionStage) {
        0 -> R.drawable.egg
        1 -> R.drawable.hatchling
        2 -> R.drawable.young_dragon
        3 -> R.drawable.adult_dragon
        4 -> R.drawable.ancient_dragon
        else -> R.drawable.egg // fallback
    }

    // Container with proper sizing
    Box(
        modifier = modifier
            .size(size.width.dp, size.height.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Layered rendering: Background -> Pet -> Scarf -> Glasses -> Hat

        // Background (if equipped)
        if (pet.equippedBackground != null) {
            val backgroundImage = when (pet.equippedBackground) {
                "background_forest" -> R.drawable.background_forest
                "background_beach" -> R.drawable.background_beach
                "background_mountains" -> R.drawable.background_mountains
                "background_night_sky" -> R.drawable.background_night_sky
                else -> R.drawable.background_forest // fallback
            }
            Image(
                painter = painterResource(backgroundImage),
                contentDescription = "Pet background",
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.Center)
            )
        }

        // Base pet image and equipped items move together for a natural idle feel
        Box(
            modifier = animatedPetModifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(petImage),
                contentDescription = "Pet image",
                modifier = Modifier.size(180.dp)
            )

            // Scarf (if equipped)
            if (pet.equippedScarf != null) {
                val scarfImage = when (pet.equippedScarf) {
                    "red_scarf" -> R.drawable.red_scarf
                    "blue_scarf" -> R.drawable.blue_scarf
                    else -> R.drawable.red_scarf // fallback
                }
                Image(
                    painter = painterResource(scarfImage),
                    contentDescription = "Pet scarf",
                    modifier = Modifier.size(180.dp)
                )
            }

            // Glasses (if equipped)
            if (pet.equippedGlasses != null) {
                val glassesImage = when (pet.equippedGlasses) {
                    "round_glasses" -> R.drawable.round_glasses
                    "sunglasses" -> R.drawable.sunglasses
                    else -> R.drawable.round_glasses // fallback
                }
                Image(
                    painter = painterResource(glassesImage),
                    contentDescription = "Pet glasses",
                    modifier = Modifier.size(180.dp)
                )
            }

            // Hat (if equipped)
            if (pet.equippedHat != null) {
                val hatImage = when (pet.equippedHat) {
                    "top_hat" -> R.drawable.top_hat
                    "crown" -> R.drawable.crown
                    "wizard_hat" -> R.drawable.wizard_hat
                    else -> R.drawable.top_hat // fallback
                }
                Image(
                    painter = painterResource(hatImage),
                    contentDescription = "Pet hat",
                    modifier = Modifier.size(180.dp)
                )
            }
        }

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

private fun petIdleBreathingDuration(stage: Int): Int {
    return when (stage) {
        0 -> 2600
        1 -> 3000
        2 -> 3400
        3 -> 3800
        4 -> 4200
        else -> 3000
    }
}

private fun petIdleSwayDuration(stage: Int): Int {
    return when (stage) {
        0 -> 4600
        1 -> 5600
        2 -> 6400
        3 -> 7200
        4 -> 8000
        else -> 5600
    }
}

private fun petIdleBounceDuration(stage: Int): Int {
    return when (stage) {
        0 -> 3600
        1 -> 4800
        2 -> 5600
        3 -> 6400
        4 -> 7200
        else -> 4800
    }
}

private fun moodAdjusted(value: Float, mood: String): Float {
    return PetAnimations.applyMoodModifier(value, mood).coerceIn(value * 0.7f, value * 1.2f)
}
