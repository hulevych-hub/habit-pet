package com.example.mobile.presentation.ui.reward

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.dotlottie.dlplayer.Mode
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource

private const val REWARD_LOTTIE_SPEED = 0.5f

@Composable
fun AnimatedRewardChest(
    size: Dp,
    tint: Color,
    onOpened: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isOpen by rememberSaveable { mutableStateOf(false) }

    val chestRadiusPx = with(LocalDensity.current) { size.toPx() * 0.62f }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawCircle(
                    color = tint.copy(alpha = if (isOpen) 0.16f else 0.06f),
                    radius = chestRadiusPx
                )
            },
        contentAlignment = Alignment.Center
    ) {
        DotLottieAnimation(
            source = if (isOpen) CHEST_OPEN_URL else CHEST_CLOSED_URL,
            autoplay = true,
            loop = !isOpen,
            speed = REWARD_LOTTIE_SPEED,
            useFrameInterpolation = false,
            playMode = Mode.FORWARD,
            modifier = Modifier.background(Color.Transparent).size(size)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    enabled = enabled && !isOpen,
                    onClick = {
                        if (!isOpen) {
                            isOpen = true
                            onOpened()
                        }
                    }
                )
        )
    }
}

private val CHEST_CLOSED_URL: DotLottieSource by lazy {
    DotLottieSource.Url("https://lottie.host/bb9fe365-b755-491c-abe4-8d95ff9c651c/HaFIwxXTlR.json")
}

private val CHEST_OPEN_URL: DotLottieSource by lazy {
    DotLottieSource.Url("https://lottie.host/a5510b88-7fa1-462b-b71f-a6c0dc679f1d/01CqxsxE3x.json")
}
