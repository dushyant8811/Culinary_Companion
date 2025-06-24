package com.example.culinarycompanion.database

import androidx.room.*
import com.example.culinarycompanion.model.RecipeCollection

@Dao
interface CollectionDao {
    @Insert
    suspend fun insertCollection(collection: RecipeCollection): Long

    @Update
    suspend fun updateCollection(collection: RecipeCollection)

    @Delete
    suspend fun deleteCollection(collection: RecipeCollection)

    // Remove ORDER BY createdAt since it doesn't exist

    @Query("SELECT * FROM recipecollection WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: Long): RecipeCollection?

    @Query("SELECT * FROM recipecollection ORDER BY createdAt DESC")
    suspend fun getAllCollections(): List<RecipeCollection>

    // Transactional methods for safe list operations
    @Transaction
    suspend fun addToCollection(recipeId: String, collectionId: Long) {
        val collection = getCollectionById(collectionId) ?: return
        val updatedIds = collection.recipeIds.toMutableList().apply {
            if (!contains(recipeId)) add(recipeId)
        }
        collection.recipeIds = updatedIds
        updateCollection(collection)
    }

    @Transaction
    suspend fun removeFromCollection(recipeId: String, collectionId: Long) {
        val collection = getCollectionById(collectionId) ?: return
        val updatedIds = collection.recipeIds.toMutableList().apply {
            remove(recipeId)
        }
        collection.recipeIds = updatedIds
        updateCollection(collection)
    }

    // Fix recipeId type to String
    suspend fun getCollectionsContainingRecipe(recipeId: String): List<RecipeCollection> {
        return getAllCollections().filter { it.recipeIds.contains(recipeId) }
    }
}