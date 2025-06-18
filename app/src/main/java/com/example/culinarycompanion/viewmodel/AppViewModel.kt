package com.example.culinarycompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCollection
import com.example.culinarycompanion.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _collections = MutableStateFlow<List<RecipeCollection>>(emptyList())
    val collections: StateFlow<List<RecipeCollection>> get() = _collections

    init {
        loadCollections()
    }

    fun loadCollections() {
        viewModelScope.launch {
            _collections.value = repository.getAllCollections()
        }
    }

    fun toggleFavorite(recipe: Recipe, isFavorite: Boolean) {
        // In-memory toggle (not persisted to database)
        recipe.isFavorite = isFavorite
    }

    // FIXED: Removed description parameter since RecipeCollection doesn't have it
    fun createCollection(name: String) {
        viewModelScope.launch {
            val newCollection = RecipeCollection(name = name)
            repository.createCollection(newCollection)
            loadCollections()
        }
    }

    fun deleteCollection(collection: RecipeCollection) {
        viewModelScope.launch {
            repository.deleteCollection(collection)
            loadCollections()
        }
    }

    fun addToCollection(recipe: Recipe, collectionId: Long) {
        viewModelScope.launch {
            repository.addToCollection(recipe.id, collectionId)
            loadCollections()
        }
    }

    fun removeFromCollection(recipe: Recipe, collectionId: Long) {
        viewModelScope.launch {
            repository.removeFromCollection(recipe.id, collectionId)
            loadCollections()
        }
    }
}