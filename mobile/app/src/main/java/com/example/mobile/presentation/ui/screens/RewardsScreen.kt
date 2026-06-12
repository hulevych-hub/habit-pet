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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.presentation.ui.components.AssetPreview
import com.example.mobile.presentation.ui.components.EmptyStateCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.lazy.grid.items as gridItems

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val inventoryItemRepository: InventoryItemRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    val outfits = inventoryItemRepository.getItemsByType(CustomizationTypes.OUTFIT)
    val backgrounds = inventoryItemRepository.getItemsByType(CustomizationTypes.BACKGROUND)
    val auras = inventoryItemRepository.getItemsByType(CustomizationTypes.AURA)

    suspend fun purchaseItem(itemId: Long): Int = inventoryItemRepository.purchaseItem(itemId)

    suspend fun equipItem(itemType: String, itemId: String): Int = petRepository.equipItem(itemType, itemId)

    suspend fun unequipItem(itemType: String): Int = petRepository.unequipItem(itemType)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    rewardsViewModel: RewardsViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    var selectedTypeTab by rememberSaveable { mutableStateOf(CollectionTypeTab.Outfits) }
    var selectedRarity by rememberSaveable { mutableStateOf<Rarity?>(null) }
    var selectedCollection by rememberSaveable { mutableStateOf(CollectionTab.Owned) }
    var activeInspectItem by remember { mutableStateOf<InventoryItemEntity?>(null) }
    val actionScope = rememberCoroutineScope()

    val items by selectedTypeTab.itemsFlow(rewardsViewModel).collectAsState(initial = emptyList())
    val progressUiState by homeScreenViewModel.uiState.collectAsState()

    val filteredItems = items.filter { item ->
        val collectionMatch = when (selectedCollection) {
            CollectionTab.Owned -> item.isPurchased
            CollectionTab.Locked -> !item.isPurchased
        }
        val rarityMatch = selectedRarity == null || item.rarity == selectedRarity
        collectionMatch && rarityMatch
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFC),
        topBar = {
            GamifiedFixedHeader(
                streak = progressUiState.globalStreak,
                coins = progressUiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(progressUiState.pet.evolutionStage)
            )
        }
    ) { padding ->
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
                        onClose = { activeInspectItem = null },
                        onActionExecute = {
                            actionScope.launch {
                                val result = when {
                                    item.isEquipped -> rewardsViewModel.unequipItem(item.type)
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

@Composable
private fun InventoryItemGridSquare(
    item: InventoryItemEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tierColor = rarityColor(item.rarity)

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .border(
                width = if (isSelected) 3.dp else if (item.isEquipped) 2.dp else 0.dp,
                color = if (isSelected) ColorPaletteRewards.Violet else if (item.isEquipped) ColorPaletteRewards.Mint else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteRewards.Card),
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
                        .background(ColorPaletteRewards.Ink.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else if (item.isEquipped) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp)
                        .background(ColorPaletteRewards.Mint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
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
    onClose: () -> Unit,
    onActionExecute: () -> Unit
) {
    val tierColor = rarityColor(item.rarity)
    val canAfford = currentWalletBalance >= item.price

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteRewards.Card),
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
                        color = ColorPaletteRewards.Ink
                    )
                }

                TextButton(onClick = onClose) {
                    Text("Close", color = ColorPaletteRewards.Muted)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Source", style = MaterialTheme.typography.labelMedium, color = ColorPaletteRewards.Muted)
                    Text(
                        text = item.unlockSource.ifBlank { "Standard Shop Asset" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = ColorPaletteRewards.Ink
                    )
                }

                if (!item.isPurchased) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = ColorPaletteRewards.Amber, modifier = Modifier.size(18.dp))
                        Text(
                            text = "${item.price}g",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorPaletteRewards.Amber
                        )
                    }
                }
            }

            Button(
                onClick = onActionExecute,
                enabled = item.isPurchased || canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        item.isEquipped -> ColorPaletteRewards.Ink.copy(alpha = 0.08f)
                        item.isPurchased -> ColorPaletteRewards.Violet
                        else -> ColorPaletteRewards.Amber
                    },
                    contentColor = if (item.isEquipped) ColorPaletteRewards.Ink else Color.White
                ),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = when {
                        item.isEquipped -> "Unequip Item"
                        item.isPurchased -> "Equip Customization"
                        canAfford -> "Unlock Reward"
                        else -> "Insufficient Gold Balance"
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
            .background(ColorPaletteRewards.Ink.copy(alpha = 0.04f), RoundedCornerShape(999.dp))
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
                    .background(if (isSelected) ColorPaletteRewards.Violet else Color.Transparent)
                    .clickable { onTypeSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color.White else ColorPaletteRewards.Muted
                )
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
                color = if (isSelected) ColorPaletteRewards.Violet.copy(alpha = 0.12f) else ColorPaletteRewards.Card,
                border = borderStrokeFix(isSelected)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${tab.label} Inventory",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) ColorPaletteRewards.Violet else ColorPaletteRewards.Muted
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
            chipColor = ColorPaletteRewards.Muted,
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
            .height(34.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (isSelected) chipColor.copy(alpha = 0.15f) else Color.Transparent,
        border = borderStrokeFix(isSelected, chipColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) chipColor else ColorPaletteRewards.Muted.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GamifiedFixedHeader(
    streak: Int,
    coins: Int,
    stageName: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = ColorPaletteRewards.Honey,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$streak d",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteRewards.Ink
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ColorPaletteRewards.Violet.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = ColorPaletteRewards.Violet,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stageName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteRewards.Violet
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Coins",
                    tint = ColorPaletteRewards.Amber,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteRewards.Ink
                )
            }
        }
    }
}

private fun borderStrokeFix(selected: Boolean, color: Color = ColorPaletteRewards.Violet) =
    if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null

private fun CollectionTypeTab.itemsFlow(viewModel: RewardsViewModel) = when (this) {
    CollectionTypeTab.Outfits -> viewModel.outfits
    CollectionTypeTab.Backgrounds -> viewModel.backgrounds
    CollectionTypeTab.Auras -> viewModel.auras
}

private fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.NORMAL -> Color(0xFF6F6A8A)
    Rarity.RARE -> Color(0xFF3B91FF)
    Rarity.EPIC -> Color(0xFFA14BFF)
    Rarity.LEGENDARY -> Color(0xFFFF9F1C)
}

private enum class CollectionTypeTab(val label: String) {
    Outfits("Outfits"),
    Backgrounds("Scenes"),
    Auras("Auras")
}

private enum class CollectionTab(val label: String) {
    Owned("Owned"),
    Locked("Locked")
}

private object ColorPaletteRewards {
    val Card = Color(0xFFFFFFFF)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Honey = Color(0xFFFF9F1C)
    val Mint = Color(0xFF23A160)
    val Muted = Color(0xFF6A6581)
    val Ink = Color(0xFF1E1A34)
}