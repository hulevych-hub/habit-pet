package com.example.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mobile.data.local.entities.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items WHERE type = :type ORDER BY name")
    fun getItemsByType(type: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :itemId")
    fun getItemById(itemId: Long): Flow<InventoryItemEntity?>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity): Int

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity): Int

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAll()
}