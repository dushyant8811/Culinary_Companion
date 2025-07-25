package com.example.culinarycompanion.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

enum class RecipeCategory {
    BREAKFAST, LUNCH, DINNER, DESSERTS, VEGAN, ALL;

    companion object {
        fun fromString(value: String): RecipeCategory {
            return values().find { it.name == value } ?: ALL
        }
    }
}

data class Recipe(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("ingredients")
    @set:PropertyName("ingredients")
    var ingredients: List<String> = emptyList(),

    @get:PropertyName("instructions")
    @set:PropertyName("instructions")
    var instructions: List<String> = emptyList(),

    @get:PropertyName("prepTime")
    @set:PropertyName("prepTime")
    var prepTime: Int = 0,

    @get:PropertyName("cookTime")
    @set:PropertyName("cookTime")
    var cookTime: Int = 0,

    @get:PropertyName("servings")
    @set:PropertyName("servings")
    var servings: Int = 1,

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = RecipeCategory.ALL.name,

    @get:PropertyName("dietaryTags")
    @set:PropertyName("dietaryTags")
    var dietaryTags: List<String> = emptyList(),

    @get:PropertyName("imageUrl")
    @set:PropertyName("imageUrl")
    var imageUrl: String? = null,

    @get:PropertyName("isFavorite")
    @set:PropertyName("isFavorite")
    var isFavorite: Boolean = false,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt: Long = System.currentTimeMillis(),

    @get:PropertyName("author")
    @set:PropertyName("author")
    var author: String = "Culinary Companion",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("averageRating")
    @set:PropertyName("averageRating")
    var averageRating: Float = 0.0f,

    @get:PropertyName("reviewCount")
    @set:PropertyName("reviewCount")
    var reviewCount: Int = 0

) {
    // Excluded from Firestore (computed property)
    @Exclude
    fun getCategoryDisplayName(): String {
        return when (category) {
            RecipeCategory.ALL.name -> "All"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }

    // Helper function to convert to map for Firestore
    @Exclude
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
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "author" to author,
            "description" to description,
            "averageRating" to averageRating,
            "reviewCount" to reviewCount
        )
    }

    companion object {
        // Helper function to create from Firestore document
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): Recipe {
            return Recipe(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                ingredients = map["ingredients"] as? List<String> ?: emptyList(),
                instructions = map["instructions"] as? List<String> ?: emptyList(),

                prepTime = (map["prepTime"] as? Number)?.toInt() ?: 0,
                cookTime = (map["cookTime"] as? Number)?.toInt() ?: 0,
                servings = (map["servings"] as? Number)?.toInt() ?: 1,
                reviewCount = (map["reviewCount"] as? Number)?.toInt() ?: 0,
                averageRating = (map["averageRating"] as? Number)?.toFloat() ?: 0.0f,

                category = map["category"] as? String ?: RecipeCategory.ALL.name,
                dietaryTags = map["dietaryTags"] as? List<String> ?: emptyList(),
                imageUrl = map["imageUrl"] as? String,
                isFavorite = map["isFavorite"] as? Boolean ?: false,
                author = map.getOrDefault("author", "Culinary Companion") as String,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}