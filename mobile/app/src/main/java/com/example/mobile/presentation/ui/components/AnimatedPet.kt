package com.example.mobile.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.presentation.ui.animations.PetAnimations

/**
 * Simple animated pet display placeholder
 */
@Composable
fun AnimatedPet(
    pet: PetEntity,
    modifier: Modifier = Modifier,
    size: IntSize = IntSize(200, 200)
) {
    val density = LocalDensity.current

    // Simple animation value
    var offset by remember { mutableStateOf(0f) }

    // Determine evolution stage and mood
    val evolutionStage = pet.evolutionStage
    val mood = pet.mood

    // Apply mood modifiers
    val moodModifier = PetAnimations.applyMoodModifier(1.0f, mood)

    // Simple animation loop - just for demo
    // In a real implementation, we'd use proper animation APIs
    offset = 5f * moodModifier

    // Background color based on evolution stage
    val backgroundColor = when (evolutionStage) {
        0 -> MaterialTheme.colorScheme.secondaryContainer // Egg - light blue
        1 -> MaterialTheme.colorScheme.tertiaryContainer  // Hatchling - light green
        2 -> MaterialTheme.colorScheme.primaryContainer   // Young Dragon - light red/orange
        3 -> MaterialTheme.colorScheme.errorContainer     // Adult Dragon - light purple
        4 -> MaterialTheme.colorScheme.surfaceContainerHighest // Ancient Dragon - gold/yellow
        else -> MaterialTheme.colorScheme.background
    }

    // Get evolution stage text
    val evolutionText = when (evolutionStage) {
        0 -> "Egg"
        1 -> "Hatchling"
        2 -> "Young Dragon"
        3 -> "Adult Dragon"
        4 -> "Ancient Dragon"
        else -> "Unknown"
    }

    // Simple container with background color
    Box(
        modifier = modifier
            .size(size.width.dp, size.height.dp)
            .background(backgroundColor)
    ) {
        // Centered text showing pet info
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offset.dp)
        ) {
            // Simple text display
            androidx.compose.material3.Text(
                text = "${pet.name}\n$evolutionText\nLv.${pet.level}",
                color = Color.Black
            )
        }
    }
}