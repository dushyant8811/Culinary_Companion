package com.example.culinarycompanion.model

import androidx.room.Ignore

enum class RecipeCategory {
    BREAKFAST, LUNCH, DINNER, DESSERTS, VEGAN, ALL
}

data class Recipe(
    val id: Int = 0,                          // Default ID
    val title: String = "",                    // Default empty title
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTime: Int = 0,                     // in minutes (default 0)
    val cookTime: Int = 0,                     // in minutes (default 0)
    val servings: Int = 1,                     // Default 1 serving
    val category: String = RecipeCategory.ALL.name, // Default category
    val dietaryTags: List<String> = emptyList(), // For filters
    val imageUrl: String? = null, // Add image URL field

    @Ignore // Add this to exclude from Room persistence
    var isFavorite: Boolean = false
)

{
    // Helper function to get category display name
    fun getCategoryDisplayName(): String {
        return when (category) {
            RecipeCategory.ALL.name -> "All"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }

    // Add this for Room compatibility
    constructor(
        id: Int,
        title: String,
        ingredients: List<String>,
        instructions: List<String>,
        prepTime: Int,
        cookTime: Int,
        servings: Int,
        category: String,
        dietaryTags: List<String>,
        imageUrl: String?
    ) : this(
        id, title, ingredients, instructions, prepTime, cookTime, servings,
        category, dietaryTags, imageUrl, false
    )

}
