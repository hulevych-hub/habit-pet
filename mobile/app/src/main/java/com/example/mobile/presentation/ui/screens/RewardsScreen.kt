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
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val inventoryItemRepository: InventoryItemRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    val hats = inventoryItemRepository.getItemsByType("HAT")
    val glasses = inventoryItemRepository.getItemsByType("GLASSES")
    val scarves = inventoryItemRepository.getItemsByType("SCARF")
    val backgrounds = inventoryItemRepository.getItemsByType("BACKGROUND")

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    rewardsViewModel: RewardsViewModel = hiltViewModel()
) {

    var selectedTab by rememberSaveable { mutableStateOf(Tab.Owned) }
    var selectedRarity by rememberSaveable { mutableStateOf<Rarity?>(null) }
    var selectedSlot by rememberSaveable { mutableStateOf<String?>(null) }

    val allItems = remember {
        mutableStateListOf<InventoryItemEntity>()
    }

    val hatsList by rewardsViewModel.hats.collectAsState(initial = emptyList())
    val glassesList by rewardsViewModel.glasses.collectAsState(initial = emptyList())
    val scarvesList by rewardsViewModel.scarves.collectAsState(initial = emptyList())
    val backgroundsList by rewardsViewModel.backgrounds.collectAsState(initial = emptyList())

    LaunchedEffect(
        hatsList,
        glassesList,
        scarvesList,
        backgroundsList
    ) {
        allItems.clear()
        allItems.addAll(hatsList)
        allItems.addAll(glassesList)
        allItems.addAll(scarvesList)
        allItems.addAll(backgroundsList)
    }

    val filteredItems = allItems.filter { item ->

        val ownedMatch = when (selectedTab) {
            Tab.Owned -> item.isPurchased
            Tab.Locked -> !item.isPurchased
        }

        val rarityMatch =
            selectedRarity == null || item.rarity == selectedRarity

        val slotMatch =
            selectedSlot == null || item.type == selectedSlot

        ownedMatch && rarityMatch && slotMatch
    }

    val groupedItems = filteredItems.groupBy { it.type }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Inventory") }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Rarity Dropdown
                Box {

                    var expanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(36.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { expanded = true }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = selectedRarity?.name ?: "All",
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

                // Slot Dropdown
                Box {

                    var expanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(36.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { expanded = true }
                            .padding(4.dp)
                    ) {

                        Text(
                            text = selectedSlot ?: "All",
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
                            text = { Text("All Slots") },
                            onClick = {
                                selectedSlot = null
                                expanded = false
                            }
                        )

                        listOf(
                            "HAT",
                            "GLASSES",
                            "SCARF",
                            "BACKGROUND"
                        ).forEach { slot ->

                            DropdownMenuItem(
                                text = { Text(slot) },
                                onClick = {
                                    selectedSlot = slot
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
                        selected = selectedTab == Tab.Owned,
                        onClick = { selectedTab = Tab.Owned },
                        text = { Text("Owned") }
                    )

                    Tab(
                        selected = selectedTab == Tab.Locked,
                        onClick = { selectedTab = Tab.Locked },
                        text = { Text("Locked") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (groupedItems.isEmpty()) {

                Text(
                    text = "No items to display",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp)
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    groupedItems.forEach { (slot, slotItems) ->

                        item {

                            Text(
                                text = slot,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        items(slotItems) { item ->

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
    tab: Tab
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        Color.LightGray.copy(alpha = 0.3f)
                    )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {

            Text(item.name)

            Row(
                modifier = Modifier.height(4.dp)
            ) {

                when (item.rarity) {

                    Rarity.NORMAL -> {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .background(Color.Gray)
                        )
                    }

                    Rarity.RARE -> {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .background(Color(0xFF0066FF))
                        )
                    }

                    Rarity.EPIC -> {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .background(Color.Magenta)
                        )
                    }

                    Rarity.LEGENDARY -> {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .background(Color(0xFFFF8C00))
                        )
                    }
                }
            }

            Text(
                "${item.price} coins",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        when (tab) {

            Tab.Owned -> {

                if (item.isPurchased && !item.isEquipped) {

                    Button(
                        onClick = {
                            viewModel.equipItem(
                                item.type,
                                item.name
                                    .lowercase(Locale.getDefault())
                                    .replace(" ", "_")
                            )
                        }
                    ) {
                        Text("Equip")
                    }

                } else if (item.isEquipped) {

                    Text(
                        "Equipped",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green
                    )
                }
            }

            Tab.Locked -> {

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

enum class Tab {
    Owned,
    Locked
}