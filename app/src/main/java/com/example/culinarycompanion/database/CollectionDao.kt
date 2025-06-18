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

    @Query("SELECT * FROM recipecollection ORDER BY createdAt DESC")
    suspend fun getAllCollections(): List<RecipeCollection>

    @Query("SELECT * FROM recipecollection WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: Long): RecipeCollection?

    // Transactional methods for safe list operations
    @Transaction
    suspend fun addToCollection(recipeId: Int, collectionId: Long) {
        val collection = getCollectionById(collectionId) ?: return
        val updatedIds = collection.recipeIds.toMutableList().apply {
            if (!contains(recipeId)) add(recipeId)
        }
        collection.recipeIds = updatedIds
        updateCollection(collection)
    }

    @Transaction
    suspend fun removeFromCollection(recipeId: Int, collectionId: Long) {
        val collection = getCollectionById(collectionId) ?: return
        val updatedIds = collection.recipeIds.toMutableList().apply {
            remove(recipeId)
        }
        collection.recipeIds = updatedIds
        updateCollection(collection)
    }

    @Query("SELECT * FROM recipecollection")
    suspend fun getAllCollectionsRaw(): List<RecipeCollection>

    suspend fun getCollectionsContainingRecipe(recipeId: Int): List<RecipeCollection> {
        return getAllCollectionsRaw().filter { it.recipeIds.contains(recipeId) }
    }
}