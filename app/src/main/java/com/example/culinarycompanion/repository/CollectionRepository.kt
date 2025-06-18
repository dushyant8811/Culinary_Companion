package com.example.culinarycompanion.repository

import com.example.culinarycompanion.database.CollectionDao
import com.example.culinarycompanion.model.RecipeCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollectionRepository(private val collectionDao: CollectionDao) {

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
}