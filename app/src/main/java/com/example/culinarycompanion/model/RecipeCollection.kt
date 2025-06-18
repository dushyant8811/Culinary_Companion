package com.example.culinarycompanion.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.culinarycompanion.database.Converters

@Entity
@TypeConverters(Converters::class)
data class RecipeCollection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    var createdAt: Long = System.currentTimeMillis(),
    var recipeIds: List<Int> = emptyList()
)