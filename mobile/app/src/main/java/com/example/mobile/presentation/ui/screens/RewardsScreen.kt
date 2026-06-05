package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.data.local.entities.InventoryItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val inventoryItemRepository: InventoryItemRepository
) : ViewModel() {
    val hats = inventoryItemRepository.getItemsByType("HAT")
    val glasses = inventoryItemRepository.getItemsByType("GLASSES")
    val scarves = inventoryItemRepository.getItemsByType("SCARF")
    val backgrounds = inventoryItemRepository.getItemsByType("BACKGROUND")

    fun purchaseItem(itemId: Long) = viewModelScope.launch {
        val result = inventoryItemRepository.purchaseItem(itemId)
        // Handle purchase result (for now just logging, could show popup based on result)
        when (result) {
            1 -> { /* Success - item purchased */ }
            -2 -> { /* Already purchased */ }
            -4 -> { /* Not enough coins */ }
            else -> { /* Other error */ }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RewardsScreen(rewardsViewModel: RewardsViewModel = hiltViewModel()) {
    val hats by rewardsViewModel.hats.collectAsState(initial = emptyList())
    val glasses by rewardsViewModel.glasses.collectAsState(initial = emptyList())
    val scarves by rewardsViewModel.scarves.collectAsState(initial = emptyList())
    val backgrounds by rewardsViewModel.backgrounds.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Rewards") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hats Section
            Text("Hats", style = MaterialTheme.typography.titleLarge)
            hats.forEach { item ->
                RewardItem(item, rewardsViewModel)
            }

            Divider()

            // Glasses Section
            Text("Glasses", style = MaterialTheme.typography.titleLarge)
            glasses.forEach { item ->
                RewardItem(item, rewardsViewModel)
            }

            Divider()

            // Scarves Section
            Text("Scarves", style = MaterialTheme.typography.titleLarge)
            scarves.forEach { item ->
                RewardItem(item, rewardsViewModel)
            }

            Divider()

            // Backgrounds Section
            Text("Backgrounds", style = MaterialTheme.typography.titleLarge)
            backgrounds.forEach { item ->
                RewardItem(item, rewardsViewModel)
            }
        }
    }
}

@Composable
private fun RewardItem(item: InventoryItemEntity, rewardsViewModel: RewardsViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder for image
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(24.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(item.name)
            Text("${item.price} coins", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (!item.isPurchased) {
                    rewardsViewModel.purchaseItem(item.id)
                }
            },
            enabled = item.isUnlocked && !item.isPurchased
        ) {
            Text(if (item.isPurchased) "Purchased" else "Buy")
        }
    }
}
