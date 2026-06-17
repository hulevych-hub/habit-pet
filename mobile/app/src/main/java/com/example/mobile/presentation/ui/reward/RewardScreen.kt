package com.example.mobile.presentation.ui.reward

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile.R
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ChestType
import com.example.mobile.domain.EquipableConfig
import com.example.mobile.presentation.ui.components.PetPhaseTransition
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.util.ReinforcementMessageProvider
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.domain.AchievementReward as ConfigAchievementReward

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
            .clickable { onRewardCompleted() }
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
                    emphasisTier = emphasisTier,
                    onConfirm = onRewardCompleted
                )

                is RewardUiEvent.StreakReward -> StreakRewardContent(
                    streak = reward.streak,
                    coinsEarned = reward.coins,
                    rewardSummary = reward.rewardSummary,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier,
                    onConfirm = onRewardCompleted
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
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier
                )

                is RewardUiEvent.AchievementReward -> AchievementRewardContent(
                    achievementName = reward.achievementName,
                    coinsEarned = reward.coins,
                    expAmount = reward.expAmount,
                    chestType = reward.chestType,
                    rewards = reward.rewards,
                    reinforcementMessage = reinforcementMessage,
                    emphasisTier = emphasisTier
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

            Text(
                text = "LEVEL UP!",
                style = MaterialTheme.typography.headlineLarge,
                color = AppTheme.current.rewardText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(AppTheme.current.blue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.toString(),
                    color = AppTheme.current.rewardText,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "+$coinsEarned coins",
                color = AppTheme.current.mint
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
                color = AppTheme.current.rewardText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(AppTheme.current.blue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AppTheme.current.rewardText,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "+$amount XP",
                color = AppTheme.current.mint,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

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
    val name = EquipableConfig.definition(equipableId)?.name ?: "Customization"

    RewardEmphasisFrame(tier = emphasisTier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NEW LOOK!",
                style = MaterialTheme.typography.headlineLarge,
                color = AppTheme.current.rewardText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(AppTheme.current.purple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(2).uppercase(),
                    color = AppTheme.current.rewardText,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                color = AppTheme.current.mint,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

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
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier
) {
    var isOpen by remember { mutableStateOf(false) }

    RewardEmphasisFrame(tier = emphasisTier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            val imageRes = if (isOpen) {
                R.drawable.chest_open
            } else {
                R.drawable.chest_closed // ensure this exists in res/drawable
            }

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(emphasisTier.chestTint),
                modifier = Modifier
                    .size((180 * emphasisTier.chestSizeMultiplier).dp)
                    .clickable { isOpen = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isOpen) {
                val rewardText = mutableListOf<String>()

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

                if (rewardText.isNotEmpty()) {
                    Text(
                        text = rewardText.joinToString("\n"),
                        color = emphasisTier.rewardColor,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Text(
                    text = rewardType,
                    color = AppTheme.current.rewardText.copy(alpha = 0.7f)
                )
            }

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

// Streak Reward Screen
@Composable
private fun StreakRewardContent(
    streak: Int,
    coinsEarned: Int,
    rewardSummary: List<String> = emptyList(),
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier,
    onConfirm: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak hearts")
    val mainScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "main heart scale"
    )
    val mainFloat = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "main heart float"
    )
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
            .fillMaxSize()
            .clickable { onConfirm() },
        contentAlignment = Alignment.Center
    ) {
        HeartOrbit(infiniteTransition, emphasisTier)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Streak celebration",
                tint = AppTheme.current.danger,
                modifier = Modifier
                    .size((96 * mainScale.value * emphasisTier.rewardScale).dp)
                    .offset(y = (mainFloat.value * emphasisTier.rewardScale).dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "GLOBAL STREAK!",
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.current.gold
            )

            Text(
                text = "$streak DAY STREAK",
                style = MaterialTheme.typography.headlineLarge,
                color = AppTheme.current.rewardText
            )

            Spacer(modifier = Modifier.height(24.dp))

            displayedSummary.forEach { rewardLine ->
                Text(
                    text = rewardLine,
                    color = AppTheme.current.mint,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

@Composable
private fun HeartOrbit(
    infiniteTransition: InfiniteTransition,
    emphasisTier: RewardEmphasisTier
) {
    repeat(8) { index ->
        val angle = (index * 45).toDouble() * (kotlin.math.PI / 180.0)
        val x = (cos(angle) * 128 * emphasisTier.rewardScale).dp
        val y = (sin(angle) * 86 * emphasisTier.rewardScale).dp
        val scale = infiniteTransition.animateFloat(
            initialValue = 0.65f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(700 + index * 70, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbit heart scale $index"
        )
        val alpha = infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(900 + index * 60, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbit heart alpha $index"
        )

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = AppTheme.current.danger.copy(alpha = alpha.value),
            modifier = Modifier
                .offset(x = x, y = y)
                .size((30 * scale.value).dp)
        )
    }
}

// Achievement Reward Screen
@Composable
private fun AchievementRewardContent(
    achievementName: String,
    coinsEarned: Int,
    expAmount: Int = 0,
    chestType: String? = null,
    rewards: List<ConfigAchievementReward> = emptyList(),
    reinforcementMessage: String,
    emphasisTier: RewardEmphasisTier
) {
    val rewardText = if (rewards.isNotEmpty()) {
        rewards.map { it.rewardLabel() }
    } else {
        buildLegacyRewardText(coinsEarned, expAmount, chestType)
    }

    RewardEmphasisFrame(tier = emphasisTier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = emphasisTier.rewardColor,
                modifier = Modifier.size((64 * emphasisTier.rewardScale).dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ACHIEVEMENT UNLOCKED",
                color = AppTheme.current.rewardText
            )

            Text(
                text = achievementName,
                color = AppTheme.current.blue,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = rewardText.joinToString("\n"),
                color = emphasisTier.rewardColor
            )

            ReinforcementMessage(reinforcementMessage)
        }
    }
}

private fun buildLegacyRewardText(
    coinsEarned: Int,
    expAmount: Int,
    chestType: String?
): List<String> {
    val rewardText = mutableListOf("+${coinsEarned} coins")

    if (expAmount > 0) {
        rewardText.add("+$expAmount EXP")
    }

    if (!chestType.isNullOrBlank()) {
        val label = chestType.lowercase().replaceFirstChar { it.uppercase() }
        rewardText.add("$label chest")
    }

    return rewardText
}

private fun ConfigAchievementReward.rewardLabel(): String = when (this) {
    is ConfigAchievementReward.CoinReward -> "+$amount coins"
    is ConfigAchievementReward.ExpReward -> "+$amount EXP"
    is ConfigAchievementReward.ChestReward -> {
        val label = chestType.name.replaceFirstChar { it.uppercase() }
        "$label chest"
    }
    is ConfigAchievementReward.CustomizationReward ->
        EquipableConfig.definition(equipableId)?.name ?: "Customization"
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
    is RewardUiEvent.AchievementReward -> if (
        chestType?.contains("legendary", ignoreCase = true) == true ||
        rewards.any { it is ConfigAchievementReward.ChestReward && it.chestType == ChestType.LEGENDARY }
    ) {
        RewardEmphasisTier.EPIC
    } else {
        RewardEmphasisTier.RARE
    }
}

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
    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = message,
        color = AppTheme.current.rewardText.copy(alpha = 0.88f),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Tap to continue",
        color = AppTheme.current.rewardText.copy(alpha = 0.7f),
        style = MaterialTheme.typography.labelMedium
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

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AppTheme.current.gold,
                modifier = Modifier.size((64 * emphasisTier.rewardScale).dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "+$amount",
                style = MaterialTheme.typography.headlineLarge,
                color = AppTheme.current.mint
            )

            Text(
                text = "Coins earned",
                color = AppTheme.current.rewardText.copy(alpha = 0.8f)
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
    emphasisTier: RewardEmphasisTier,
    onConfirm: () -> Unit
) {
    var isTransitionComplete by remember { mutableStateOf(false) }

    RewardEmphasisFrame(tier = emphasisTier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .clickable {
                    if (isTransitionComplete) {
                        onConfirm()
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PetPhaseTransition(
                pet = pet.copy(evolutionStage = toStage),
                fromStage = fromStage,
                toStage = toStage,
                onTransitionCompleted = { isTransitionComplete = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isTransitionComplete) "Evolution Complete!" else "Watch your dragon evolve",
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.current.rewardText
            )

            ReinforcementMessage(reinforcementMessage)

            if (!isTransitionComplete) {
                Text(
                    text = "Tap after the transformation to continue",
                    color = AppTheme.current.rewardText.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

