package com.americanpizzabar.food.domain.repository

import com.americanpizzabar.food.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface JournalRepository {
    fun getAllEntries(): Flow<List<JournalEntry>>
    fun searchEntries(query: String): Flow<List<JournalEntry>>
    fun getEntriesByDateRange(from: LocalDate, to: LocalDate): Flow<List<JournalEntry>>
    fun getEntriesByRating(rating: Int): Flow<List<JournalEntry>>
    suspend fun getEntryById(id: Long): JournalEntry?
    suspend fun saveEntry(entry: JournalEntry): Long
    suspend fun updateEntry(entry: JournalEntry)
    suspend fun deleteEntry(id: Long)
}
