package com.example.culinarycompanion.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.culinarycompanion.database.Converters
import com.example.culinarycompanion.model.Recipe
import java.util.Date

@Entity(
    tableName = "saved_recipes",
    indices = [
        Index(value = ["isFavorite"], name = "idx_favorite"),
        Index(value = ["category"], name = "idx_category"),
        Index(value = ["lastUpdated"], name = "idx_last_updated")
    ]
)
@TypeConverters(Converters::class)
data class SavedRecipe(
    @PrimaryKey
    val id: String = "",

    @ColumnInfo(defaultValue = "")
    val title: String = "",

    @ColumnInfo(defaultValue = "")
    val ingredients: List<String> = emptyList(),

    @ColumnInfo(defaultValue = "")
    val instructions: List<String> = emptyList(),

    @ColumnInfo(defaultValue = "0")
    val prepTime: Int = 0,

    @ColumnInfo(defaultValue = "0")
    val cookTime: Int = 0,

    @ColumnInfo(defaultValue = "1")
    val servings: Int = 1,

    @ColumnInfo(defaultValue = "ALL")
    val category: String = "ALL",

    @ColumnInfo(defaultValue = "")
    val dietaryTags: List<String> = emptyList(),

    val imageUrl: String? = null,

    @ColumnInfo(defaultValue = "0")
    val isFavorite: Boolean = false,

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // Required empty constructor for Firestore
    constructor() : this(
        id = "",
        title = "",
        ingredients = emptyList(),
        instructions = emptyList(),
        prepTime = 0,
        cookTime = 0,
        servings = 1,
        category = "ALL",
        dietaryTags = emptyList(),
        imageUrl = null,
        isFavorite = false,
        lastUpdated = System.currentTimeMillis()
    )

    // Conversion from Recipe
    companion object {
        fun fromRecipe(recipe: Recipe): SavedRecipe {
            return SavedRecipe(
                id = recipe.id,
                title = recipe.title,
                ingredients = recipe.ingredients,
                instructions = recipe.instructions,
                prepTime = recipe.prepTime,
                cookTime = recipe.cookTime,
                servings = recipe.servings,
                category = recipe.category,
                dietaryTags = recipe.dietaryTags,
                imageUrl = recipe.imageUrl,
                isFavorite = recipe.isFavorite,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    // Conversion to Recipe
    fun toRecipe(): Recipe {
        return Recipe(
            id = id,
            title = title,
            ingredients = ingredients,
            instructions = instructions,
            prepTime = prepTime,
            cookTime = cookTime,
            servings = servings,
            category = category,
            dietaryTags = dietaryTags,
            imageUrl = imageUrl,
            isFavorite = isFavorite
        )
    }

    // For Firestore operations
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "ingredients" to ingredients,
            "instructions" to instructions,
            "prepTime" to prepTime,
            "cookTime" to cookTime,
            "servings" to servings,
            "category" to category,
            "dietaryTags" to dietaryTags,
            "imageUrl" to imageUrl,
            "isFavorite" to isFavorite,
            "lastUpdated" to lastUpdated
        )
    }
}