package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
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
}
