package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.example.mobile.data.local.entities.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pet WHERE id = 1")
    fun getPet(): Flow<PetEntity?>

    @Update
    suspend fun updatePet(pet: PetEntity): Int
}
