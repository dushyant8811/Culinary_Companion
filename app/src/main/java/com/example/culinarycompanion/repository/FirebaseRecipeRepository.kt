package com.example.culinarycompanion.repository

import android.util.Log
import com.example.culinarycompanion.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
}