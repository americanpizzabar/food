package com.americanpizzabar.food.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.americanpizzabar.food.domain.model.FeelingAfter
import com.americanpizzabar.food.domain.model.HealthCondition
import com.americanpizzabar.food.domain.model.JournalEntry
import com.americanpizzabar.food.domain.model.Mood
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long? = null,
    val recipeName: String,
    val date: String,
    val rating: Int,
    val comment: String,
    val mood: String? = null,
    val healthCondition: String? = null,
    val imageUri: String? = null,
    val tagsJson: String = "[]",
    val feelingAfter: String? = null
) {
    fun toDomain(): JournalEntry {
        val gson = Gson()
        return JournalEntry(
            id = id,
            recipeId = recipeId,
            recipeName = recipeName,
            date = LocalDate.parse(date),
            rating = rating,
            comment = comment,
            mood = mood?.let { Mood.valueOf(it) },
            healthCondition = healthCondition?.let { HealthCondition.valueOf(it) },
            imageUri = imageUri,
            tags = gson.fromJson(tagsJson, object : TypeToken<List<String>>() {}.type),
            feelingAfter = feelingAfter?.let { FeelingAfter.valueOf(it) }
        )
    }

    companion object {
        fun fromDomain(entry: JournalEntry): JournalEntity {
            val gson = Gson()
            return JournalEntity(
                id = entry.id,
                recipeId = entry.recipeId,
                recipeName = entry.recipeName,
                date = entry.date.toString(),
                rating = entry.rating,
                comment = entry.comment,
                mood = entry.mood?.name,
                healthCondition = entry.healthCondition?.name,
                imageUri = entry.imageUri,
                tagsJson = gson.toJson(entry.tags),
                feelingAfter = entry.feelingAfter?.name
            )
        }
    }
}
