package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao,
    private val inventoryItemRepository: InventoryItemRepository
) : PetRepository {
    override fun getPet(): Flow<PetEntity> =
        petDao.getPet().map { it ?: PetEntity(id = 0) }

    override suspend fun updatePet(pet: PetEntity): Int {
        val updatedRows = petDao.updatePet(pet.copy(id = 1))
        if (updatedRows == 0) {
            petDao.insertPet(pet.copy(id = 1))
        }
        return updatedRows
    }

    override suspend fun resetPet() {
        petDao.resetPet()
    }

    override suspend fun equipItem(itemType: String, itemId: String): Int {
        return try {
            val currentPet = petDao.getPet().firstOrNull() ?: PetEntity(id = 1)
            val inventoryItemId = itemId.toLongOrNull() ?: return -1
            val inventoryItem = inventoryItemRepository.getItemById(inventoryItemId).firstOrNull()
                ?: return -1

            if (!inventoryItem.isPurchased || inventoryItem.type != itemType) return -1

            val updatedPet = when (itemType) {
                CustomizationTypes.OUTFIT -> currentPet.copy(equippedOutfit = itemId)
                CustomizationTypes.BACKGROUND -> currentPet.copy(equippedBackground = itemId)
                CustomizationTypes.AURA -> currentPet.copy(equippedAura = itemId)
                else -> return -1
            }

            clearEquippedInventoryItem(currentPet, itemType)
            val updatedInventoryItem = inventoryItem.copy(isEquipped = true)
            inventoryItemRepository.updateItem(updatedInventoryItem)
            petDao.updatePet(updatedPet.copy(id = 1))
            1
        } catch (e: Exception) {
            -1
        }
    }

    override suspend fun unequipItem(itemType: String): Int {
        return try {
            val currentPet = petDao.getPet().firstOrNull() ?: PetEntity(id = 1)
            val equippedItemId = when (itemType) {
                CustomizationTypes.OUTFIT -> currentPet.equippedOutfit
                CustomizationTypes.BACKGROUND -> currentPet.equippedBackground
                CustomizationTypes.AURA -> currentPet.equippedAura
                else -> null
            }

            val updatedPet = when (itemType) {
                CustomizationTypes.OUTFIT -> currentPet.copy(equippedOutfit = null)
                CustomizationTypes.BACKGROUND -> currentPet.copy(equippedBackground = null)
                CustomizationTypes.AURA -> currentPet.copy(equippedAura = null)
                else -> return -1
            }

            clearEquippedInventoryItem(currentPet, itemType)
            equippedItemId?.toLongOrNull()?.let { id ->
                inventoryItemRepository.getItemById(id).firstOrNull()
                    ?.let { inventoryItemRepository.updateItem(it.copy(isEquipped = false)) }
            }
            petDao.updatePet(updatedPet.copy(id = 1))
            1
        } catch (e: Exception) {
            -1
        }
    }

    private suspend fun clearEquippedInventoryItem(pet: PetEntity, itemType: String) {
        val equippedItemId = when (itemType) {
            CustomizationTypes.OUTFIT -> pet.equippedOutfit
            CustomizationTypes.BACKGROUND -> pet.equippedBackground
            CustomizationTypes.AURA -> pet.equippedAura
            else -> null
        }

        equippedItemId?.toLongOrNull()?.let { id ->
            inventoryItemRepository.getItemById(id).firstOrNull()
                ?.let { inventoryItemRepository.updateItem(it.copy(isEquipped = false)) }
        }
    }


}
