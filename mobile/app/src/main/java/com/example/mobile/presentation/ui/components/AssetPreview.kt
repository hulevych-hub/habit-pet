package com.example.mobile.presentation.ui.components

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
import androidx.compose.ui.unit.dp
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
        // NOTE: If you are using Coil for remote URLs later, uncomment this line:
        // AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())

        when (itemType) {
            CustomizationTypes.OUTFIT -> {
                Icon(
                    imageVector = Icons.Default.Checkroom, // Premium clothing hanger icon
                    contentDescription = "Outfit Preview",
                    tint = tintColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            CustomizationTypes.BACKGROUND -> {
                Icon(
                    imageVector = Icons.Default.Wallpaper, // Premium wallpaper/scene card icon
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
                        imageVector = Icons.Default.Brush, // Sparkle / dynamic glow brush style marker
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