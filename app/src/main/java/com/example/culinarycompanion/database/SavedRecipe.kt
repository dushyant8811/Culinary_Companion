package com.example.culinarycompanion.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.culinarycompanion.database.Converters

@Entity(tableName = "saved_recipes")
@TypeConverters(Converters::class)
data class SavedRecipe(
    @PrimaryKey val id: String,
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val category: String,
    val dietaryTags: List<String>,
    val imageUrl: String?,
    val isFavorite: Boolean
)