package com.example.culinarycompanion.repository

import android.util.Log
import com.example.culinarycompanion.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Define the repository interface
interface RecipeRepository {
    suspend fun getAllRecipes(): List<Recipe>
    suspend fun getRecipeById(id: String): Recipe?
    suspend fun addRecipe(recipe: Recipe): String
}
