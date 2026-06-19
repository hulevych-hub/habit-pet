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
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.UnlockSources
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.presentation.ui.components.AssetPreview
import com.example.mobile.presentation.ui.components.CoinPill
import com.example.mobile.presentation.ui.components.GamifiedFixedHeader
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.ui.components.LoadingStateCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.lazy.grid.items as gridItems

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val inventoryItemRepository: InventoryItemRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    val outfits = inventoryItemRepository.getItemsByType(CustomizationTypes.OUTFIT)
    val backgrounds = inventoryItemRepository.getItemsByType(CustomizationTypes.BACKGROUND)
    val auras = inventoryItemRepository.getItemsByType(CustomizationTypes.AURA)

    init {
        viewModelScope.launch {
            combine(outfits, backgrounds, auras) { _, _, _ -> }
                .collectLatest { _isLoading.value = false }
        }
    }

    suspend fun purchaseItem(itemId: Long): Int = try {
        _error.value = null
        inventoryItemRepository.purchaseItem(itemId)
    } catch (e: Exception) {
        _error.value = e.message ?: "Reward could not be claimed"
        -1
    }

    suspend fun equipItem(itemType: String, itemId: String): Int = try {
        _error.value = null
        petRepository.equipItem(itemType, itemId)
    } catch (e: Exception) {
        _error.value = e.message ?: "Reward could not be equipped"
        -1
    }

    suspend fun unequipItem(itemType: String): Int = try {
        _error.value = null
        petRepository.unequipItem(itemType)
    } catch (e: Exception) {
        _error.value = e.message ?: "Reward could not be unequipped"
        -1
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    rewardsViewModel: RewardsViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
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
    val progressUiState by homeScreenViewModel.uiState.collectAsState()

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
                onCoinsClick = onNavigateToRewardsLocked
            )
        }
    ) { padding ->
        if (!error.isNullOrBlank()) {
            ErrorStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                message = error.orEmpty(),
                onRetry = rewardsViewModel::clearError
            )
        } else if (isLoading) {
            LoadingStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                message = "Opening the reward chest..."
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CollectionTypeTabs(
                        selectedTypeTab = selectedTypeTab,
                        onTypeSelected = {
                            selectedTypeTab = it
                            activeInspectItem = null
                        }
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    CollectionToggle(
                        selectedCollection = selectedCollection,
                        onSelected = {
                            selectedCollection = it
                            activeInspectItem = null
                        }
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    RarityFilter(
                        selectedRarity = selectedRarity,
                        onRaritySelected = { selectedRarity = it }
                    )
                }

                if (filteredItems.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.padding(top = 24.dp)) {
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

                gridItems(
                    items = filteredItems,
                    key = { it.id }
                ) { item ->
                    InventoryItemGridSquare(
                        item = item,
                        isSelected = activeInspectItem?.id == item.id,
                        isEquipped = equippedItemId == item.itemId,
                        onClick = { activeInspectItem = item }
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
                        onClose = { activeInspectItem = null },
                        onActionExecute = {
                            actionScope.launch {
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
            }
        }
    }
}
}

@Composable
private fun InventoryItemGridSquare(
    item: InventoryItemEntity,
    isSelected: Boolean,
    isEquipped: Boolean = item.isEquipped,
    onClick: () -> Unit
) {
    val tierColor = rarityColor(item.rarity)

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .border(
                width = if (isSelected) 3.dp else if (isEquipped) 2.dp else 0.dp,
                color = if (isSelected) AppTheme.current.violet else if (isEquipped) AppTheme.current.mint else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPurchased && !isSelected) 1.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(tierColor.copy(alpha = 0.2f), Color.Transparent)
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
                modifier = Modifier.fillMaxSize().padding(14.dp)
            )

            if (!item.isPurchased) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.current.ink.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = AppTheme.current.onPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else if (isEquipped) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp)
                        .background(AppTheme.current.mint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppTheme.current.onPrimary,
                        modifier = Modifier.size(10.dp)
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
            .padding(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = tierColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = item.rarity.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                        isEquipped -> AppTheme.current.ink.copy(alpha = 0.08f)
                        item.isPurchased -> AppTheme.current.violet
                        isPurchasable -> AppTheme.current.amber
                        else -> AppTheme.current.muted
                    },
                    contentColor = if (isEquipped) AppTheme.current.ink else AppTheme.current.onPrimary
                ),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
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
            .background(AppTheme.current.ink.copy(alpha = 0.04f), RoundedCornerShape(999.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CollectionTypeTab.values().forEach { tab ->
            val isSelected = selectedTypeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) AppTheme.current.violet else Color.Transparent)
                    .clickable { onTypeSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) AppTheme.current.onPrimary else AppTheme.current.muted,
                        modifier = Modifier.size(15.dp)
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
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CollectionTab.values().forEach { tab ->
            val isSelected = selectedCollection == tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clickable { onSelected(tab) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) AppTheme.current.violet else AppTheme.current.violet.copy(alpha = 0.08f),
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
        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
        shape = RoundedCornerShape(999.dp),
        color = if (isSelected) chipColor else chipColor.copy(alpha = 0.16f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, chipColor) else androidx.compose.foundation.BorderStroke(1.dp, chipColor.copy(alpha = 0.28f))
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
    if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null

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
