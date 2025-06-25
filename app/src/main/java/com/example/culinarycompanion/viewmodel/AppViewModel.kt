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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(
    private val repository: CollectionRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _collections = MutableStateFlow<List<RecipeCollection>>(emptyList())
    val collections: StateFlow<List<RecipeCollection>> = _collections.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        loadData()
    }

    sealed class SyncStatus {
        object IDLE : SyncStatus()
        object SYNCING : SyncStatus()
        object SYNC_COMPLETE : SyncStatus()
        data class SYNC_ERROR(val message: String) : SyncStatus()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _syncStatus.value = SyncStatus.SYNCING

            try {
                // Load in sequence to ensure proper sync
                syncRecipes()
                loadRecipes()
                syncCollections()
                loadCollections()

                _syncStatus.value = SyncStatus.SYNC_COMPLETE
            } catch (e: Exception) {
                val errorMsg = "Failed to load data: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                _syncStatus.value = SyncStatus.SYNC_ERROR(errorMsg)
                Log.e("AppViewModel", "Data loading error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun syncRecipes() {
        try {
            Log.d("AppViewModel", "Syncing recipes...")
            repository.syncLocalFavorites()
            Log.d("AppViewModel", "Recipe sync completed")
        } catch (e: Exception) {
            Log.e("AppViewModel", "Recipe sync failed", e)
            throw e
        }
    }

    private suspend fun syncCollections() {
        try {
            Log.d("AppViewModel", "Syncing collections...")
            // Trigger sync by getting collections
            repository.getAllCollections()
            Log.d("AppViewModel", "Collection sync completed")
        } catch (e: Exception) {
            Log.e("AppViewModel", "Collection sync failed", e)
            throw e
        }
    }

    private suspend fun loadRecipes() {
        try {
            Log.d("AppViewModel", "Loading recipes...")
            val remoteRecipes = recipeRepository.getAllRecipes()
            val localFavorites = repository.getFavoritesFromFirestore() // Changed from getLocalRecipes()

            // Create mapping of favorites for quick lookup
            val favoriteMap = localFavorites.associate { it.id to it.isFavorite }

            _recipes.value = remoteRecipes.map { remoteRecipe ->
                remoteRecipe.copy(isFavorite = favoriteMap[remoteRecipe.id] ?: false)
            }
            Log.d("AppViewModel", "Loaded ${_recipes.value.size} recipes")
        } catch (e: Exception) {
            val errorMsg = "Failed to load recipes: ${e.message}"
            _error.value = errorMsg
            Log.e("AppViewModel", errorMsg, e)
        }
    }

    private suspend fun loadCollections() {
        try {
            Log.d("AppViewModel", "Loading collections...")
            _collections.value = withContext(Dispatchers.IO) {
                repository.getAllCollections()
            }
            Log.d("AppViewModel", "Loaded ${_collections.value.size} collections")
        } catch (e: Exception) {
            val errorMsg = "Failed to load collections: ${e.message}"
            _error.value = errorMsg
            Log.e("AppViewModel", errorMsg, e)
        }
    }

    fun toggleFavorite(recipe: Recipe, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                // Optimistic UI update
                _recipes.update { current ->
                    current.map { if (it.id == recipe.id) it.copy(isFavorite = isFavorite) else it }
                }

                repository.toggleFavorite(recipe, isFavorite)
                Log.d("AppViewModel", "Favorite toggled for recipe ${recipe.id}")
            } catch (e: Exception) {
                val errorMsg = "Failed to toggle favorite: ${e.message}"
                _error.value = errorMsg
                Log.e("AppViewModel", errorMsg, e)

                // Revert UI state
                _recipes.update { current ->
                    current.map { if (it.id == recipe.id) it.copy(isFavorite = !isFavorite) else it }
                }
            }
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val collectionId = repository.createCollection(name)
                loadCollections() // Refresh the list
                Log.d("AppViewModel", "Created collection $name with ID $collectionId")
            } catch (e: Exception) {
                val errorMsg = "Failed to create collection: ${e.message}"
                _error.value = errorMsg
                Log.e("AppViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCollection(collection: RecipeCollection) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteCollection(collection)
                loadCollections()
                Log.d("AppViewModel", "Deleted collection ${collection.id}")
            } catch (e: Exception) {
                val errorMsg = "Failed to delete collection: ${e.message}"
                _error.value = errorMsg
                Log.e("AppViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToCollection(recipe: Recipe, collectionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addToCollection(recipe.id, collectionId)
                loadCollections()
                Log.d("AppViewModel", "Added recipe ${recipe.id} to collection $collectionId")
            } catch (e: Exception) {
                val errorMsg = "Failed to add to collection: ${e.message}"
                _error.value = errorMsg
                Log.e("AppViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromCollection(recipe: Recipe, collectionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.removeFromCollection(recipe.id, collectionId)
                loadCollections()
                Log.d("AppViewModel", "Removed recipe ${recipe.id} from collection $collectionId")
            } catch (e: Exception) {
                val errorMsg = "Failed to remove from collection: ${e.message}"
                _error.value = errorMsg
                Log.e("AppViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadData()
    }
}