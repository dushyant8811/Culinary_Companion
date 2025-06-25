package com.example.culinarycompanion.repository

import android.util.Log
import com.example.culinarycompanion.database.CollectionDao
import com.example.culinarycompanion.database.SavedRecipe
import com.example.culinarycompanion.database.SavedRecipeDao
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val savedRecipeDao: SavedRecipeDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User not authenticated")

    suspend fun getAllCollections(): List<RecipeCollection> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("users/$userId/collections")
                    .get()
                    .await()

                val firestoreCollections = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    try {
                        RecipeCollection(
                            id = doc.id, // ✅ Fix: Use Firestore document ID
                            name = data["name"] as? String ?: return@mapNotNull null,
                            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                            recipeIds = data["recipeIds"] as? List<String> ?: emptyList(),
                            ownerId = userId
                        )
                    } catch (e: Exception) {
                        Log.e("CollectionRepo", "Error parsing collection ${doc.id}", e)
                        null
                    }
                }

                if (firestoreCollections.isNotEmpty()) {
                    collectionDao.deleteAllCollections()
                    collectionDao.insertAllCollections(firestoreCollections)
                    return@withContext firestoreCollections
                }

                // Fallback: local data
                collectionDao.getAllCollections()
            } catch (e: Exception) {
                Log.e("CollectionRepo", "Error getting collections", e)
                collectionDao.getAllCollections()
            }
        }
    }


    // In CollectionRepository.kt, modify the createCollection function
    suspend fun createCollection(name: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Create in Firestore
                val collectionData = hashMapOf(
                    "name" to name,
                    "recipeIds" to emptyList<String>(),
                    "createdAt" to System.currentTimeMillis()
                )

                val docRef = firestore.collection("users/$userId/collections")
                    .add(collectionData)
                    .await()

                // Use the actual Firestore document ID
                val collectionId = docRef.id
                val collection = RecipeCollection(
                    id = collectionId,
                    name = name,
                    createdAt = System.currentTimeMillis(),
                    recipeIds = emptyList(),
                    ownerId = userId
                )
                collectionDao.insertCollection(collection)

                collectionId
            } catch (e: Exception) {
                Log.e("CollectionRepo", "Error creating collection", e)
                throw e
            }
        }
    }

    // Modify getCollectionById to use String ID
    suspend fun getCollectionById(collectionId: String): RecipeCollection? {
        return withContext(Dispatchers.IO) {
            try {
                // Try Firestore first
                val doc = firestore.collection("users/$userId/collections")
                    .document(collectionId)
                    .get()
                    .await()

                val firestoreCollection = doc.toObject(RecipeCollection::class.java)
                if (firestoreCollection != null) {
                    // Update local database
                    collectionDao.insertCollection(firestoreCollection)
                    return@withContext firestoreCollection
                }

                // Fall back to local
                collectionDao.getCollectionById(collectionId)
            } catch (e: Exception) {
                Log.e("CollectionRepo", "Error getting collection", e)
                collectionDao.getCollectionById(collectionId)
            }
        }
    }

    // Modify deleteCollection to use String ID
    suspend fun deleteCollection(collection: RecipeCollection) {
        withContext(Dispatchers.IO) {
            try {
                // Delete from Firestore
                firestore.collection("users/$userId/collections")
                    .document(collection.id)
                    .delete()
                    .await()

                // Delete locally
                collectionDao.deleteCollection(collection)
            } catch (e: Exception) {
                Log.e("CollectionRepo", "Error deleting collection", e)
                throw e
            }
        }
    }

    // Modify addToCollection to use String IDs
    suspend fun addToCollection(recipeId: String, collectionId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Get current collection
                val collection = getCollectionById(collectionId)
                    ?: throw Exception("Collection not found")

                // Update recipe IDs
                val updatedIds = if (recipeId in collection.recipeIds) {
                    collection.recipeIds
                } else {
                    collection.recipeIds + recipeId
                }

                // Update Firestore
                firestore.collection("users/$userId/collections")
                    .document(collectionId)
                    .update("recipeIds", updatedIds)
                    .await()

                // Update local
                collectionDao.addToCollection(recipeId, collectionId)
            } catch (e: Exception) {
                Log.e("CollectionRepo", "Error adding to collection", e)
                throw e
            }
        }
    }

    // Modify removeFromCollection to use String IDs
    suspend fun removeFromCollection(recipeId: String, collectionId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Get current collection
                val collection = getCollectionById(collectionId)
                    ?: throw Exception("Collection not found")

                // Update recipe IDs
                val updatedIds = collection.recipeIds - recipeId

                // Update Firestore
                firestore.collection("users/$userId/collections")
                    .document(collectionId)
                    .update("recipeIds", updatedIds)
                    .await()

                // Update local
                collectionDao.removeFromCollection(recipeId, collectionId)
            } catch (e: Exception) {
                Log.e("CollectionRepo", "Error removing from collection", e)
                throw e
            }
        }
    }

    suspend fun toggleFavorite(recipe: Recipe, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                val favoriteRef = firestore.collection("users/$userId/favorites")
                    .document(recipe.id)

                if (isFavorite) {
                    // Add to Firestore
                    favoriteRef.set(recipe.toFirestoreMap()).await()
                    // Add locally
                    savedRecipeDao.insert(recipe.toSavedRecipe())
                } else {
                    // Remove from Firestore
                    favoriteRef.delete().await()
                    // Remove locally
                    savedRecipeDao.deleteById(recipe.id)
                }
            } catch (e: Exception) {
                Log.e("CollectionRepo", "Error toggling favorite", e)
                throw e
            }
        }
    }

    suspend fun syncLocalFavorites() {
        withContext(Dispatchers.IO) {
            try {
                // Fetch favorites from Firestore
                val firestoreFavorites = getFavoritesFromFirestore()
                Log.d("CollectionRepo", "Fetched ${firestoreFavorites.size} favorites from Firestore")

                // Delete existing favorites in local database
                savedRecipeDao.deleteAll()
                Log.d("CollectionRepo", "Deleted all local favorites")

                // Insert fetched favorites into local database
                firestoreFavorites.forEach { recipe ->
                    val savedRecipe = recipe.toSavedRecipe()
                    savedRecipeDao.insert(savedRecipe)
                    Log.d("CollectionRepo", "Inserted favorite: ${recipe.id}")
                }

                Log.d("CollectionRepo", "Successfully synced favorites")
            } catch (e: Exception) {
                Log.e("CollectionRepo", "Error syncing favorites", e)
                // Consider rethrowing the exception to handle it in the caller
                throw e
            }
        }
    }

    suspend fun getFavoritesFromFirestore(): List<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("users/$userId/favorites")
                    .get()
                    .await()
                    .toObjects(Recipe::class.java)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun Recipe.toFirestoreMap(): Map<String, Any?> {
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
            "isFavorite" to true // Always true when saved as favorite
        )
    }

    private fun Recipe.toSavedRecipe(): SavedRecipe {
        return SavedRecipe(
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
            isFavorite = true,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun SavedRecipe.toRecipe(): Recipe {
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
}