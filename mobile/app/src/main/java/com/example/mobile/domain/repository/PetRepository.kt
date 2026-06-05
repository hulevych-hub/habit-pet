package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.PetEntity
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getPet(): Flow<PetEntity>
    suspend fun updatePet(pet: PetEntity): Int
    suspend fun equipItem(itemType: String, itemId: String): Int
    suspend fun unequipItem(itemType: String): Int
}