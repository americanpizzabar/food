package com.americanpizzabar.food.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.americanpizzabar.food.domain.model.HealthLog
import java.time.LocalDate

@Entity(tableName = "health_logs")
data class HealthLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val journalEntryId: Long,
    val recipeName: String,
    val feelingBefore: Int,
    val feelingAfter: Int,
    val energyLevel: Int,
    val digestiveComfort: Int,
    val notes: String = ""
) {
    fun toDomain() = HealthLog(
        id = id, date = LocalDate.parse(date), journalEntryId = journalEntryId,
        recipeName = recipeName, feelingBefore = feelingBefore, feelingAfter = feelingAfter,
        energyLevel = energyLevel, digestiveComfort = digestiveComfort, notes = notes
    )

    companion object {
        fun fromDomain(log: HealthLog) = HealthLogEntity(
            id = log.id, date = log.date.toString(), journalEntryId = log.journalEntryId,
            recipeName = log.recipeName, feelingBefore = log.feelingBefore,
            feelingAfter = log.feelingAfter, energyLevel = log.energyLevel,
            digestiveComfort = log.digestiveComfort, notes = log.notes
        )
    }
}
