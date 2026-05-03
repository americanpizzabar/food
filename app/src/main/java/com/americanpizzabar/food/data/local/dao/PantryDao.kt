package com.americanpizzabar.food.data.local.dao

import androidx.room.*
import com.americanpizzabar.food.data.local.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY category, name")
    fun getAllItems(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE name LIKE '%' || :query || '%' ORDER BY category, name")
    fun searchItems(query: String): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE expiryDate IS NOT NULL AND expiryDate <= :threshold ORDER BY expiryDate")
    fun getExpiringItems(threshold: String): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun getItemById(id: Long): PantryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PantryItemEntity): Long

    @Update
    suspend fun updateItem(item: PantryItemEntity)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteItem(id: Long)
}
