package com.example.culinarycompanion.repository

import android.util.Log
import com.example.culinarycompanion.data.DataSource
import com.example.culinarycompanion.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecipeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")

    fun getRecipes(): Flow<List<Recipe>> = flow {
        // Simulate network/database fetch
        delay(500) // Add small delay to prevent UI freeze
        emit(DataSource.recipes)
    }

    // Add withContext to ensure IO operations run on background thread
    suspend fun getAllRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = recipesCollection.get().await()
            result.documents.mapNotNull { document ->
                document.toObject(Recipe::class.java)?.copy(id = document.id.toIntOrNull() ?: 0)
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error loading recipes: ${e.message}")
            emptyList()
        }
    }

    // Add withContext to ensure IO operations run on background thread
    suspend fun getRecipeById(id: Int): Recipe? = withContext(Dispatchers.IO) {
        return@withContext try {
            recipesCollection.document(id.toString()).get().await()
                .toObject(Recipe::class.java)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error loading recipe $id: ${e.message}")
            null
        }
    }

    // Add withContext to ensure IO operations run on background thread
    suspend fun uploadRecipes(recipes: List<Recipe>) = withContext(Dispatchers.IO) {
        try {
            recipes.forEach { recipe ->
                recipesCollection.document(recipe.id.toString()).set(recipe).await()
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error uploading recipes: ${e.message}")
        }
    }
}