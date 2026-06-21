package com.example.mobile.presentation.ui.reward

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dotlottie.dlplayer.Mode
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ChestType
import com.example.mobile.domain.EquipableConfig
import com.example.mobile.presentation.ui.components.AssetPreview
import com.example.mobile.presentation.ui.components.PetPhaseTransition
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.ui.theme.AppTheme
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppTheme.current.background,
                        AppTheme.current.surfaceVariant,
                        AppTheme.current.background
                    )
                )
            )
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
        tint = AppTheme.current.primary,
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

            Spacer(modifier = Modifier.height((-10).dp))

            Text(
                text = "LEVEL $level",
                color = AppTheme.current.primary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "+$coinsEarned coins",
                color = AppTheme.current.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
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
            Text(
                text = "XP BOOST!",
                style = MaterialTheme.typography.headlineLarge,
                color = AppTheme.current.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            XpBoostAnimation(
                modifier = Modifier.size((120 * emphasisTier.rewardScale * REWARD_LOTTIE_SIZE_MULTIPLIER).dp)
            )

            Spacer(modifier = Modifier.height((-10).dp))

            Text(
                text = "+$amount XP",
                color = AppTheme.current.primary,
                style = MaterialTheme.typography.headlineSmall,
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
            Text(
                text = "NEW LOOK!",
                style = MaterialTheme.typography.headlineLarge,
                color = AppTheme.current.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            definition?.let {
                AssetPreview(
                    itemType = definition.type.value,
                    itemId = definition.id,
                    imageUrl = definition.imageUrl,
                    tintColor = emphasisTier.rewardColor,
                    modifier = Modifier.size((120 * emphasisTier.rewardScale * REWARD_LOTTIE_SIZE_MULTIPLIER).dp)
                )
            }

            Spacer(modifier = Modifier.height((-10).dp))

            Text(
                text = name,
                color = AppTheme.current.primary,
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
                        val rewardText = mutableListOf<String>()
                        val customizationDefinition = equipableId?.let { EquipableConfig.definition(it) }

                        when (amount) {
                            is Int -> if (amount > 0) rewardText.add("+$amount coins")
                            is String -> if (!amount.isEmpty()) rewardText.add(amount)
                        }

                        if (expAmount > 0) {
                            rewardText.add("+$expAmount EXP")
                        }

                        if (customizationId != null || equipableId != null) {
                            val equipableName = equipableId
                                ?.let { EquipableConfig.definition(it)?.name }
                                ?: "Customization"
                            rewardText.add("$equipableName unlocked!")
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
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

                            if (rewardText.isNotEmpty()) {
                                Text(
                                    text = rewardText.joinToString("\n"),
                                    color = AppTheme.current.primary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = rewardType,
                                color = AppTheme.current.primary.copy(alpha = 0.78f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (areRewardsVisible) {
                    ReinforcementMessage(reinforcementMessage)
                } else {
                    Text(
                        text = "Tap anywhere to open",
                        color = AppTheme.current.primary.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
            "$streak Day Streak",
            if (coinsEarned > 0) "+$coinsEarned coins" else "Reward chest unlocked"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        RewardDotLottie(
            source = STREAK_LOTTIE_URL,
            loop = true,
            modifier = Modifier.size((150 * emphasisTier.rewardScale * REWARD_LOTTIE_SIZE_MULTIPLIER).dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height((-12).dp))

            Text(
                text = "GLOBAL STREAK!",
                style = MaterialTheme.typography.headlineSmall,
                color = AppTheme.current.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$streak DAY STREAK",
                style = MaterialTheme.typography.headlineLarge,
                color = AppTheme.current.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            displayedSummary.forEach { rewardLine ->
                Text(
                    text = rewardLine,
                    color = AppTheme.current.primary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            ReinforcementMessage(reinforcementMessage)
        }
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
    val glowAlpha by animateFloatAsState(
        targetValue = if (emphasisActive) 0.28f else 0f,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "reward emphasis glow"
    )
    val elevation = if (emphasisActive) (24 * tier.rewardScale).dp else 8.dp

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                clip = false
            )
            .drawWithContent {
                if (glowAlpha > 0.01f) {
                    drawCircle(
                        color = tier.glowColor.copy(alpha = glowAlpha),
                        radius = size.maxDimension * tier.chestSizeMultiplier
                    )
                }
                drawContent()
            }
    ) {
        content()
    }
}

@Composable
private fun ReinforcementMessage(message: String) {
    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = message,
        color = AppTheme.current.primary.copy(alpha = 0.92f),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Tap to continue",
        color = AppTheme.current.primary.copy(alpha = 0.72f),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
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

            Spacer(modifier = Modifier.height((-10).dp))

            Text(
                text = "+$amount",
                style = MaterialTheme.typography.headlineLarge,
                color = AppTheme.current.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Coins earned",
                color = AppTheme.current.primary.copy(alpha = 0.88f),
                style = MaterialTheme.typography.titleMedium,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PetPhaseTransition(
                pet = pet.copy(evolutionStage = toStage),
                fromStage = fromStage,
                toStage = toStage
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Evolution Complete!",
                style = MaterialTheme.typography.headlineSmall,
                color = AppTheme.current.primary,
                fontWeight = FontWeight.Bold
            )

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

