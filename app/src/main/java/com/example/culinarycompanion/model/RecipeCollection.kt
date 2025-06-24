package com.example.culinarycompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.culinarycompanion.database.Converters

@Entity
@TypeConverters(Converters::class)
data class RecipeCollection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    @ColumnInfo(name = "createdAt")
    var createdAt: Long = System.currentTimeMillis(),
    var recipeIds: List<String> = emptyList()  // Change to String
)

// database/SavedRecipe.kt
@Entity(tableName = "saved_recipes")
@TypeConverters(Converters::class)
data class SavedRecipe(
    @PrimaryKey val id: String,  // Change to String
    // ... other fields ...
)

// database/Converters.kt
@TypeConverter
fun fromStringList(value: String?): List<String> {
    if (value.isNullOrEmpty()) return emptyList()
    return value.split(",")
}

@TypeConverter
fun toStringList(list: List<String>?): String {
    return list?.joinToString(",") ?: ""
}