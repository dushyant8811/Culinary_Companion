package com.example.culinarycompanion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection
import com.example.culinarycompanion.repository.CollectionRepository
import com.example.culinarycompanion.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(
    private val repository: CollectionRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> get() = _recipes

    private val _collections = MutableStateFlow<List<RecipeCollection>>(emptyList())
    val collections: StateFlow<List<RecipeCollection>> get() = _collections

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    init {
        loadData()
    }

    // Combined data loading
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Load both in parallel
                val recipesDeferred = launch { loadRecipes() }
                val collectionsDeferred = launch { loadCollections() }

                // Wait for both to complete
                recipesDeferred.join()
                collectionsDeferred.join()
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
                Log.e("AppViewModel", "Data loading error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Separate function to load recipes
    private suspend fun loadRecipes() {
        try {
            _recipes.value = recipeRepository.getAllRecipes()
        } catch (e: Exception) {
            _error.value = "Failed to load recipes: ${e.message}"
            Log.e("AppViewModel", "Recipe loading error", e)
        }
    }

    // Separate function to load collections
    private suspend fun loadCollections() {
        try {
            _collections.value = withContext(Dispatchers.IO) {
                repository.getAllCollections()
            }
        } catch (e: Exception) {
            _error.value = "Failed to load collections: ${e.message}"
            Log.e("AppViewModel", "Collection loading error", e)
        }
    }

    fun toggleFavorite(recipe: Recipe, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Update in-memory state
                val updatedRecipes = _recipes.value.toMutableList()
                val index = updatedRecipes.indexOfFirst { it.id == recipe.id }
                if (index != -1) {
                    updatedRecipes[index] = updatedRecipes[index].copy(isFavorite = isFavorite)
                    _recipes.value = updatedRecipes
                }

                // Persist to local database
                if (isFavorite) {
                    repository.saveRecipeLocally(recipe)
                } else {
                    repository.deleteLocalRecipe(recipe.id)
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error toggling favorite", e)
            }
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newCollection = RecipeCollection(name = name)
                repository.createCollection(newCollection)
                loadCollections()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error creating collection", e)
            }
        }
    }

    fun deleteCollection(collection: RecipeCollection) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteCollection(collection)
                loadCollections()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error deleting collection", e)
            }
        }
    }

    fun addToCollection(recipe: Recipe, collectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addToCollection(recipe.id, collectionId)
                loadCollections()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error adding to collection", e)
            }
        }
    }

    fun removeFromCollection(recipe: Recipe, collectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.removeFromCollection(recipe.id, collectionId)
                loadCollections()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error removing from collection", e)
            }
        }
    }
}