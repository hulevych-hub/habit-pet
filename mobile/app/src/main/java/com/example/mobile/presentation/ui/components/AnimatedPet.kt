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
 * Animated pet display using actual drawable assets with proper animations
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
        androidx.compose.foundation.Image(
            painter = painterResource(petImage),
            contentDescription = "Pet image",
            modifier = Modifier
                .size(180.dp)
                .offset(y = offset.dp)
        )

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