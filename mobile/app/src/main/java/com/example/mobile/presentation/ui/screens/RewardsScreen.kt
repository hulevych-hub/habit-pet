package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ProgressHeader
import com.example.mobile.presentation.ui.components.ProgressHeaderState
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val inventoryItemRepository: InventoryItemRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    val outfits = inventoryItemRepository.getItemsByType(CustomizationTypes.OUTFIT)
    val backgrounds = inventoryItemRepository.getItemsByType(CustomizationTypes.BACKGROUND)
    val auras = inventoryItemRepository.getItemsByType(CustomizationTypes.AURA)

    fun purchaseItem(itemId: Long) = viewModelScope.launch {
        inventoryItemRepository.purchaseItem(itemId)
    }

    fun equipItem(itemType: String, itemId: String) = viewModelScope.launch {
        petRepository.equipItem(itemType, itemId)
    }

    fun unequipItem(itemType: String) = viewModelScope.launch {
        petRepository.unequipItem(itemType)
    }
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
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Collection", color = ColorPaletteRewards.Ink) }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CollectionHeader(
                    totalCoins = progressUiState.totalCoins,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProgressHeader(
                    state = ProgressHeaderState(
                        level = progressUiState.pet.level,
                        xp = progressUiState.pet.xp,
                        evolutionStage = progressUiState.pet.evolutionStage,
                        totalCoins = progressUiState.totalCoins,
                        globalStreak = progressUiState.globalStreak,
                        currentCombo = progressUiState.currentCombo,
                        lastHabitCompletionTimestamp = progressUiState.lastHabitCompletionTimestamp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                CollectionTypeTabs(
                    selectedTypeTab = selectedTypeTab,
                    onTypeSelected = { selectedTypeTab = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                RarityFilter(
                    selectedRarity = selectedRarity,
                    onRaritySelected = { selectedRarity = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                CollectionToggle(
                    selectedCollection = selectedCollection,
                    onSelected = { selectedCollection = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
            }

            if (filteredItems.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCard(
                        title = when (selectedCollection) {
                            CollectionTab.Owned -> "No items equipped yet"
                            CollectionTab.Locked -> "New discoveries await"
                        },
                        message = when (selectedCollection) {
                            CollectionTab.Owned -> "Purchase or claim items to decorate your dragon's journey."
                            CollectionTab.Locked -> "Complete habits, open chests, and claim achievements to reveal more."
                        },
                        hint = "Locked items are future rewards waiting for the right moment.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            gridItems(
                items = filteredItems,
                key = { filteredItem -> filteredItem.id }
            ) { item ->
                InventoryItemGridCard(
                    item = item,
                    selectedCollection = selectedCollection,
                    onPurchase = { rewardsViewModel.purchaseItem(item.id) },
                    onEquip = { rewardsViewModel.equipItem(item.type, item.id.toString()) },
                    onUnequip = { rewardsViewModel.unequipItem(item.type) }
                )
            }
        }
    }
}

@Composable
private fun CollectionHeader(
    totalCoins: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ColorPaletteRewards.Card),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPaletteRewards.LavenderSoft)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dragon wardrobe",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ColorPaletteRewards.Ink
                )
                Text(
                    text = "$totalCoins coins",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorPaletteRewards.Amber
                )
                Text(
                    text = "Outfits, backgrounds, and auras make every growth milestone feel personal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CollectionTypeTabs(
    selectedTypeTab: CollectionTypeTab,
    onTypeSelected: (CollectionTypeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTypeTab.ordinal,
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        divider = {}
    ) {
        CollectionTypeTab.values().forEach { tab ->
            Tab(
                selected = selectedTypeTab == tab,
                onClick = { onTypeSelected(tab) },
                text = { Text(tab.label) },
                modifier = Modifier.background(
                    if (selectedTypeTab == tab) ColorPaletteRewards.Violet else ColorPaletteRewards.SoftSurface,
                    RoundedCornerShape(999.dp)
                )
            )
        }
    }
}

@Composable
private fun RarityFilter(
    selectedRarity: Rarity?,
    onRaritySelected: (Rarity?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RarityChip(
            label = "All",
            color = ColorPaletteRewards.Muted,
            selected = selectedRarity == null,
            onClick = { onRaritySelected(null) },
            modifier = Modifier.weight(1f)
        )
        Rarity.values().forEach { rarity ->
            RarityChip(
                label = rarity.name.lowercase(),
                color = rarityColor(rarity),
                selected = selectedRarity == rarity,
                onClick = { onRaritySelected(rarity) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RarityChip(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) color.copy(alpha = 0.22f) else ColorPaletteRewards.SoftSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CollectionToggle(
    selectedCollection: CollectionTab,
    onSelected: (CollectionTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = ColorPaletteRewards.SoftSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CollectionTab.values().forEach { tab ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable { onSelected(tab) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (selectedCollection == tab) ColorPaletteRewards.Card else Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedCollection == tab) ColorPaletteRewards.Violet else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryItemGridCard(
    item: InventoryItemEntity,
    selectedCollection: CollectionTab,
    onPurchase: () -> Unit,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isEquipped) ColorPaletteRewards.MintSoft else ColorPaletteRewards.Card
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPurchased) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            rarityColor(item.rarity).copy(alpha = 0.18f),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isPurchased) Icons.Default.Star else Icons.Default.Lock,
                        contentDescription = null,
                        tint = rarityColor(item.rarity),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = item.rarity.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = rarityColor(item.rarity)
                )
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = ColorPaletteRewards.Ink
            )
            Text(
                text = item.unlockSource.ifBlank { "Shop" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.price} coins",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorPaletteRewards.Amber
                )
                when {
                    item.isEquipped -> {
                        Button(
                            onClick = onUnequip,
                            colors = ButtonDefaults.buttonColors(containerColor = ColorPaletteRewards.SoftSurface),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text("Unequip", color = ColorPaletteRewards.Ink)
                        }
                    }
                    selectedCollection == CollectionTab.Owned && item.isPurchased -> {
                        Button(
                            onClick = onEquip,
                            colors = ButtonDefaults.buttonColors(containerColor = ColorPaletteRewards.Violet),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text("Equip")
                        }
                    }
                    selectedCollection == CollectionTab.Locked && item.isUnlocked -> {
                        Button(
                            onClick = onPurchase,
                            colors = ButtonDefaults.buttonColors(containerColor = ColorPaletteRewards.Amber),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text("Buy")
                        }
                    }
                    else -> {
                        Text(
                            text = "Locked",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            if (item.isEquipped) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ColorPaletteRewards.Mint,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Equipped",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorPaletteRewards.Mint
                    )
                }
            }
        }
    }
}

private fun CollectionTypeTab.itemsFlow(viewModel: RewardsViewModel) = when (this) {
    CollectionTypeTab.Outfits -> viewModel.outfits
    CollectionTypeTab.Backgrounds -> viewModel.backgrounds
    CollectionTypeTab.Auras -> viewModel.auras
}

private fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.NORMAL -> Color(0xFF6F6A8A)
    Rarity.RARE -> Color(0xFF4BA3FF)
    Rarity.EPIC -> Color(0xFFB26CFF)
    Rarity.LEGENDARY -> Color(0xFFFFB84D)
}

private enum class CollectionTypeTab(val label: String) {
    Outfits("Outfits"),
    Backgrounds("Backgrounds"),
    Auras("Auras")
}

private enum class CollectionTab(val label: String) {
    Owned("Owned"),
    Locked("Locked")
}

private object ColorPaletteRewards {
    val Card = Color(0xFFFFFFFF)
    val SoftSurface = Color(0xFFF1EDFF)
    val LavenderSoft = Color(0xFFF2EEFF)
    val MintSoft = Color(0xFFE8FBF2)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Mint = Color(0xFF4EDB95)
    val Muted = Color(0xFF6F6A8A)
    val Ink = Color(0xFF302B4A)
}
