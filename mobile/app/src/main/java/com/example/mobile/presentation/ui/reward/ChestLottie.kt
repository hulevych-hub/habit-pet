package com.example.mobile.presentation.ui.reward

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.dotlottie.dlplayer.Mode
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource

private const val REWARD_LOTTIE_SPEED = 1.5f

@Composable
fun AnimatedRewardChest(
    size: Dp,
    tint: Color,
    onOpened: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    stateKey: String = "reward-chest",
    isOpenOverride: Boolean? = null
) {
    var chestIsOpen by rememberSaveable(stateKey) { mutableStateOf(false) }
    val isOpen = isOpenOverride ?: chestIsOpen

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = !isOpen,
            enter = scaleIn(
                animationSpec = tween(durationMillis = 220),
                initialScale = 0.86f
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = scaleOut(
                animationSpec = tween(durationMillis = 180),
                targetScale = 0.72f
            ) + fadeOut(animationSpec = tween(durationMillis = 160))
        ) {
            DotLottieAnimation(
                source = CHEST_CLOSED_URL,
                autoplay = true,
                loop = true,
                speed = REWARD_LOTTIE_SPEED,
                useFrameInterpolation = false,
                playMode = Mode.FORWARD,
                modifier = Modifier.background(Color.Transparent).size(size)
            )
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = scaleIn(
                animationSpec = tween(durationMillis = 360),
                initialScale = 0.24f
            ) + fadeIn(animationSpec = tween(durationMillis = 260)),
            exit = fadeOut(animationSpec = tween(durationMillis = 160))
        ) {
            DotLottieAnimation(
                source = CHEST_OPEN_URL,
                autoplay = true,
                loop = false,
                speed = REWARD_LOTTIE_SPEED,
                useFrameInterpolation = false,
                playMode = Mode.FORWARD,
                modifier = Modifier.background(Color.Transparent).size(size)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (enabled && !isOpen) {
                        Modifier.clickable {
                            chestIsOpen = true
                            onOpened()
                        }
                    } else {
                        Modifier
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
