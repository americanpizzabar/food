package com.americanpizzabar.food.data.repository

import com.americanpizzabar.food.data.local.dao.JournalDao
import com.americanpizzabar.food.data.local.entity.JournalEntity
import com.americanpizzabar.food.domain.model.JournalEntry
import com.americanpizzabar.food.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val dao: JournalDao
) : JournalRepository {
    override fun getAllEntries(): Flow<List<JournalEntry>> =
        dao.getAllEntries().map { it.map { e -> e.toDomain() } }

    override fun searchEntries(query: String): Flow<List<JournalEntry>> =
        dao.searchEntries(query).map { it.map { e -> e.toDomain() } }

    override fun getEntriesByDateRange(from: LocalDate, to: LocalDate): Flow<List<JournalEntry>> =
        dao.getEntriesByDateRange(from.toString(), to.toString()).map { it.map { e -> e.toDomain() } }

    override fun getEntriesByRating(rating: Int): Flow<List<JournalEntry>> =
        dao.getEntriesByRating(rating).map { it.map { e -> e.toDomain() } }

    override suspend fun getEntryById(id: Long): JournalEntry? =
        dao.getEntryById(id)?.toDomain()

    override suspend fun saveEntry(entry: JournalEntry): Long =
        dao.insertEntry(JournalEntity.fromDomain(entry))

    override suspend fun updateEntry(entry: JournalEntry) =
        dao.updateEntry(JournalEntity.fromDomain(entry))

    override suspend fun deleteEntry(id: Long) = dao.deleteEntry(id)
}
