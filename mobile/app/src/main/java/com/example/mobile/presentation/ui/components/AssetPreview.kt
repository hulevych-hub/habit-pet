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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.ui.theme.HabitPetTheme
import com.example.mobile.util.AssetResolver

@Composable
fun AssetPreview(
    itemType: String,
    itemId: String,
    imageUrl: String,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetPath = remember(itemType, itemId, imageUrl) {
        AssetResolver.itemAssetPath(context.assets, itemType, itemId, imageUrl)
    }
    val assetPainter = rememberAssetPainter(assetPath, "asset preview")

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (assetPainter != null) {
            Image(
                painter = assetPainter,
                contentDescription = "${CustomizationTypes.displayName(itemType)} Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else when (itemType) {
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

@Preview(showBackground = true, device = "spec:width=120px,height=120px,dpi=320")
@Composable
private fun AssetPreviewIconFallbackPreview() {
    HabitPetTheme {
        AssetPreview(
            itemType = CustomizationTypes.OUTFIT,
            itemId = "missing-outfit",
            imageUrl = "",
            tintColor = Color(0xFF6C5CE7),
            modifier = Modifier.size(120.dp)
        )
    }
}
