package com.example.culinarycompanion.viewmodel

import android.app.Application
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection
import com.example.culinarycompanion.model.Review
import com.example.culinarycompanion.repository.CollectionRepository
import com.example.culinarycompanion.repository.FirebaseRecipeRepository
import com.example.culinarycompanion.repository.RecipeRepository
import com.example.culinarycompanion.util.ConnectivityUtil
import com.example.culinarycompanion.util.TtsHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AppViewModel(
    application: Application,
    private val repository: CollectionRepository,
    private val recipeRepository: RecipeRepository
) : AndroidViewModel(application) {

    // --- Core App State ---
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _collections = MutableStateFlow<List<RecipeCollection>>(emptyList())
    val collections: StateFlow<List<RecipeCollection>> = _collections.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _downloadedRecipeIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedRecipeIds: StateFlow<Set<String>> = _downloadedRecipeIds.asStateFlow()

    // --- Cooking Mode State ---
    private val ttsHelper = TtsHelper(application)
    private val _isReadingAloud = MutableStateFlow(false)
    val isReadingAloud: StateFlow<Boolean> = _isReadingAloud.asStateFlow()
    private val _currentInstructionIndex = MutableStateFlow(0)
    val currentInstructionIndex: StateFlow<Int> = _currentInstructionIndex.asStateFlow()
    private val _currentRecipeForReading = MutableStateFlow<Recipe?>(null)
    val currentRecipeForReading: StateFlow<Recipe?> = _currentRecipeForReading.asStateFlow()
    private val _listenNowEvent = MutableSharedFlow<Unit>()
    val listenNowEvent = _listenNowEvent.asSharedFlow()

    // --- Reviews State ---
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _reviewsAreLoading = MutableStateFlow(false)
    val reviewsAreLoading: StateFlow<Boolean> = _reviewsAreLoading.asStateFlow()

    init {
        loadDownloadedIds()
        ttsHelper.onSpeechDone = {
            viewModelScope.launch {
                _listenNowEvent.emit(Unit)
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            if (ConnectivityUtil.isOnline(getApplication())) {
                try {
                    awaitAll(async { loadRecipes() }, async { loadCollections() })
                } catch (e: Exception) {
                    _error.value = "Error fetching cloud data: ${e.message}"
                }
            } else {
                try {
                    _recipes.value = repository.getDownloadedRecipes()
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
            if (downloadedRecipeIds.value.contains(recipe.id)) {
                repository.removeRecipeFromOffline(recipe.id)
                _downloadedRecipeIds.update { it - recipe.id }
            } else {
                repository.saveRecipeForOffline(recipe)
                _downloadedRecipeIds.update { it + recipe.id }
            }
        }
    }

    private suspend fun loadRecipes() {
        try {
            _recipes.value = recipeRepository.getAllRecipes()
        } catch (e: Exception) {
            _error.value = "Failed to load recipes: ${e.message}"
        }
    }

    private suspend fun loadCollections() {
        try {
            _collections.value = repository.getAllCollections()
        } catch (e: Exception) {
            _error.value = "Failed to load collections: ${e.message}"
        }
    }

    fun toggleFavorite(recipe: Recipe, isFavorite: Boolean) {
        viewModelScope.launch {
            _recipes.update { current ->
                current.map { if (it.id == recipe.id) it.copy(isFavorite = isFavorite) else it }
            }
            repository.toggleFavorite(recipe, isFavorite)
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            repository.createCollection(name)
            loadCollections()
        }
    }

    fun deleteCollection(collection: RecipeCollection) {
        viewModelScope.launch {
            repository.deleteCollection(collection)
            loadCollections()
        }
    }

    fun addToCollection(recipe: Recipe, collectionId: String) {
        viewModelScope.launch {
            repository.addToCollection(recipe.id, collectionId)
            loadCollections()
        }
    }

    fun removeFromCollection(recipe: Recipe, collectionId: String) {
        viewModelScope.launch {
            repository.removeFromCollection(recipe.id, collectionId)
            loadCollections()
        }
    }

    fun createRecipe(title: String, description: String, ingredients: List<String>, instructions: List<String>, prepTime: Int, cookTime: Int, servings: Int, category: String, dietaryTags: List<String>) {
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser ?: return@launch
            val newRecipe = Recipe(
                title = title,
                description = description,
                ingredients = ingredients.filter { it.isNotBlank() },
                instructions = instructions.filter { it.isNotBlank() },
                prepTime = prepTime,
                cookTime = cookTime,
                servings = servings,
                category = category,
                author = currentUser.displayName ?: "Anonymous",
                dietaryTags = dietaryTags,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val newId = recipeRepository.addRecipe(newRecipe)
            _recipes.update { listOf(newRecipe.copy(id = newId)) + it }
        }
    }

    fun startReading(recipe: Recipe) {
        _currentRecipeForReading.value = recipe
        _isReadingAloud.value = true
        _currentInstructionIndex.value = 0
        readCurrentStep()
    }

    fun stopReading() {
        _isReadingAloud.value = false
        _currentRecipeForReading.value = null
    }

    fun nextStep() {
        val recipe = _currentRecipeForReading.value ?: return
        if (_currentInstructionIndex.value < recipe.instructions.size - 1) {
            _currentInstructionIndex.update { it + 1 }
            readCurrentStep()
        } else {
            ttsHelper.speak("You have reached the final step.")
        }
    }

    fun previousStep() {
        if (_currentInstructionIndex.value > 0) {
            _currentInstructionIndex.update { it - 1 }
            readCurrentStep()
        }
    }

    fun repeatStep() {
        readCurrentStep()
    }

    private fun readCurrentStep() {
        viewModelScope.launch {
            ttsHelper.isInitialized.first { it }
            val recipe = _currentRecipeForReading.value
            val index = _currentInstructionIndex.value
            recipe?.instructions?.getOrNull(index)?.let {
                ttsHelper.speak("Step ${index + 1}: $it")
            }
        }
    }

    private fun handleCommand(command: String) {
        when {
            command.contains("next") -> nextStep()
            command.contains("previous") || command.contains("back") -> previousStep()
            command.contains("repeat") -> repeatStep()
            command.contains("stop") -> stopReading()
        }
    }

    fun processVoiceCommandResult(data: Intent?) {
        data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let {
            handleCommand(it.lowercase(Locale.getDefault()))
        }
    }

    fun loadReviewsFor(recipeId: String) {
        viewModelScope.launch {
            _reviewsAreLoading.value = true
            _reviews.value = emptyList()
            try {
                _reviews.value = recipeRepository.getReviewsForRecipe(recipeId)
            } catch (e: Exception) {
                _error.value = "Failed to load reviews: ${e.message}"
            } finally {
                _reviewsAreLoading.value = false
            }
        }
    }

    fun submitReview(recipeId: String, rating: Float, text: String) {
        viewModelScope.launch {
            _reviewsAreLoading.value = true
            try {
                val currentUser = Firebase.auth.currentUser ?: throw Exception("User not logged in")

                val newReview = Review(
                    recipeId = recipeId,
                    authorId = currentUser.uid,
                    authorName = currentUser.displayName ?: "Anonymous",
                    rating = rating,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                recipeRepository.submitReview(newReview)


                _reviews.update { currentReviews ->
                    listOf(newReview.copy(id = "local_${System.currentTimeMillis()}")) + currentReviews
                }

                val updatedRecipe = recipeRepository.getRecipeById(recipeId)

                if (updatedRecipe != null) {
                    _recipes.update { currentList ->
                        currentList.map { if (it.id == recipeId) updatedRecipe else it }
                    }
                }

            } catch (e: Exception) {
                _error.value = "Failed to submit review: ${e.message}"

                loadReviewsFor(recipeId)
            } finally {
                _reviewsAreLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.stop()
    }
}