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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
            // Large Pet Display
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color.LightGray, androidx.compose.foundation.shape.CircleShape)
            ) {
                Text("Pet: ${pet.name}\nStage: ${pet.evolutionStage}\nLevel: ${pet.level}\nXP: ${pet.xp}",
                    textAlign = TextAlign.Center,
                    color = Color.Black)
            }

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
