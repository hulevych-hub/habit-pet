package com.example.mobile.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
    // Determine evolution stage and mood
    val evolutionStage = pet.evolutionStage
    val mood = pet.mood

    // Simple animation values
    var offset by remember { mutableStateOf(0f) }

    // Simple animation loop - subtle floating movement
    offset = (System.currentTimeMillis() % 2000) / 2000f * 3f

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
    androidx.compose.foundation.layout.Box(
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
            androidx.compose.foundation.Image(
                painter = painterResource(backgroundImage),
                contentDescription = "Pet background",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = offset.dp)
            )
        }

        // Base pet image
        androidx.compose.foundation.Image(
            painter = painterResource(petImage),
            contentDescription = "Pet image",
            modifier = Modifier
                .size(180.dp)
                .offset(y = offset.dp)
        )

        // Scarf (if equipped)
        if (pet.equippedScarf != null) {
            val scarfImage = when (pet.equippedScarf) {
                "red_scarf" -> R.drawable.red_scarf
                "blue_scarf" -> R.drawable.blue_scarf
                else -> R.drawable.red_scarf // fallback
            }
            androidx.compose.foundation.Image(
                painter = painterResource(scarfImage),
                contentDescription = "Pet scarf",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = offset.dp)
            )
        }

        // Glasses (if equipped)
        if (pet.equippedGlasses != null) {
            val glassesImage = when (pet.equippedGlasses) {
                "round_glasses" -> R.drawable.round_glasses
                "sunglasses" -> R.drawable.sunglasses
                else -> R.drawable.round_glasses // fallback
            }
            androidx.compose.foundation.Image(
                painter = painterResource(glassesImage),
                contentDescription = "Pet glasses",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = offset.dp)
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
            androidx.compose.foundation.Image(
                painter = painterResource(hatImage),
                contentDescription = "Pet hat",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = offset.dp)
            )
        }

        // Pet name and level overlay
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(8.dp)
                .background(
                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                )
        ) {
            androidx.compose.material3.Text(
                text = "${pet.name}\nLv.${pet.level}",
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}