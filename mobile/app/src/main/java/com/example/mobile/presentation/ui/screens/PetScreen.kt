package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import com.example.mobile.presentation.ui.components.AnimatedPet
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.data.local.entities.PetEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {
    val pet = petRepository.getPet()
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PetScreen(petViewModel: PetViewModel = hiltViewModel()) {
    val pet by petViewModel.pet.collectAsState(initial = PetEntity())
    val xpForNextLevel = remember { calculateXpForNextLevel(pet.level) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Pet") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Pet Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Level ${pet.level}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    // Evolution stage text
                    Text(
                        text = when (pet.evolutionStage) {
                            0 -> "Egg"
                            1 -> "Hatchling"
                            2 -> "Young Dragon"
                            3 -> "Adult Dragon"
                            4 -> "Ancient Dragon"
                            else -> "Unknown"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    // XP Progress Bar
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .padding(top = 8.dp),
                        progress = if (xpForNextLevel > 0) pet.xp % xpForNextLevel.toFloat() / xpForNextLevel else 0f,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "XP: ${pet.xp % xpForNextLevel.toLong()} / ${xpForNextLevel}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Pet Display
            AnimatedPet(
                pet = pet,
                modifier = Modifier.size(350.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Equipment Section
            Text("Equipment", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EquipmentSlot("Hat", pet.equippedHat)
                EquipmentSlot("Glasses", pet.equippedGlasses)
                EquipmentSlot("Scarf", pet.equippedScarf)
            }

            Spacer(modifier = Modifier.height(16.dp))

            EquipmentSlot("Background", pet.equippedBackground)
        }
    }
}

// Helper function to calculate XP needed for next level
private fun calculateXpForNextLevel(level: Int): Int {
    // Same formula as in ViewModel: level 0 -> 100, level 1 -> 150, level 2 -> 200, etc.
    return 100 + (level * 50)
}

@Composable
private fun EquipmentSlot(label: String, equippedItem: String?) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(
                if (equippedItem != null) Color.Green.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (equippedItem != null) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (equippedItem != null) "$label equipped" else "No $label",
            tint = if (equippedItem != null) Color.Green else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(label)
        Text(equippedItem ?: "None", style = MaterialTheme.typography.bodySmall)
    }
}
