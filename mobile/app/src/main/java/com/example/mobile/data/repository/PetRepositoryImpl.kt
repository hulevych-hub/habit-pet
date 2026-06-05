package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao
) : PetRepository {
    override fun getPet(): Flow<PetEntity> =
        petDao.getPet().map { it ?: PetEntity(id = 1) }

    override suspend fun updatePet(pet: PetEntity): Int {
        val updatedRows = petDao.updatePet(pet.copy(id = 1))
        if (updatedRows == 0) {
            petDao.insertPet(pet.copy(id = 1))
        }
        return updatedRows
    }

    override suspend fun equipItem(itemType: String, itemId: String): Int {
        return try {
            val currentPet = petDao.getPet().firstOrNull() ?: PetEntity(id = 1)
            val updatedPet = when (itemType) {
                "HAT" -> currentPet.copy(equippedHat = itemId)
                "GLASSES" -> currentPet.copy(equippedGlasses = itemId)
                "SCARF" -> currentPet.copy(equippedScarf = itemId)
                "BACKGROUND" -> currentPet.copy(equippedBackground = itemId)
                else -> currentPet
            }
            petDao.updatePet(updatedPet.copy(id = 1))
        } catch (e: Exception) {
            -1
        }
    }

    override suspend fun unequipItem(itemType: String): Int {
        return try {
            val currentPet = petDao.getPet().firstOrNull() ?: PetEntity(id = 1)
            val updatedPet = when (itemType) {
                "HAT" -> currentPet.copy(equippedHat = null)
                "GLASSES" -> currentPet.copy(equippedGlasses = null)
                "SCARF" -> currentPet.copy(equippedScarf = null)
                "BACKGROUND" -> currentPet.copy(equippedBackground = null)
                else -> currentPet
            }
            petDao.updatePet(updatedPet.copy(id = 1))
        } catch (e: Exception) {
            -1
        }
    }
}
