package com.example.mobile.presentation.ui.reward

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotlottie.dlplayer.Mode
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ChestType
import com.example.mobile.domain.EquipableConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.presentation.ui.components.AssetPreview
import com.example.mobile.presentation.ui.components.rememberAssetPainter
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.util.AssetResolver
import com.example.mobile.util.ReinforcementMessageProvider
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.delay

private const val REWARD_LOTTIE_SIZE_MULTIPLIER = 3.0f
private const val REWARD_LOTTIE_SPEED = 1.5f

private val LEVEL_UP_LOTTIE_URL: DotLottieSource by lazy {
    DotLottieSource.Url("https://lottie.host/7fc1d557-213d-491f-bec9-9dcd074249fc/dw4NytzH9z.lottie")
}

private val EXP_LOTTIE_URL: DotLottieSource by lazy {
    DotLottieSource.Url("https://lottie.host/13198b39-d865-446e-a6dc-57f28065f3a0/y4WegYS6fl.lottie")
}

private val COIN_REWARD_LOTTIE_URL: DotLottieSource by lazy {
    DotLottieSource.Url("https://lottie.host/7cc17b3d-9404-4630-a046-d72799cc98c4/jmIwdfBZBo.lottie")
}

private val STREAK_LOTTIE_URL: DotLottieSource by lazy {
    DotLottieSource.Url("https://lottie.host/fc31fca3-cd41-44da-8dba-80942d5ad3d3/eRS4EJkA29.lottie")
}
private val ACHIEVEMENT_LOTTIE_URL: DotLottieSource by lazy {
    DotLottieSource.Url("https://lottie.host/a92142bc-ccbb-4df2-82b7-42247bc22610/i09Ybooztt.lottie")
}

@Composable
fun RewardScreen(
    reward: RewardUiEvent?,
    pet: PetEntity = PetEntity(id = 1),
    onRewardCompleted: () -> Unit
) {
    if (reward == null) return

    val context = LocalContext.current
    val reinforcementMessage = remember(reward) {
        ReinforcementMessageProvider.rewardMessage(context, reward)
    }
    val emphasisTier = reward.emphasisTier()

    var isChestOpen by remember(reward) { mutableStateOf(false) }
    var areChestRewardsVisible by remember(reward) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppTheme.current.rewardBackdropStart,
                        AppTheme.current.rewardBackdropCenter,
                        AppTheme.current.rewardBackdropEnd
                    )
                )
            )
            .pointerInput(reward, isChestOpen, areChestRewardsVisible) {
                detectTapGestures {
                    if (reward is RewardUiEvent.ChestReward) {
                        if (!isChestOpen) {
                            isChestOpen = true
                        } else if (areChestRewardsVisible) {
                            onRewardCompleted()
                        } else {
                            areChestRewardsVisible = true
                        }
                    } else {
                        onRewardCompleted()
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {

            when (reward) {

                is RewardUiEvent.LevelUpReward -> LevelUpRewardContent(
                    level = reward.level,
                    coinsEarned = reward.coins,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier
                )

                is RewardUiEvent.DragonEvolutionReward -> DragonEvolutionRewardContent(
                    pet = pet,
                    fromStage = reward.fromStage,
                    toStage = reward.toStage,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier
                )

                is RewardUiEvent.StreakReward -> StreakRewardContent(
                    streak = reward.streak,
                    coinsEarned = reward.coins,
                    rewardSummary = reward.rewardSummary,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier
                )

                is RewardUiEvent.ExpReward -> ExpRewardContent(
                    amount = reward.amount,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier
                )

                is RewardUiEvent.CustomizationReward -> CustomizationRewardContent(
                    equipableId = reward.equipableId,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier
                )

                is RewardUiEvent.ChestReward -> ChestRewardContent(
                    rewardType = reward.rewardType,
                    amount = reward.amount,
                    expAmount = reward.expAmount,
                    customizationId = reward.customizationId,
                    equipableId = reward.equipableId,
                    isOpen = isChestOpen,
                    areRewardsVisible = areChestRewardsVisible,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier,
                    onRewardsVisibleChanged = { areChestRewardsVisible = it },
                    onChestOpened = { isChestOpen = true },
                    onChestContinue = onRewardCompleted
                )

                is RewardUiEvent.CoinReward -> CoinRewardContent(
                    amount = reward.amount,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier
                )
            }
        }
    }
}

@Composable
private fun RewardDotLottie(
    source: DotLottieSource,
    loop: Boolean,
    modifier: Modifier = Modifier
) {
    DotLottieAnimation(
        source = source,
        autoplay = true,
        loop = loop,
        speed = REWARD_LOTTIE_SPEED,
        useFrameInterpolation = false,
        playMode = Mode.FORWARD,
        modifier = modifier.background(Color.Transparent)
    )
}

@Composable
private fun XpBoostAnimation(
    modifier: Modifier = Modifier
) {
    var isPulsing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isPulsing = !isPulsing
            delay(420)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.18f else 0.86f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "xp boost scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isPulsing) 18f else -18f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "xp boost rotation"
    )

    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = "XP boost",
        tint = AppTheme.current.rewardAccent,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .background(Color.Transparent)
    )
}

// Level Up Reward Screen
@Composable
private fun LevelUpRewardContent(
    level: Int,
    coinsEarned: Int,
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier
) {
    RewardEmphasisFrame(tier = emphasisTier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            RewardDotLottie(
                source = LEVEL_UP_LOTTIE_URL,
                loop = false,
                modifier = Modifier.size((120 * emphasisTier.rewardScale * REWARD_LOTTIE_SIZE_MULTIPLIER).dp)
            )

            Spacer(modifier = Modifier.height((-4).dp))

            Text(
                text = "Level $level",
                color = AppTheme.current.rewardText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "+$coinsEarned coins",
                color = AppTheme.current.rewardTextMuted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

@Composable
private fun ExpRewardContent(
    amount: Long,
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier
) {
    RewardEmphasisFrame(tier = emphasisTier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            XpBoostAnimation(
                modifier = Modifier.size((120 * emphasisTier.rewardScale * REWARD_LOTTIE_SIZE_MULTIPLIER).dp)
            )

            Spacer(modifier = Modifier.height((-4).dp))

            Text(
                text = "+$amount XP",
                color = AppTheme.current.rewardText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

@Composable
private fun CustomizationRewardContent(
    equipableId: String,
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier
) {
    val definition = EquipableConfig.definition(equipableId)
    val name = definition?.name ?: "Customization"

    RewardEmphasisFrame(tier = emphasisTier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            definition?.let {
                AssetPreview(
                    itemType = definition.type.value,
                    itemId = definition.id,
                    imageUrl = definition.imageUrl,
                    tintColor = emphasisTier.rewardColor,
                    modifier = Modifier.size((120 * emphasisTier.rewardScale * REWARD_LOTTIE_SIZE_MULTIPLIER).dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name,
                color = AppTheme.current.rewardText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

// Chest Reward Screen
@Composable
private fun ChestRewardContent(
    rewardType: String,
    amount: Any,
    expAmount: Int = 0,
    customizationId: Long? = null,
    equipableId: String? = null,
    isOpen: Boolean,
    areRewardsVisible: Boolean,
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier,
    onRewardsVisibleChanged: (Boolean) -> Unit,
    onChestOpened: () -> Unit,
    onChestContinue: () -> Unit
) {
    LaunchedEffect(rewardType, amount, expAmount, customizationId, equipableId) {
        onRewardsVisibleChanged(false)
    }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            delay(900)
            onRewardsVisibleChanged(true)
        } else {
            onRewardsVisibleChanged(false)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        RewardEmphasisFrame(tier = emphasisTier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                val chestSize = (180 * emphasisTier.chestSizeMultiplier * REWARD_LOTTIE_SIZE_MULTIPLIER).dp

                Box(
                    modifier = Modifier.size(chestSize),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AnimatedRewardChest(
                        size = chestSize,
                        tint = emphasisTier.chestTint,
                        modifier = Modifier.fillMaxSize(),
                        stateKey = chestStateKey(rewardType, amount, expAmount, customizationId, equipableId),
                        isOpenOverride = isOpen,
                        enabled = false
                    )

                    if (areRewardsVisible) {
                        val rewardItems = mutableListOf<ChestRewardItem>()
                        val customizationDefinition = equipableId?.let { EquipableConfig.definition(it) }

                        when (amount) {
                            is Int -> if (amount > 0) rewardItems.add(ChestRewardItem.Coin(amount))
                            is String -> if (!amount.isEmpty()) rewardItems.add(ChestRewardItem.Text(amount))
                        }

                        if (expAmount > 0) {
                            rewardItems.add(ChestRewardItem.Exp(expAmount))
                        }

                        if (customizationId != null || equipableId != null) {
                            val equipableName = equipableId
                                ?.let { EquipableConfig.definition(it)?.name }
                                ?: "Customization"
                            rewardItems.add(ChestRewardItem.Customization(equipableName))
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            customizationDefinition?.let { definition ->
                                AssetPreview(
                                    itemType = definition.type.value,
                                    itemId = definition.id,
                                    imageUrl = definition.imageUrl,
                                    tintColor = emphasisTier.rewardColor,
                                    modifier = Modifier.size(96.dp)
                                )
                            }

                            rewardItems.forEach { item ->
                                when (item) {
                                    is ChestRewardItem.Coin -> RewardLineWithIcon(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.MonetizationOn,
                                                contentDescription = null,
                                                tint = AppTheme.current.gold,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        text = "+${item.amount} coins"
                                    )
                                    is ChestRewardItem.Exp -> RewardLineWithIcon(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = AppTheme.current.rewardAccent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        text = "+${item.amount} XP"
                                    )
                                    is ChestRewardItem.Text -> Text(
                                        text = item.text,
                                        color = AppTheme.current.rewardText,
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                    is ChestRewardItem.Customization -> Text(
                                        text = item.name,
                                        color = AppTheme.current.rewardText,
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height((-4).dp))

                if (areRewardsVisible) {
                    ReinforcementMessage(reinforcementMessage)
                } else {
                    Text(
                        text = "Tap to open",
                        color = AppTheme.current.rewardTextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isOpen, areRewardsVisible, onChestOpened, onRewardsVisibleChanged, onChestContinue) {
                    detectTapGestures {
                        if (!isOpen) {
                            onChestOpened()
                        } else if (areRewardsVisible) {
                            onChestContinue()
                        } else {
                            onRewardsVisibleChanged(true)
                        }
                    }
                }
        )
    }
}

// Streak Reward Screen
@Composable
private fun StreakRewardContent(
    streak: Int,
    coinsEarned: Int,
    rewardSummary: List<String> = emptyList(),
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier
) {
    val displayedSummary = if (rewardSummary.isNotEmpty()) {
        rewardSummary
    } else {
        listOf(
            "$streak-day streak",
            if (coinsEarned > 0) "+$coinsEarned coins" else "Reward chest unlocked"
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RewardDotLottie(
            source = STREAK_LOTTIE_URL,
            loop = true,
            modifier = Modifier.size((150 * emphasisTier.rewardScale * REWARD_LOTTIE_SIZE_MULTIPLIER).dp)
        )

        Spacer(modifier = Modifier.height((-4).dp))

        Text(
            text = "$streak-Day Streak",
            style = MaterialTheme.typography.headlineMedium,
            color = AppTheme.current.rewardText,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        displayedSummary.drop(1).forEach { rewardLine ->
            Text(
                text = rewardLine,
                color = AppTheme.current.rewardTextMuted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        ReinforcementMessage(reinforcementMessage)
    }
}

private sealed class ChestRewardItem {
    data class Coin(val amount: Int) : ChestRewardItem()
    data class Exp(val amount: Int) : ChestRewardItem()
    data class Text(val text: String) : ChestRewardItem()
    data class Customization(val name: String) : ChestRewardItem()
}

@Composable
private fun RewardLineWithIcon(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text = text,
            color = AppTheme.current.rewardText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private enum class RewardEmphasisTier(
    val rewardColor: Color,
    val chestTint: Color,
    val glowColor: Color,
    val rewardScale: Float,
    val chestSizeMultiplier: Float,
    val emphasisDurationMillis: Long
) {
    SMALL(
        rewardColor = AppTheme.current.mint,
        chestTint = AppTheme.current.gold,
        glowColor = AppTheme.current.gold,
        rewardScale = 1.0f,
        chestSizeMultiplier = 1.0f,
        emphasisDurationMillis = 650L
    ),
    RARE(
        rewardColor = AppTheme.current.blue,
        chestTint = AppTheme.current.blue,
        glowColor = AppTheme.current.blue,
        rewardScale = 1.06f,
        chestSizeMultiplier = 1.08f,
        emphasisDurationMillis = 950L
    ),
    EPIC(
        rewardColor = AppTheme.current.purple,
        chestTint = AppTheme.current.purple,
        glowColor = AppTheme.current.purple,
        rewardScale = 1.12f,
        chestSizeMultiplier = 1.16f,
        emphasisDurationMillis = 1300L
    )
}

private fun RewardUiEvent.emphasisTier(): RewardEmphasisTier = when (this) {
    is RewardUiEvent.CoinReward -> RewardEmphasisTier.SMALL
    is RewardUiEvent.LevelUpReward -> RewardEmphasisTier.RARE
    is RewardUiEvent.DragonEvolutionReward -> RewardEmphasisTier.EPIC
    is RewardUiEvent.StreakReward -> if (streak >= 30) RewardEmphasisTier.EPIC else RewardEmphasisTier.RARE
    is RewardUiEvent.ExpReward -> RewardEmphasisTier.RARE
    is RewardUiEvent.CustomizationReward -> RewardEmphasisTier.RARE
    is RewardUiEvent.ChestReward -> chestEmphasisTier(
        rewardType = rewardType,
        amount = amount,
        expAmount = expAmount,
        customizationId = customizationId,
        equipableId = equipableId
    )
}

private fun chestStateKey(
    rewardType: String,
    amount: Any,
    expAmount: Int,
    customizationId: Long?,
    equipableId: String?
): String = "$rewardType|$amount|$expAmount|$customizationId|$equipableId"

private fun chestEmphasisTier(
    rewardType: String,
    amount: Any,
    expAmount: Int,
    customizationId: Long?,
    equipableId: String? = null
): RewardEmphasisTier {
    val chestType = ChestType.values().firstOrNull {
        it.name.equals(rewardType.substringAfterLast('_'), ignoreCase = true) ||
            rewardType.contains(it.name, ignoreCase = true)
    }

    return when (chestType) {
        ChestType.LEGENDARY -> RewardEmphasisTier.EPIC
        ChestType.EPIC -> RewardEmphasisTier.EPIC
        ChestType.RARE -> RewardEmphasisTier.RARE
        ChestType.NORMAL, null -> {
            val coinAmount = amount as? Int ?: 0
            when {
                customizationId != null || equipableId != null || expAmount >= 150 || coinAmount >= 80 -> RewardEmphasisTier.EPIC
                expAmount > 0 || coinAmount >= 30 -> RewardEmphasisTier.RARE
                else -> RewardEmphasisTier.SMALL
            }
        }
    }
}

@Composable
private fun RewardEmphasisFrame(
    tier: RewardEmphasisTier,
    content: @Composable () -> Unit
) {
    var emphasisActive by remember { mutableStateOf(false) }

    LaunchedEffect(tier) {
        emphasisActive = true
        delay(tier.emphasisDurationMillis)
        emphasisActive = false
    }

    val scale by animateFloatAsState(
        targetValue = if (emphasisActive) tier.rewardScale else 1f,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "reward emphasis scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        content()
    }
}

@Composable
private fun ReinforcementMessage(message: String) {
    Spacer(modifier = Modifier.height((-8).dp))

    Text(
        text = message,
        color = AppTheme.current.rewardText,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium
    )
}

// Coin Reward Screen
@Composable
private fun CoinRewardContent(
    amount: Int,
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier
) {
    RewardEmphasisFrame(tier = emphasisTier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            RewardDotLottie(
                source = COIN_REWARD_LOTTIE_URL,
                loop = true,
                modifier = Modifier.size((112 * emphasisTier.rewardScale * REWARD_LOTTIE_SIZE_MULTIPLIER).dp)
            )

            Spacer(modifier = Modifier.height((-4).dp))

            Text(
                text = "+$amount coins",
                style = MaterialTheme.typography.headlineMedium,
                color = AppTheme.current.rewardText,
                fontWeight = FontWeight.Bold
            )

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

// Dragon Evolution Reward Screen
@Composable
private fun DragonEvolutionRewardContent(
    pet: PetEntity,
    fromStage: Int,
    toStage: Int,
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier
) {
    RewardEmphasisFrame(tier = emphasisTier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EvolutionTransitionAnimation(
                fromStage = fromStage,
                toStage = toStage,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            AnimatedEvolutionLabel(
                fromStage = fromStage,
                toStage = toStage,
                modifier = Modifier.graphicsLayer { translationY = -24f }
            )

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

/**
 * Multi-phase evolution transition animation that always plays on the reward screen.
 *
 * Sequence:
 * 1. Old form breathes (0-600ms)
 * 2. Glow builds around old form (600-1400ms)
 * 3. Flash dissolve - old form fades out with white flash (1400-2000ms)
 * 4. New form appears with scale-in + glow (2000-2800ms)
 * 5. New form settles with gentle idle (2800ms+)
 */
@Composable
private fun EvolutionTransitionAnimation(
    fromStage: Int,
    toStage: Int,
    modifier: Modifier = Modifier
) {
    // Timeline-driven animation phases
    var phase by remember(fromStage, toStage) { mutableStateOf(EvolutionPhase.IDLE_OLD) }

    val glowAlpha = remember { Animatable(0f) }
    val oldAlpha = remember { Animatable(1f) }
    val oldScale = remember { Animatable(1f) }
    val flashAlpha = remember { Animatable(0f) }
    val newAlpha = remember { Animatable(0f) }
    val newScale = remember { Animatable(0.6f) }
    val newGlowAlpha = remember { Animatable(0f) }

    // Idle breathing for whichever form is currently visible
    val infiniteTransition = rememberInfiniteTransition(label = "evo idle")
    val breathingScale = infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "evo breathing"
    )

    LaunchedEffect(fromStage, toStage) {
        // Phase 1: Old form breathing (idle) - just wait
        phase = EvolutionPhase.IDLE_OLD
        delay(600)

        // Phase 2: Glow builds around old form
        phase = EvolutionPhase.GLOW_BUILD
        glowAlpha.animateTo(
            1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
        delay(100)

        // Phase 3: Flash and dissolve old form
        phase = EvolutionPhase.FLASH_DISSOLVE
        flashAlpha.animateTo(
            1f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
        oldAlpha.animateTo(
            0f,
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        )
        oldScale.animateTo(
            1.25f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        glowAlpha.animateTo(
            0f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
        delay(50)

        // Phase 4: Reveal new form with scale-in
        phase = EvolutionPhase.REVEAL_NEW
        newAlpha.animateTo(
            1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
        newScale.animateTo(
            1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
        newGlowAlpha.animateTo(
            0.8f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        delay(200)
        flashAlpha.animateTo(
            0f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        newGlowAlpha.animateTo(
            0f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )

        // Phase 5: Settled
        phase = EvolutionPhase.SETTLED
    }

    val context = LocalContext.current
    val assetManager = context.assets

    val oldAssetPath = remember(fromStage) {
        AssetResolver.defaultAssetPath(assetManager, fromStage)
    }
    val newAssetPath = remember(toStage) {
        AssetResolver.defaultAssetPath(assetManager, toStage)
    }
    val oldPainter = rememberAssetPainter(oldAssetPath, "evolution old form")
    val newPainter = rememberAssetPainter(newAssetPath, "evolution new form")

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow behind old form
        if (glowAlpha.value > 0.01f && oldPainter != null) {
            Image(
                painter = oldPainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = oldScale.value * 1.15f
                        scaleY = oldScale.value * 1.15f
                        alpha = glowAlpha.value * 0.5f
                    }
                    .blur(20.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Old form
        if (oldAlpha.value > 0.01f && oldPainter != null) {
            Image(
                painter = oldPainter,
                contentDescription = ExpConfig.evolutionStageName(fromStage),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val idle = if (phase == EvolutionPhase.IDLE_OLD) breathingScale.value else 1f
                        scaleX = oldScale.value * idle
                        scaleY = oldScale.value * idle
                        alpha = oldAlpha.value
                    },
                contentScale = ContentScale.Fit
            )
        }

        // Radial glow flash overlay — bright center fading smoothly to transparent edges
        if (flashAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = flashAlpha.value * 0.85f
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = androidx.compose.ui.geometry.Offset.Unspecified,
                            radius = 200f
                        )
                    )
            )
        }

        // Glow behind new form
        if (newGlowAlpha.value > 0.01f && newPainter != null) {
            Image(
                painter = newPainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = newScale.value * 1.2f
                        scaleY = newScale.value * 1.2f
                        alpha = newGlowAlpha.value * 0.6f
                    }
                    .blur(24.dp),
                contentScale = ContentScale.Fit
            )
        }

        // New form
        if (newAlpha.value > 0.01f && newPainter != null) {
            Image(
                painter = newPainter,
                contentDescription = ExpConfig.evolutionStageName(toStage),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val idle = if (phase == EvolutionPhase.SETTLED) breathingScale.value else 1f
                        scaleX = newScale.value * idle
                        scaleY = newScale.value * idle
                        alpha = newAlpha.value
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}

private enum class EvolutionPhase {
    IDLE_OLD,
    GLOW_BUILD,
    FLASH_DISSOLVE,
    REVEAL_NEW,
    SETTLED
}

/**
 * Animated label showing "Old Stage → New Stage" with a smooth crossfade.
 */
@Composable
private fun AnimatedEvolutionLabel(
    fromStage: Int,
    toStage: Int,
    modifier: Modifier = Modifier
) {
    var showNewLabel by remember(fromStage, toStage) { mutableStateOf(false) }

    val fromName = ExpConfig.evolutionStageName(fromStage)
    val toName = ExpConfig.evolutionStageName(toStage)

    val labelAlpha by animateFloatAsState(
        targetValue = if (showNewLabel) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "evolution label alpha"
    )

    LaunchedEffect(fromStage, toStage) {
        delay(2000)
        showNewLabel = true
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = "Evolution Complete!",
            style = MaterialTheme.typography.headlineMedium,
            color = AppTheme.current.rewardText,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (!showNewLabel) {
            Text(
                text = fromName,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.current.rewardTextMuted,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (showNewLabel) {
            Text(
                text = toName,
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.current.rewardAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer { alpha = labelAlpha }
            )
        }
    }
}

