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

    override suspend fun updatePet(pet: PetEntity): Int = petDao.updatePet(pet)
}
