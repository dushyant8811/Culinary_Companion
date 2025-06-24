package com.example.culinarycompanion.repository

import android.util.Log
import com.example.culinarycompanion.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseRecipeRepository : RecipeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")

    override suspend fun getAllRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            val result = recipesCollection.get().await()
            result.documents.mapNotNull { document ->
                document.toObject(Recipe::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRecipeRepo", "Error loading recipes", e)
            emptyList()
        }
    }

    override suspend fun getRecipeById(id: String): Recipe? = withContext(Dispatchers.IO) {
        try {
            val document = recipesCollection.document(id).get().await()
            document.toObject(Recipe::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            Log.e("FirebaseRecipeRepo", "Error getting recipe $id", e)
            null
        }
    }

}