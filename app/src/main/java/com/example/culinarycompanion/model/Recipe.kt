package com.example.culinarycompanion.model

import com.google.firebase.firestore.PropertyName

enum class RecipeCategory {
    BREAKFAST, LUNCH, DINNER, DESSERTS, VEGAN, ALL
}

data class Recipe(
    val id: String = "",  // Keep as immutable
    val title: String = "",

    // Make these mutable for PropertyName annotations
    @get:PropertyName("ingredients")
    @set:PropertyName("ingredients")
    var ingredients: List<String> = emptyList(),  // Changed to var

    @get:PropertyName("instructions")
    @set:PropertyName("instructions")
    var instructions: List<String> = emptyList(),  // Changed to var

    val prepTime: Int = 0,
    val cookTime: Int = 0,
    val servings: Int = 1,
    val category: String = "",

    // Make this mutable for PropertyName annotation
    @get:PropertyName("dietaryTags")
    @set:PropertyName("dietaryTags")
    var dietaryTags: List<String> = emptyList(),  // Changed to var

    val imageUrl: String? = null,
    var isFavorite: Boolean = false
) {
    // Helper function to get category display name
    fun getCategoryDisplayName(): String {
        return when (category) {
            RecipeCategory.ALL.name -> "All"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }
}