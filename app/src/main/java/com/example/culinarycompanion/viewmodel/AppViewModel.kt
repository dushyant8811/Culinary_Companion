package com.example.culinarycompanion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection
import com.example.culinarycompanion.repository.CollectionRepository
import com.example.culinarycompanion.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.culinarycompanion.util.ConnectivityUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class AppViewModel(
    application: Application,
    private val repository: CollectionRepository,
    private val recipeRepository: RecipeRepository
) : AndroidViewModel(application) {

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

    private val _downloadedRecipeIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedRecipeIds: StateFlow<Set<String>> = _downloadedRecipeIds.asStateFlow()

    init {
        // Load the set of downloaded IDs when the ViewModel is created
        loadDownloadedIds()
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

            val context = getApplication<Application>().applicationContext
            if (ConnectivityUtil.isOnline(context)) {
                // --- ONLINE PATH ---
                Log.d("AppViewModel", "Device is ONLINE. Fetching from cloud.")
                try {
                    val recipesJob = async { loadRecipes() } // loadRecipes fetches from Firestore
                    val collectionsJob = async { loadCollections() }
                    awaitAll(recipesJob, collectionsJob)
                } catch (e: Exception) {
                    _error.value = "Error fetching cloud data: ${e.message}"
                }
            } else {
                // --- OFFLINE PATH ---
                Log.d("AppViewModel", "Device is OFFLINE. Loading from local database.")
                try {
                    val localRecipes = repository.getDownloadedRecipes()
                    _recipes.value = localRecipes
                    // Optionally, load collections from local cache too
                    _collections.value = repository.getAllCollections()
                } catch (e: Exception) {
                    _error.value = "Error loading offline data: ${e.message}"
                }
            }
            _isLoading.value = false
        }
    }

    private fun loadDownloadedIds() {
        viewModelScope.launch {
            _downloadedRecipeIds.value = repository.getDownloadedRecipeIds().toSet()
        }
    }

    fun toggleDownload(recipe: Recipe) {
        viewModelScope.launch {
            val isCurrentlyDownloaded = _downloadedRecipeIds.value.contains(recipe.id)
            try {
                if (isCurrentlyDownloaded) {
                    // --- UN-DOWNLOAD ---
                    repository.removeRecipeFromOffline(recipe.id)
                    // Optimistic UI update
                    _downloadedRecipeIds.update { it - recipe.id }
                    Log.d("AppViewModel", "Removed ${recipe.title} from offline cache.")
                } else {
                    // --- DOWNLOAD ---
                    repository.saveRecipeForOffline(recipe)
                    // Optimistic UI update
                    _downloadedRecipeIds.update { it + recipe.id }
                    Log.d("AppViewModel", "Saved ${recipe.title} for offline.")
                }
            } catch (e: Exception) {
                _error.value = "Failed to update offline status: ${e.message}"
                // TODO: Revert optimistic update on failure
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

    fun createRecipe(
        title: String,
        ingredients: List<String>,
        instructions: List<String>,
        prepTime: Int,
        cookTime: Int,
        servings: Int,
        dietaryTags: List<String>,
        category: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {

                val currentUser = Firebase.auth.currentUser
                val authorName = currentUser?.displayName ?: "Anonymous User"

                val newRecipe = Recipe(
                    title = title,
                    ingredients = ingredients.filter { it.isNotBlank() },
                    instructions = instructions.filter { it.isNotBlank() },
                    prepTime = prepTime,
                    cookTime = cookTime,
                    servings = servings,
                    category = category,
                    dietaryTags = dietaryTags,
                    imageUrl = null,
                    author = authorName,
                    isFavorite = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val newId = recipeRepository.addRecipe(newRecipe)
                val finalRecipe = newRecipe.copy(id = newId)

                _recipes.update { currentList ->
                    listOf(finalRecipe) + currentList
                }
                Log.d("AppViewModel", "Successfully created recipe: $title")

            } catch (e: Exception) {
                _error.value = "Failed to create recipe: ${e.message}"
                Log.e("AppViewModel", "Recipe creation failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}