package com.example.culinarycompanion.repository

import android.util.Log
import com.example.culinarycompanion.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.culinarycompanion.model.Review
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction

class FirebaseRecipeRepository : RecipeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")

    override suspend fun getAllRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d("FirebaseRepo", "Starting recipe fetch from Firestore")

            val result = recipesCollection
                .orderBy("title")
                .limit(100)
                .get()
                .await()

            if (result.isEmpty) {
                Log.w("FirebaseRepo", "No recipes found in Firestore")
                return@withContext emptyList()
            }

            Log.d("FirebaseRepo", "Retrieved ${result.size()} documents")

            val recipes = result.documents.mapNotNull { document ->
                try {
                    val recipe = document.toObject(Recipe::class.java)
                    if (recipe == null) {
                        Log.w("FirebaseRepo", "Failed to parse document ${document.id}")
                        null
                    } else {
                        recipe.copy(id = document.id).also {
                            Log.v("FirebaseRepo", "Parsed recipe: ${it.title} (ID: ${it.id})")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseRepo", "Error parsing document ${document.id}", e)
                    null
                }
            }

            Log.d("FirebaseRepo", "Successfully parsed ${recipes.size} recipes")
            recipes
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Failed to fetch recipes", e)
            emptyList()
        }
    }

    override suspend fun getRecipeById(id: String): Recipe? = withContext(Dispatchers.IO) {
        try {
            Log.d("FirebaseRepo", "Fetching recipe with ID: $id")
            val document = recipesCollection.document(id).get().await()

            if (!document.exists()) {
                Log.w("FirebaseRepo", "Recipe $id not found")
                return@withContext null
            }

            document.toObject(Recipe::class.java)?.copy(id = document.id)?.also {
                Log.d("FirebaseRepo", "Found recipe: ${it.title}")
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error getting recipe $id", e)
            null
        }
    }

    override suspend fun addRecipe(recipe: Recipe): String = withContext(Dispatchers.IO) {
        try {
            val documentReference = recipesCollection.add(recipe.toMap()).await()
            Log.d("FirebaseRepo", "Recipe added successfully with ID: ${documentReference.id}")

            val newId = documentReference.id
            recipesCollection.document(newId).update("id", newId).await()
            return@withContext newId
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error adding recipe", e)
            throw e
        }
    }

    override suspend fun getReviewsForRecipe(recipeId: String): List<Review> {
        return try {
            recipesCollection.document(recipeId).collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await().toObjects(Review::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error getting reviews for recipe $recipeId", e)
            emptyList()
        }
    }

    override suspend fun submitReview(review: Review) {
        withContext(Dispatchers.IO) {
            Log.d("ReviewDebug", "4. Repository: submitReview called.")
            val recipeRef = recipesCollection.document(review.recipeId)
            val reviewRef = recipeRef.collection("reviews").document()
            review.id = reviewRef.id

            try {
                db.runTransaction { transaction ->
                    Log.d("ReviewDebug", "4a. Repository: Transaction started.")
                    val recipeSnapshot = transaction.get(recipeRef)

                    val currentReviewCount = recipeSnapshot.getLong("reviewCount")?.toInt() ?: 0
                    val currentAverageRating = recipeSnapshot.getDouble("averageRating")?.toFloat() ?: 0.0f
                    Log.d("ReviewDebug", "4b. Repository: Current state: count=$currentReviewCount, avg=$currentAverageRating")

                    val newReviewCount = currentReviewCount + 1
                    val newAverageRating = ((currentAverageRating * currentReviewCount) + review.rating) / newReviewCount
                    Log.d("ReviewDebug", "4c. Repository: New state: count=$newReviewCount, avg=$newAverageRating")

                    transaction.update(recipeRef, "reviewCount", newReviewCount)
                    transaction.update(recipeRef, "averageRating", newAverageRating)
                    transaction.set(reviewRef, review)
                    Log.d("ReviewDebug", "4d. Repository: Transaction updates queued.")

                    null
                }.await()
                Log.d("ReviewDebug", "4e. Repository: Transaction successfully committed.")
            } catch (e: Exception) {
                Log.e("ReviewDebug", "4f. Repository: TRANSACTION FAILED: ${e.message}", e)
            }
        }
    }
}