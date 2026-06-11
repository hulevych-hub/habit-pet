package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ProgressHeader
import com.example.mobile.presentation.ui.components.ProgressHeaderState
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
        val result = inventoryItemRepository.purchaseItem(itemId)

        when (result) {
            1 -> { /* Success */ }
            -2 -> { /* Already purchased */ }
            -4 -> { /* Not enough coins */ }
            else -> { /* Other error */ }
        }
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

    var selectedTab by rememberSaveable { mutableStateOf(CollectionTab.Owned) }
    var selectedRarity by rememberSaveable { mutableStateOf<Rarity?>(null) }
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }

    val allItems = remember {
        mutableStateListOf<InventoryItemEntity>()
    }

    val outfitsList by rewardsViewModel.outfits.collectAsState(initial = emptyList())
    val backgroundsList by rewardsViewModel.backgrounds.collectAsState(initial = emptyList())
    val aurasList by rewardsViewModel.auras.collectAsState(initial = emptyList())
    val progressUiState by homeScreenViewModel.uiState.collectAsState()

    LaunchedEffect(
        outfitsList,
        backgroundsList,
        aurasList
    ) {
        allItems.clear()
        allItems.addAll(outfitsList)
        allItems.addAll(backgroundsList)
        allItems.addAll(aurasList)
    }

    val filteredItems = allItems.filter { item ->

        val ownedMatch = when (selectedTab) {
            CollectionTab.Owned -> item.isPurchased
            CollectionTab.Locked -> !item.isPurchased
        }

        val rarityMatch =
            selectedRarity == null || item.rarity == selectedRarity

        val typeMatch =
            selectedType == null || item.type == selectedType

        ownedMatch && rarityMatch && typeMatch
    }

    val groupedItems = filteredItems.groupBy { it.type }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Collection") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            ProgressHeader(
                state = ProgressHeaderState(
                    level = progressUiState.pet.level,
                    xp = progressUiState.pet.xp,
                    evolutionStage = progressUiState.pet.evolutionStage,
                    totalCoins = progressUiState.totalCoins,
                    globalStreak = progressUiState.globalStreak
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box {

                    var expanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .width(128.dp)
                            .height(36.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { expanded = true }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = selectedRarity?.name ?: "All Rarities",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("All Rarities") },
                            onClick = {
                                selectedRarity = null
                                expanded = false
                            }
                        )

                        Rarity.values().forEach { rarity ->

                            DropdownMenuItem(
                                text = { Text(rarity.name) },
                                onClick = {
                                    selectedRarity = rarity
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box {

                    var expanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .width(128.dp)
                            .height(36.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { expanded = true }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = selectedType?.let(CustomizationTypes::displayName) ?: "All Types",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("All Types") },
                            onClick = {
                                selectedType = null
                                expanded = false
                            }
                        )

                        CustomizationTypes.TYPES.forEach { type ->

                            DropdownMenuItem(
                                text = { Text(CustomizationTypes.displayName(type)) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TabRow(
                    selectedTabIndex = selectedTab.ordinal
                ) {

                    Tab(
                        selected = selectedTab == CollectionTab.Owned,
                        onClick = { selectedTab = CollectionTab.Owned },
                        text = { Text("Owned") }
                    )

                    Tab(
                        selected = selectedTab == CollectionTab.Locked,
                        onClick = { selectedTab = CollectionTab.Locked },
                        text = { Text("Locked") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (groupedItems.isEmpty()) {

                EmptyStateCard(
                    title = "New discoveries await you",
                    message = "Complete habits, open chests, and claim achievements to fill your collection.",
                    hint = "Locked items are future rewards waiting for the right moment.",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    groupedItems.forEach { (type, typeItems) ->

                        item {

                            Text(
                                text = CustomizationTypes.displayName(type),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        items(typeItems) { item ->

                            InventoryItemRow(
                                item = item,
                                viewModel = rewardsViewModel,
                                tab = selectedTab
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemRow(
    item: InventoryItemEntity,
    viewModel: RewardsViewModel,
    tab: CollectionTab
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    rarityColor(item.rarity).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        rarityColor(item.rarity).copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {

            Text(item.name)

            Text(
                text = "${CustomizationTypes.displayName(item.type).dropLast(1)} • ${item.rarity.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Source: ${item.unlockSource.ifBlank { "Shop" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "${item.price} coins",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        when (tab) {

            CollectionTab.Owned -> {

                if (item.isPurchased && !item.isEquipped) {

                    Button(
                        onClick = {
                            viewModel.equipItem(item.type, item.id.toString())
                        }
                    ) {
                        Text("Equip")
                    }

                } else if (item.isEquipped) {

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Equipped",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF16A34A)
                        )

                        Button(
                            onClick = {
                                viewModel.unequipItem(item.type)
                            }
                        ) {
                            Text("Unequip")
                        }
                    }
                }
            }

            CollectionTab.Locked -> {

                if (item.isUnlocked && !item.isPurchased) {

                    Button(
                        onClick = {
                            viewModel.purchaseItem(item.id)
                        }
                    ) {
                        Text("Buy")
                    }

                } else {

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

private fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.NORMAL -> Color.Gray
    Rarity.RARE -> Color(0xFF0066FF)
    Rarity.EPIC -> Color.Magenta
    Rarity.LEGENDARY -> Color(0xFFFF8C00)
}

enum class CollectionTab {
    Owned,
    Locked
}
