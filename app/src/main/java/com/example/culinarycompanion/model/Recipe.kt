package com.example.culinarycompanion.model

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
) {
    // Helper function to get category display name
    fun getCategoryDisplayName(): String {
        return when (category) {
            RecipeCategory.ALL.name -> "All"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }
}