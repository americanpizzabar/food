package com.americanpizzabar.food.data.local.dao

import androidx.room.*
import com.americanpizzabar.food.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE recipeName LIKE '%' || :query || '%' OR comment LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchEntries(query: String): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE date >= :from AND date <= :to ORDER BY date DESC")
    fun getEntriesByDateRange(from: String, to: String): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE rating = :rating ORDER BY date DESC")
    fun getEntriesByRating(rating: Int): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntity): Long

    @Update
    suspend fun updateEntry(entry: JournalEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)
}
