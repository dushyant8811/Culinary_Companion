package com.example.culinarycompanion.repository

import com.example.culinarycompanion.database.CollectionDao
import com.example.culinarycompanion.database.SavedRecipe
import com.example.culinarycompanion.database.SavedRecipeDao
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollectionRepository(private val collectionDao: CollectionDao,
                           private val savedRecipeDao: SavedRecipeDao
) {

    suspend fun getAllCollections(): List<RecipeCollection> {
        return withContext(Dispatchers.IO) {
            collectionDao.getAllCollections()
        }
    }

    suspend fun createCollection(collection: RecipeCollection) {
        withContext(Dispatchers.IO) {
            collectionDao.insertCollection(collection)
        }
    }

    suspend fun deleteCollection(collection: RecipeCollection) {
        withContext(Dispatchers.IO) {
            collectionDao.deleteCollection(collection)
        }
    }

    // Added these methods to match ViewModel requirements
    suspend fun addToCollection(recipeId: Int, collectionId: Long) {
        withContext(Dispatchers.IO) {
            collectionDao.addToCollection(recipeId, collectionId)
        }
    }

    suspend fun removeFromCollection(recipeId: Int, collectionId: Long) {
        withContext(Dispatchers.IO) {
            collectionDao.removeFromCollection(recipeId, collectionId)
        }
    }

    // Optional: Keep if needed for other features
    suspend fun getCollectionById(collectionId: Long): RecipeCollection? {
        return withContext(Dispatchers.IO) {
            collectionDao.getCollectionById(collectionId)
        }
    }

    // Optional: Keep if needed for other features
    suspend fun getCollectionsContainingRecipe(recipeId: Int): List<RecipeCollection> {
        return withContext(Dispatchers.IO) {
            collectionDao.getCollectionsContainingRecipe(recipeId)
        }
    }

    // Add these new functions
    suspend fun saveRecipeLocally(recipe: Recipe) {
        withContext(Dispatchers.IO) {
            val savedRecipe = SavedRecipe(
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
                isFavorite = true
            )
            savedRecipeDao.insert(savedRecipe)
        }
    }

    suspend fun deleteLocalRecipe(recipeId: Int) {
        withContext(Dispatchers.IO) {
            savedRecipeDao.deleteById(recipeId)
        }
    }

    suspend fun getLocalRecipes(): List<Recipe> {
        return withContext(Dispatchers.IO) {
            savedRecipeDao.getAllSavedRecipes().map { it.toRecipe() }
        }
    }
}

// Add this extension function
fun SavedRecipe.toRecipe(): Recipe {
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
