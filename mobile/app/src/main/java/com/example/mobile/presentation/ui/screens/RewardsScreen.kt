package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.UnlockSources
import com.example.mobile.presentation.ui.components.AssetPreview
import com.example.mobile.presentation.ui.components.CoinPill
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.ui.animations.HabitPetAnimations
import com.example.mobile.presentation.ui.animations.pressableScale
import com.example.mobile.presentation.ui.animations.staggeredListItem
import com.example.mobile.presentation.ui.components.GamifiedFixedHeader
import com.example.mobile.presentation.ui.components.LoadingStateCard
import com.example.mobile.presentation.ui.components.StreakCalendarOverlay
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.presentation.viewmodel.RewardsViewModel
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.DesignTokens
import com.example.mobile.ui.theme.HabitPetTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    rewardsViewModel: RewardsViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel,
    initialCollection: String? = null,
    onNavigateToRewardsLocked: () -> Unit
) {
    var selectedTypeTab by rememberSaveable { mutableStateOf(CollectionTypeTab.Outfits) }
    var selectedRarity by rememberSaveable { mutableStateOf<Rarity?>(null) }
    val initialCollectionTab = remember(initialCollection) { CollectionTab.fromRoute(initialCollection.orEmpty()) }
    var selectedCollection by rememberSaveable(initialCollection) { mutableStateOf(initialCollectionTab) }
    var activeInspectItem by remember { mutableStateOf<InventoryItemEntity?>(null) }
    val actionScope = rememberCoroutineScope()

    val items by selectedTypeTab.itemsFlow(rewardsViewModel).collectAsState(initial = emptyList())
    val isLoading by rewardsViewModel.isLoading.collectAsState()
    val error by rewardsViewModel.error.collectAsState(initial = null)
    val progressUiState by rewardsViewModel.uiState.collectAsState()
    val streakCalendarState by homeScreenViewModel.streakCalendarState.collectAsState()

    RewardsScreenContent(
        progressUiState = progressUiState,
        items = items,
        isLoading = isLoading,
        error = error,
        selectedTypeTab = selectedTypeTab,
        selectedCollection = selectedCollection,
        selectedRarity = selectedRarity,
        activeInspectItem = activeInspectItem,
        onNavigateToRewardsLocked = onNavigateToRewardsLocked,
        onStreakClick = homeScreenViewModel::openGlobalStreakCalendar,
        onStreakCalendarDismiss = homeScreenViewModel::closeStreakCalendar,
        onPreviousStreakMonth = homeScreenViewModel::showPreviousStreakMonth,
        onNextStreakMonth = homeScreenViewModel::showNextStreakMonth,
        streakCalendarState = streakCalendarState,
        onTypeSelected = {
            selectedTypeTab = it
            activeInspectItem = null
        },
        onCollectionSelected = {
            selectedCollection = it
            activeInspectItem = null
        },
        onRaritySelected = { selectedRarity = it },
        onInspect = { activeInspectItem = it },
        onCloseInspect = { activeInspectItem = null },
        onClearError = rewardsViewModel::clearError,
        onActionExecute = { item ->
            actionScope.launch {
                val equippedItemId = when (selectedTypeTab) {
                    CollectionTypeTab.Outfits -> progressUiState.pet.equippedOutfit
                    CollectionTypeTab.Backgrounds -> progressUiState.pet.equippedBackground
                    CollectionTypeTab.Auras -> progressUiState.pet.equippedAura
                }
                val isEquipped = equippedItemId == item.itemId
                val result = when {
                    isEquipped -> rewardsViewModel.unequipItem(item.type)
                    item.isPurchased -> rewardsViewModel.equipItem(item.type, item.itemId)
                    else -> rewardsViewModel.purchaseItem(item.id)
                }
                if (result > 0) {
                    activeInspectItem = null
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardsScreenContent(
    progressUiState: RewardsViewModel.UiState,
    items: List<InventoryItemEntity>,
    isLoading: Boolean,
    error: String?,
    selectedTypeTab: CollectionTypeTab,
    selectedCollection: CollectionTab,
    selectedRarity: Rarity?,
    activeInspectItem: InventoryItemEntity?,
    onNavigateToRewardsLocked: () -> Unit,
    onStreakClick: () -> Unit,
    onStreakCalendarDismiss: () -> Unit,
    onPreviousStreakMonth: () -> Unit,
    onNextStreakMonth: () -> Unit,
    streakCalendarState: StreakCalendarUiState?,
    onTypeSelected: (CollectionTypeTab) -> Unit,
    onCollectionSelected: (CollectionTab) -> Unit,
    onRaritySelected: (Rarity?) -> Unit,
    onInspect: (InventoryItemEntity) -> Unit,
    onCloseInspect: () -> Unit,
    onClearError: () -> Unit,
    onActionExecute: (InventoryItemEntity) -> Unit
) {
    val equippedItemId = remember(progressUiState.pet, selectedTypeTab) {
        when (selectedTypeTab) {
            CollectionTypeTab.Outfits -> progressUiState.pet.equippedOutfit
            CollectionTypeTab.Backgrounds -> progressUiState.pet.equippedBackground
            CollectionTypeTab.Auras -> progressUiState.pet.equippedAura
        }
    }

    val filteredItems = items.filter { item ->
        val collectionMatch = when (selectedCollection) {
            CollectionTab.Owned -> item.isPurchased
            CollectionTab.Locked -> !item.isPurchased
        }
        val rarityMatch = selectedRarity == null || item.rarity == selectedRarity
        collectionMatch && rarityMatch
    }

    Scaffold(
        containerColor = AppTheme.current.background,
        topBar = {
            GamifiedFixedHeader(
                streak = progressUiState.globalStreak,
                coins = progressUiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(progressUiState.pet.evolutionStage),
                streakCompletedToday = progressUiState.globalStreakCompletedToday,
                onCoinsClick = onNavigateToRewardsLocked,
                onStreakClick = onStreakClick
            )
        }
    ) { padding ->
        if (!error.isNullOrBlank()) {
            ErrorStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(DesignTokens.Section.horizontalPadding),
                message = error.orEmpty(),
                onRetry = onClearError
            )
        } else if (isLoading) {
            LoadingStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(DesignTokens.Section.horizontalPadding),
                message = "Opening the reward chest..."
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = DesignTokens.Section.horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.space12),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.space12),
                contentPadding = PaddingValues(top = DesignTokens.Section.topPadding, bottom = DesignTokens.space96)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CollectionTypeTabs(
                        selectedTypeTab = selectedTypeTab,
                        onTypeSelected = onTypeSelected
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    CollectionToggle(
                        selectedCollection = selectedCollection,
                        onSelected = onCollectionSelected
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    RarityFilter(
                        selectedRarity = selectedRarity,
                        onRaritySelected = onRaritySelected
                    )
                }

                if (filteredItems.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.padding(top = DesignTokens.space24)) {
                            EmptyStateCard(
                                title = when (selectedCollection) {
                                    CollectionTab.Owned -> "Vault is Empty"
                                    CollectionTab.Locked -> "All Items Discovered"
                                },
                                message = when (selectedCollection) {
                                    CollectionTab.Owned -> "Acquire rare customizable content from the locked items catalog tab."
                                    CollectionTab.Locked -> "Check back later as your dragon grows into newer evolutionary tiers."
                                },
                                hint = "Earn gold by maintaining your daily task streaks.",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                gridItemsIndexed(
                    items = filteredItems,
                    key = { _, item -> item.id }
                ) { index, item ->
                    InventoryItemGridSquare(
                        item = item,
                        isSelected = activeInspectItem?.id == item.id,
                        isEquipped = equippedItemId == item.itemId,
                        onClick = { onInspect(item) },
                        modifier = Modifier.staggeredListItem(index)
                    )
                }
            }

            activeInspectItem?.let { item ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ItemInspectDrawer(
                        item = item,
                        currentWalletBalance = progressUiState.totalCoins,
                        isEquipped = equippedItemId == item.itemId,
                        onClose = onCloseInspect,
                        onActionExecute = { onActionExecute(item) }
                    )
                }
            }
        }

        StreakCalendarOverlay(
            state = streakCalendarState,
            onDismiss = onStreakCalendarDismiss,
            onPreviousMonth = onPreviousStreakMonth,
            onNextMonth = onNextStreakMonth
        )
    }
}

}

@Composable
private fun InventoryItemGridSquare(
    item: InventoryItemEntity,
    isSelected: Boolean,
    isEquipped: Boolean = item.isEquipped,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = rarityColor(item.rarity)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .border(
                width = if (isSelected) 3.dp else if (isEquipped) DesignTokens.strokeThick else 0.dp,
                color = if (isSelected) AppTheme.current.violet else if (isEquipped) AppTheme.current.mint else Color.Transparent,
                shape = DesignTokens.cardCorner
            )
            .clickable(onClick = onClick)
            .pressableScale(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
        shape = DesignTokens.cardCorner,
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPurchased && !isSelected) DesignTokens.elevationSm else DesignTokens.elevationNone)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.Card.iconSizeLg)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(tierColor.copy(alpha = DesignTokens.alpha20), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            // ✅ Uses the updated entity item parameter references
            AssetPreview(
                itemType = item.type,
                itemId = item.itemId,
                imageUrl = item.imageUrl,
                tintColor = tierColor,
                modifier = Modifier.fillMaxSize().padding(DesignTokens.space14)
            )

            if (!item.isPurchased) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.current.ink.copy(alpha = DesignTokens.alpha8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = AppTheme.current.onPrimary.copy(alpha = DesignTokens.alpha60),
                        modifier = Modifier.size(DesignTokens.Icon.sizeXl)
                    )
                }
            } else if (isEquipped) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(DesignTokens.space8)
                        .size(DesignTokens.Icon.sizeXs)
                        .background(AppTheme.current.mint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppTheme.current.onPrimary,
                        modifier = Modifier.size(DesignTokens.space10)
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemInspectDrawer(
    item: InventoryItemEntity,
    currentWalletBalance: Int,
    isEquipped: Boolean = item.isEquipped,
    onClose: () -> Unit,
    onActionExecute: () -> Unit
) {
    val tierColor = rarityColor(item.rarity)
    val isPurchasable = item.unlockSource == UnlockSources.SHOP && item.price > 0
    val canAfford = isPurchasable && currentWalletBalance >= item.price
    val unavailableLabel = when (item.unlockSource) {
        UnlockSources.CHEST -> "Chest Reward"
        UnlockSources.ACHIEVEMENT -> "Achievement Reward"
        else -> "Not Available In Shop"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Card.padding, vertical = DesignTokens.Card.padding),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
        shape = DesignTokens.cardCornerRounded,
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Section.horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Card.padding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.space2)) {
                    Surface(
                        shape = DesignTokens.cardCornerCircle,
                        color = tierColor.copy(alpha = DesignTokens.alpha12)
                    ) {
                        Text(
                            text = item.rarity.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = DesignTokens.Badge.paddingHorizontal, vertical = DesignTokens.Badge.paddingVertical)
                        )
                    }
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.ink
                    )
                }

                TextButton(onClick = onClose) {
                    Text("Close", color = AppTheme.current.muted)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Source", style = MaterialTheme.typography.labelMedium, color = AppTheme.current.muted)
                    Text(
                        text = item.unlockSource.ifBlank { "Standard Shop Asset" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AppTheme.current.ink
                    )
                }

                if (!item.isPurchased && isPurchasable) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.space4)
                    ) {
                        CoinPill(amount = item.price)
                    }
                }
            }

            Button(
                onClick = onActionExecute,
                enabled = isEquipped || item.isPurchased || isPurchasable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isEquipped -> AppTheme.current.ink.copy(alpha = DesignTokens.alpha8)
                        item.isPurchased -> AppTheme.current.violet
                        isPurchasable -> AppTheme.current.amber
                        else -> AppTheme.current.muted
                    },
                    contentColor = if (isEquipped) AppTheme.current.ink else AppTheme.current.onPrimary
                ),
                shape = DesignTokens.cardCornerCircle,
                modifier = Modifier.fillMaxWidth().height(DesignTokens.Button.heightSm)
            ) {
                Text(
                    text = when {
                        isEquipped -> "Unequip Item"
                        item.isPurchased -> "Equip Customization"
                        canAfford -> "Unlock Reward"
                        isPurchasable -> "Insufficient Gold Balance"
                        else -> unavailableLabel
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CollectionTypeTabs(
    selectedTypeTab: CollectionTypeTab,
    onTypeSelected: (CollectionTypeTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.current.ink.copy(alpha = DesignTokens.alpha4), DesignTokens.cardCornerCircle)
            .padding(DesignTokens.space4),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.space4)
    ) {
        CollectionTypeTab.values().forEach { tab ->
            val isSelected = selectedTypeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(DesignTokens.cardCornerCircle)
                    .background(if (isSelected) AppTheme.current.violet else Color.Transparent)
                    .clickable { onTypeSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.space4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) AppTheme.current.onPrimary else AppTheme.current.muted,
                        modifier = Modifier.size(DesignTokens.Icon.sizeXs)
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) AppTheme.current.onPrimary else AppTheme.current.muted
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionToggle(
    selectedCollection: CollectionTab,
    onSelected: (CollectionTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.space10)
    ) {
        CollectionTab.values().forEach { tab ->
            val isSelected = selectedCollection == tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clickable { onSelected(tab) },
                shape = DesignTokens.cardCornerSm,
                color = if (isSelected) AppTheme.current.violet else AppTheme.current.violet.copy(alpha = DesignTokens.alpha8),
                border = borderStrokeFix(isSelected)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${tab.label} Inventory",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) AppTheme.current.onPrimary else AppTheme.current.violet
                    )
                }
            }
        }
    }
}

@Composable
private fun RarityFilter(
    selectedRarity: Rarity?,
    onRaritySelected: (Rarity?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.space6)
    ) {
        RarityChip(
            label = "All",
            chipColor = AppTheme.current.muted,
            isSelected = selectedRarity == null,
            onClick = { onRaritySelected(null) },
            modifier = Modifier.weight(1f)
        )
        Rarity.values().forEach { rarity ->
            RarityChip(
                label = rarity.name.lowercase(),
                chipColor = rarityColor(rarity),
                isSelected = selectedRarity == rarity,
                onClick = { onRaritySelected(rarity) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RarityChip(
    label: String,
    chipColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable(onClick = onClick),
        shape = DesignTokens.cardCornerCircle,
        color = if (isSelected) chipColor else chipColor.copy(alpha = DesignTokens.alpha16),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(DesignTokens.strokeMedium, chipColor) else androidx.compose.foundation.BorderStroke(DesignTokens.strokeThin, chipColor.copy(alpha = DesignTokens.alpha28))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) AppTheme.current.onPrimary else chipColor
            )
        }
    }
}


private fun borderStrokeFix(selected: Boolean, color: Color = AppTheme.current.violet) =
    if (selected) androidx.compose.foundation.BorderStroke(DesignTokens.strokeMedium, color) else null

private fun CollectionTypeTab.itemsFlow(viewModel: RewardsViewModel) = when (this) {
    CollectionTypeTab.Outfits -> viewModel.outfits
    CollectionTypeTab.Backgrounds -> viewModel.backgrounds
    CollectionTypeTab.Auras -> viewModel.auras
}

private fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.NORMAL -> AppTheme.current.violetMuted
    Rarity.RARE -> AppTheme.current.mint
    Rarity.EPIC -> AppTheme.current.blue
    Rarity.LEGENDARY -> AppTheme.current.purple
}

private enum class CollectionTypeTab(val label: String, val icon: ImageVector) {
    Outfits("Outfits", Icons.Default.AutoAwesome),
    Backgrounds("Scenes", Icons.Default.Pets),
    Auras("Auras", Icons.Default.Star)
}

private enum class CollectionTab(val label: String) {
    Owned("Owned"),
    Locked("Locked");

    companion object {
        fun fromRoute(value: String): CollectionTab = when (value.lowercase()) {
            "locked" -> Locked
            "owned" -> Owned
            else -> Owned
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390px,height=844px,dpi=420")
@Composable
private fun RewardsScreenPreview() {
    val pet = PetEntity(
        id = 1,
        name = "Luna",
        level = 3,
        xp = 180,
        evolutionStage = 1,
        equippedOutfit = "classic_blue_outfit",
        equippedBackground = "misty_meadow_background",
        equippedAura = null,
        mood = "Calm"
    )
    HabitPetTheme {
        RewardsScreenContent(
            progressUiState = RewardsViewModel.UiState(
                globalStreak = 4,
                globalStreakCompletedToday = false,
                totalCoins = 240,
                pet = pet
            ),
            items = listOf(
                InventoryItemEntity(
                    id = 1,
                    itemId = "classic_blue_outfit",
                    name = "Classic Blue Outfit",
                    type = CustomizationTypes.OUTFIT,
                    imageUrl = "",
                    isUnlocked = true,
                    isPurchased = true,
                    isEquipped = true,
                    price = 80,
                    rarity = Rarity.NORMAL,
                    unlockSource = UnlockSources.SHOP
                ),
                InventoryItemEntity(
                    id = 2,
                    itemId = "misty_meadow_background",
                    name = "Misty Meadow Scene",
                    type = CustomizationTypes.BACKGROUND,
                    imageUrl = "",
                    isUnlocked = true,
                    isPurchased = true,
                    isEquipped = true,
                    price = 120,
                    rarity = Rarity.RARE,
                    unlockSource = UnlockSources.SHOP
                ),
                InventoryItemEntity(
                    id = 3,
                    itemId = "aurora_pulse_aura",
                    name = "Aurora Pulse Aura",
                    type = CustomizationTypes.AURA,
                    imageUrl = "",
                    isUnlocked = false,
                    isPurchased = false,
                    isEquipped = false,
                    price = 0,
                    rarity = Rarity.EPIC,
                    unlockSource = UnlockSources.CHEST
                )
            ),
            isLoading = false,
            error = null,
            selectedTypeTab = CollectionTypeTab.Outfits,
            selectedCollection = CollectionTab.Owned,
            selectedRarity = null,
            activeInspectItem = null,
            onNavigateToRewardsLocked = {},
            onStreakClick = {},
            onStreakCalendarDismiss = {},
            onPreviousStreakMonth = {},
            onNextStreakMonth = {},
            streakCalendarState = null,
            onTypeSelected = {},
            onCollectionSelected = {},
            onRaritySelected = {},
            onInspect = {},
            onCloseInspect = {},
            onClearError = {},
            onActionExecute = {}
        )
    }
    }
