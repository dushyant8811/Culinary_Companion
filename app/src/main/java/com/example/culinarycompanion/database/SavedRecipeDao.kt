package com.example.culinarycompanion.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SavedRecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: SavedRecipe)

    @Update
    suspend fun update(recipe: SavedRecipe)

    @Delete
    suspend fun delete(recipe: SavedRecipe)

    @Query("SELECT * FROM saved_recipes")
    suspend fun getAllSavedRecipes(): List<SavedRecipe>

    @Query("SELECT * FROM saved_recipes WHERE id = :recipeId")
    suspend fun getSavedRecipe(recipeId: String): SavedRecipe?

    @Query("DELETE FROM saved_recipes WHERE id = :recipeId")
    suspend fun deleteById(recipeId: String)

    @Query("DELETE FROM saved_recipes")
    suspend fun deleteAll()

    @Query("SELECT * FROM saved_recipes WHERE isFavorite = 1")
    suspend fun getFavoriteRecipes(): List<SavedRecipe>

    @Query("SELECT * FROM saved_recipes WHERE lastUpdated > :timestamp")
    suspend fun getRecipesUpdatedAfter(timestamp: Long): List<SavedRecipe>
}