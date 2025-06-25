package com.example.culinarycompanion.database

import androidx.room.*
import com.example.culinarycompanion.model.RecipeCollection

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: RecipeCollection)

    @Update
    suspend fun updateCollection(collection: RecipeCollection)

    @Delete
    suspend fun deleteCollection(collection: RecipeCollection)

    @Query("DELETE FROM recipecollection")
    suspend fun deleteAllCollections()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCollections(collections: List<RecipeCollection>)

    @Query("SELECT * FROM recipecollection WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: String): RecipeCollection?

    @Query("SELECT * FROM recipecollection ORDER BY createdAt DESC")
    suspend fun getAllCollections(): List<RecipeCollection>

    // This will work correctly since it operates on getAllCollections
    suspend fun getCollectionsContainingRecipe(recipeId: String): List<RecipeCollection> {
        return getAllCollections().filter { collection ->
            collection.recipeIds.contains(recipeId)
        }
    }

    @Transaction
    suspend fun addToCollection(recipeId: String, collectionId: String) {
        val collection = getCollectionById(collectionId) ?: return
        val updatedIds = if (collection.recipeIds.contains(recipeId)) {
            collection.recipeIds
        } else {
            collection.recipeIds + recipeId
        }
        updateCollection(collection.copy(recipeIds = updatedIds))
    }

    @Transaction
    suspend fun removeFromCollection(recipeId: String, collectionId: String) {
        val collection = getCollectionById(collectionId) ?: return
        val updatedIds = collection.recipeIds - recipeId
        updateCollection(collection.copy(recipeIds = updatedIds))
    }
}
