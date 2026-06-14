package com.example.mobile.presentation.ui.components

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext

private const val ASSET_RENDERER_TAG = "AssetRenderer"

@Composable
fun rememberAssetPainter(assetPath: String?, logLabel: String): Painter? {
    val assetManager = LocalContext.current.assets
    return remember(assetPath) {
        if (assetPath == null) {
            null
        } else {
            decodeAsset(assetManager, assetPath, logLabel)
        }
    }
}

private fun decodeAsset(assetManager: AssetManager, assetPath: String, logLabel: String): Painter? {
    return try {
        assetManager.open(assetPath).use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap?.asImageBitmap()?.let { BitmapPainter(it) }
        }
    } catch (exception: Exception) {
        Log.w(ASSET_RENDERER_TAG, "Missing asset for $logLabel: $assetPath", exception)
        null
    }
}
