package com.example.mobile.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mobile.R
import com.example.mobile.domain.CustomizationTypes

@Composable
fun AssetPreview(
    itemType: String,
    itemId: String,
    imageUrl: String,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        drawableIdForAsset(itemType, itemId, imageUrl)?.let { drawableId ->
            Image(
                painter = painterResource(drawableId),
                contentDescription = "${CustomizationTypes.displayName(itemType)} Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } ?: when (itemType) {
            CustomizationTypes.OUTFIT -> {
                Icon(
                    imageVector = Icons.Default.Checkroom,
                    contentDescription = "Outfit Preview",
                    tint = tintColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            CustomizationTypes.BACKGROUND -> {
                Icon(
                    imageVector = Icons.Default.Wallpaper,
                    contentDescription = "Scene Background Preview",
                    tint = tintColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            CustomizationTypes.AURA -> {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(tintColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Aura Preview",
                        tint = tintColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            else -> {
                Icon(Icons.Default.Block, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            }
        }
    }
}

private fun drawableIdForAsset(itemType: String, itemId: String, imageUrl: String): Int? = when (itemType) {
    CustomizationTypes.BACKGROUND -> backgroundImageForAsset(imageUrl)
    CustomizationTypes.OUTFIT -> outfitImageForAsset(itemId)
    CustomizationTypes.AURA -> R.drawable.aura_placeholder
    else -> null
}

private fun backgroundImageForAsset(imageUrl: String): Int? = when (imageUrl.removeSuffix(".png")) {
    "background_forest", "forest" -> R.drawable.background_forest
    "background_beach", "beach" -> R.drawable.background_beach
    "background_mountains", "mountains" -> R.drawable.background_mountains
    "background_night_sky", "night_sky" -> R.drawable.background_night_sky
    else -> null
}

private fun outfitImageForAsset(itemId: String): Int = when (itemId) {
    "royal_scarf" -> R.drawable.red_scarf
    "crystal_crown" -> R.drawable.crown
    else -> R.drawable.outfit_placeholder
}